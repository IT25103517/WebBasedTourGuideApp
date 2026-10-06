package com.tourguide.offers.repository;

import com.tourguide.offers.dto.CreateOfferRequest;
import com.tourguide.offers.dto.UpdateOfferRequest;
import com.tourguide.offers.model.ExpiredOffersResult;
import com.tourguide.offers.model.OfferPackageLink;
import com.tourguide.offers.model.OfferPreview;
import com.tourguide.offers.model.SpecialOffer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Abstraction over special-offer data access: {@link com.tourguide.offers.service.OffersServiceImpl}
 * depends on this interface only, never on SQL or JDBC directly - Spring injects the
 * concrete {@link JdbcOffersRepository} through constructor injection.
 */
public interface OffersRepository {

    List<SpecialOffer> findActive(Integer packageId, Integer guideId);

    List<SpecialOffer> findByGuide(int guideId);

    Optional<SpecialOffer> findById(Integer offerId);

    List<OfferPackageLink> findPackageLinks(List<Integer> offerIds);

    Optional<OfferOwnership> findOwnership(int offerId);

    /** package_id -> guide_id, for every package in the given id list. */
    Map<Integer, Integer> findPackageOwners(List<Integer> packageIds);

    int create(int guideId, CreateOfferRequest data, List<Integer> packageIds);

    void update(int offerId, UpdateOfferRequest data, List<Integer> packageIds);

    long countBookingsUsingOffer(int offerId);

    void markExpired(int offerId);

    void delete(int offerId);

    ExpiredOffersResult expireOutdated(int guideId);

    Optional<OfferPreview> preview(int offerId, int packageId, int groupSize);
}
