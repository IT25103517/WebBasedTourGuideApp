package com.tourguide.availability.model;

/** Whether a guide is free on one given date, and why not if they aren't. */
public class DateAvailability {

    private final boolean available;
    private final String reason;

    public DateAvailability(boolean available, String reason) {
        this.available = available;
        this.reason = reason;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getReason() {
        return reason;
    }
}
