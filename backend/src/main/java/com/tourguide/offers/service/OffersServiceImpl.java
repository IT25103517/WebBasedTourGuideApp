package com.tourguide.offers.service;

import com.tourguide.offers.dto.CreateOfferRequest;
import com.tourguide.offers.dto.UpdateOfferRequest;
import com.tourguide.offers.model.ExpiredOffersResult;
import com.tourguide.offers.model.OfferPreview;
import com.tourguide.offers.model.SpecialOffer;
import com.tourguide.offers.repository.OfferOwnership;
import com.tourguide.offers.repository.OffersRepository;
import com.tourguide.shared.error.ApiException;
import com.tourguide.shared.model.DeletionResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Port of the original OffersService, now delegating all SQL to {@link OffersRepository}. */
@Service
public class OffersServiceImpl implements OffersService {

    private final OffersRepository repository;

    public OffersServiceImpl(OffersRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<SpecialOffer> listActive(Integer packageId, Integer guideId) {
        return repository.findActive(packageId, guideId);
    }

    @Override
    public List<SpecialOffer> listByGuide(int guideId) {
        return repository.findByGuide(guideId);
    }

    @Override
    public SpecialOffer getById(int offerId) {
        return repository.findById(offerId).orElseThrow(() -> ApiException.notFound("Offer not found"));
    }

    private OfferOwnership assertOwnership(int offerId, int guideId) {
        OfferOwnership offer = repository.findOwnership(offerId)
                .orElseThrow(() -> ApiException.notFound("Offer not found"));
        if (offer.guideId() != guideId) throw ApiException.forbidden("That offer belongs to another guide");
        return offer;
    }

    /** The packages must all belong to the guide creating/editing the offer. */
    private void assertPackagesOwned(List<Integer> packageIds, int guideId) {
        if (packageIds == null || packageIds.isEmpty()) return;
        Map<Integer, Integer> owners = repository.findPackageOwners(packageIds);
        if (owners.size() != packageIds.size()) throw ApiException.badRequest("One or more packages do not exist");
        List<Integer> foreign = packageIds.stream().filter(id -> owners.get(id) != guideId).toList();
        if (!foreign.isEmpty()) {
            throw ApiException.forbidden("You can only promote your own packages", Map.of("package_ids", foreign));
        }
    }

    private void checkBusinessRules(LocalDate start, LocalDate end, String discountType, BigDecimal discountValue) {
        Map<String, Object> errors = new LinkedHashMap<>();
        if (start != null && end != null && end.isBefore(start)) {
            errors.put("end_date", "end_date cannot be before start_date");
        }
        if ("PERCENT".equals(discountType) && discountValue != null && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            errors.put("discount_value", "a percentage discount cannot be more than 100");
        }
        if (!errors.isEmpty()) throw ApiException.badRequest("Validation failed", errors);
    }

    @Override
    @Transactional
    public SpecialOffer create(int guideId, CreateOfferRequest data, List<Integer> packageIds) {
        checkBusinessRules(data.start_date(), data.end_date(), data.discount_type(), data.discount_value());
        if (packageIds == null || packageIds.isEmpty()) {
            throw ApiException.badRequest("package_ids is required (an array of package ids)");
        }
        assertPackagesOwned(packageIds, guideId);

        int offerId = repository.create(guideId, data, packageIds);
        return getById(offerId);
    }

    @Override
    @Transactional
    public SpecialOffer update(int offerId, int guideId, UpdateOfferRequest data, List<Integer> packageIds) {
        OfferOwnership current = assertOwnership(offerId, guideId);
        if (packageIds != null) assertPackagesOwned(packageIds, guideId);

        LocalDate start = data.start_date() != null ? data.start_date() : current.startDate();
        LocalDate end = data.end_date() != null ? data.end_date() : current.endDate();
        String discountType = data.discount_type() != null ? data.discount_type() : current.discountType();
        BigDecimal discountValue = data.discount_value() != null ? data.discount_value() : current.discountValue();
        checkBusinessRules(start, end, discountType, discountValue);

        repository.update(offerId, data, packageIds);
        return getById(offerId);
    }

    /**
     * Delete an offer. If any booking already used it we keep the record
     * (marked EXPIRED) so the price history on that booking still makes sense.
     */
    @Override
    public DeletionResult remove(int offerId, int guideId) {
        assertOwnership(offerId, guideId);

        long used = repository.countBookingsUsingOffer(offerId);
        if (used > 0) {
            repository.markExpired(offerId);
            return new DeletionResult(false,
                    used + " booking(s) used this offer, so it was marked EXPIRED instead of deleted");
        }

        repository.delete(offerId);
        return new DeletionResult(true, "Offer deleted");
    }

    /** "Remove expired promotions": flags every offer whose end_date has passed. */
    @Override
    public ExpiredOffersResult expireOutdated(int guideId) {
        return repository.expireOutdated(guideId);
    }

    /** Preview what an offer does to a price, used by the booking form. */
    @Override
    public OfferPreview preview(int offerId, int packageId, int groupSize) {
        return repository.preview(offerId, packageId, groupSize)
                .orElseThrow(() -> ApiException.notFound("That offer does not apply to this package"));
    }
}
