package com.tourguide.shared.model;

/**
 * A plain {"message": "..."} result, used by the several delete/remove endpoints that only echo a message.
 * A small class instead of a raw Map: the "message" field is named and typed at compile time,
 * so a typo or a wrong type is caught by the compiler instead of surfacing as a runtime JSON bug.
 */
public class MessageResult {

    private final String message;

    public MessageResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
