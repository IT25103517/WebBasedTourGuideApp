package com.tourguide.offers.service;

import com.tourguide.offers.dto.CreateOfferRequest;
import com.tourguide.offers.dto.UpdateOfferRequest;
import com.tourguide.offers.model.ExpiredOffersResult;
import com.tourguide.offers.model.OfferPreview;
import com.tourguide.offers.model.SpecialOffer;
import com.tourguide.shared.model.DeletionResult;

import java.util.List;

/** Abstraction: OffersController depends on this interface, not on the OffersServiceImpl class directly - Spring injects the implementation (constructor injection). */
public interface OffersService {

    List<SpecialOffer> listActive(Integer packageId, Integer guideId);

    List<SpecialOffer> listByGuide(int guideId);

    SpecialOffer getById(int offerId);

    SpecialOffer create(int guideId, CreateOfferRequest data, List<Integer> packageIds);

    SpecialOffer update(int offerId, int guideId, UpdateOfferRequest data, List<Integer> packageIds);

    DeletionResult remove(int offerId, int guideId);

    ExpiredOffersResult expireOutdated(int guideId);

    OfferPreview preview(int offerId, int packageId, int groupSize);
}
