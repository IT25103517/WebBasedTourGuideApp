package com.tourguide.shared.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects the authenticated CurrentUserDetails into a controller method
 * parameter. By default the request must carry a valid Bearer token
 * (equivalent to the Node `authenticate` middleware) - use
 * {@code @CurrentUser(required = false)} for a public/optional-auth route
 * (equivalent to `optionalAuth`), which resolves to null instead of
 * throwing when no valid token is present.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
    boolean required() default true;
}
