package com.tourguide.shared.model;

/**
 * The {"deleted": bool, "message": "..."} shape returned by the several
 * soft-delete-or-hard-delete endpoints (packages, offers, bookings).
 * A small class instead of a raw Map: both fields are named and typed at compile time
 * (a boolean can't silently become a string), so every call site is self-documenting.
 */
public class DeletionResult {

    private final boolean deleted;
    private final String message;

    public DeletionResult(boolean deleted, String message) {
        this.deleted = deleted;
        this.message = message;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getMessage() {
        return message;
    }
}
