package com.tourguide.offers.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Result of "remove expired promotions": POST /api/offers/expire. */
public class ExpiredOffersResult {

    @JsonProperty("expired_count")
    private final int expiredCount;

    private final List<ExpiredOffer> offers;

    public ExpiredOffersResult(int expiredCount, List<ExpiredOffer> offers) {
        this.expiredCount = expiredCount;
        this.offers = offers;
    }

    public int getExpiredCount() {
        return expiredCount;
    }

    public List<ExpiredOffer> getOffers() {
        return offers;
    }

    public static class ExpiredOffer {
        @JsonProperty("offer_id")
        private final Integer offerId;
        private final String title;

        public ExpiredOffer(Integer offerId, String title) {
            this.offerId = offerId;
            this.title = title;
        }

        public Integer getOfferId() {
            return offerId;
        }

        public String getTitle() {
            return title;
        }
    }
}
