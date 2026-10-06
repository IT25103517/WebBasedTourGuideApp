package com.tourguide.shared.error;

/** Port of utils/ApiError.js - a small error class so services/controllers can throw HTTP errors. */
public class ApiException extends RuntimeException {

    private final int status;
    private final Object details;

    public ApiException(int status, String message, Object details) {
        super(message);
        this.status = status;
        this.details = details;
    }

    public ApiException(int status, String message) {
        this(status, message, null);
    }

    public int getStatus() {
        return status;
    }

    public Object getDetails() {
        return details;
    }

    public static ApiException badRequest(String msg) {
        return new ApiException(400, msg);
    }

    public static ApiException badRequest(String msg, Object details) {
        return new ApiException(400, msg, details);
    }

    public static ApiException unauthorized() {
        return new ApiException(401, "Authentication required");
    }

    public static ApiException unauthorized(String msg) {
        return new ApiException(401, msg);
    }

    public static ApiException forbidden() {
        return new ApiException(403, "You are not allowed to do this");
    }

    public static ApiException forbidden(String msg) {
        return new ApiException(403, msg);
    }

    public static ApiException forbidden(String msg, Object details) {
        return new ApiException(403, msg, details);
    }

    public static ApiException notFound() {
        return new ApiException(404, "Not found");
    }

    public static ApiException notFound(String msg) {
        return new ApiException(404, msg);
    }

    public static ApiException conflict(String msg) {
        return new ApiException(409, msg);
    }

    public static ApiException conflict(String msg, Object details) {
        return new ApiException(409, msg, details);
    }
}
