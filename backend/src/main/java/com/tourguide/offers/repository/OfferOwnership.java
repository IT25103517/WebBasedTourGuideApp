package com.tourguide.offers.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Just enough of a SpecialOffers row for ownership and business-rule checks. */
public record OfferOwnership(int offerId, int guideId, LocalDate startDate, LocalDate endDate,
                              String discountType, BigDecimal discountValue) {
}
