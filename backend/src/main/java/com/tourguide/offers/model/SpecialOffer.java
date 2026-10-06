package com.tourguide.offers.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A row from dbo.SpecialOffers, joined with the guide's name and the packages it applies to.
 * AGGREGATION: an offer holds a list of {@link OfferPackageLink}s, but it does not own those
 * TourPackages - they exist independently and keep existing if the offer is deleted.
 */
public class SpecialOffer {

    @JsonProperty("offer_id")
    private Integer offerId;

    @JsonProperty("guide_id")
    private Integer guideId;

    private String title;

    private String description;

    @JsonProperty("discount_type")
    private String discountType;

    @JsonProperty("discount_value")
    private BigDecimal discountValue;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("guide_name")
    private String guideName;

    /** 1 if ACTIVE and today falls within [start_date, end_date], else 0 - matches the original SQL CASE expression. */
    @JsonProperty("is_live")
    private Integer isLive;

    private List<OfferPackageLink> packages;

    public SpecialOffer() {
    }

    public Integer getOfferId() {
        return offerId;
    }

    public void setOfferId(Integer offerId) {
        this.offerId = offerId;
    }

    public Integer getGuideId() {
        return guideId;
    }

    public void setGuideId(Integer guideId) {
        this.guideId = guideId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getGuideName() {
        return guideName;
    }

    public void setGuideName(String guideName) {
        this.guideName = guideName;
    }

    public Integer getIsLive() {
        return isLive;
    }

    public void setIsLive(Integer isLive) {
        this.isLive = isLive;
    }

    public List<OfferPackageLink> getPackages() {
        return packages;
    }

    public void setPackages(List<OfferPackageLink> packages) {
        this.packages = packages;
    }
}
