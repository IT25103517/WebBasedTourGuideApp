package com.tourguide.availability.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One day of a guide's calendar: AVAILABLE | BLOCKED | BOOKED | UNSET, plus whichever window/booking produced it. */
public class CalendarDay {

    private LocalDate date;

    private String state;

    @JsonProperty("availability_id")
    private Integer availabilityId;

    @JsonProperty("max_group_size")
    private Integer maxGroupSize;

    @JsonProperty("price_override")
    private BigDecimal priceOverride;

    private String note;

    @JsonProperty("booking_id")
    private Integer bookingId;

    public CalendarDay() {
    }

    public CalendarDay(LocalDate date, String state, Integer availabilityId, Integer maxGroupSize,
                        BigDecimal priceOverride, String note, Integer bookingId) {
        this.date = date;
        this.state = state;
        this.availabilityId = availabilityId;
        this.maxGroupSize = maxGroupSize;
        this.priceOverride = priceOverride;
        this.note = note;
        this.bookingId = bookingId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getAvailabilityId() {
        return availabilityId;
    }

    public void setAvailabilityId(Integer availabilityId) {
        this.availabilityId = availabilityId;
    }

    public Integer getMaxGroupSize() {
        return maxGroupSize;
    }

    public void setMaxGroupSize(Integer maxGroupSize) {
        this.maxGroupSize = maxGroupSize;
    }

    public BigDecimal getPriceOverride() {
        return priceOverride;
    }

    public void setPriceOverride(BigDecimal priceOverride) {
        this.priceOverride = priceOverride;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }
}
