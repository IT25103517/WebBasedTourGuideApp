package com.tourguide.offers.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/** One dbo.OfferPackages row joined with the linked package's own basic columns. */
public class OfferPackageLink {

    @JsonProperty("offer_id")
    private Integer offerId;

    @JsonProperty("package_id")
    private Integer packageId;

    private String title;

    @JsonProperty("base_price")
    private BigDecimal basePrice;

    private String destination;

    public OfferPackageLink() {
    }

    public OfferPackageLink(Integer offerId, Integer packageId, String title, BigDecimal basePrice, String destination) {
        this.offerId = offerId;
        this.packageId = packageId;
        this.title = title;
        this.basePrice = basePrice;
        this.destination = destination;
    }

    public Integer getOfferId() {
        return offerId;
    }

    public void setOfferId(Integer offerId) {
        this.offerId = offerId;
    }

    public Integer getPackageId() {
        return packageId;
    }

    public void setPackageId(Integer packageId) {
        this.packageId = packageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
