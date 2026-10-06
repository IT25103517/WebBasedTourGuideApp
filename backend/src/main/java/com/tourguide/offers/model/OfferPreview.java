package com.tourguide.offers.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/** What an offer does to a price, used by the booking form (GET /api/offers/{id}/preview). */
public class OfferPreview {

    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal total;

    @JsonProperty("offer_title")
    private final String offerTitle;

    public OfferPreview(BigDecimal subtotal, BigDecimal discount, BigDecimal total, String offerTitle) {
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;
        this.offerTitle = offerTitle;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getOfferTitle() {
        return offerTitle;
    }
}
