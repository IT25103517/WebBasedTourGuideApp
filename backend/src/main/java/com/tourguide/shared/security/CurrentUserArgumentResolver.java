package com.tourguide.shared.security;

import com.tourguide.shared.error.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves @CurrentUser controller parameters from the attribute JwtAuthFilter
 * left on the request, reproducing authenticate/optionalAuth semantics.
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(CurrentUserDetails.class)
                && parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        CurrentUserDetails user = (CurrentUserDetails) request.getAttribute(JwtAuthFilter.USER_ATTR);
        CurrentUser annotation = parameter.getParameterAnnotation(CurrentUser.class);
        boolean required = annotation == null || annotation.required();

        if (user == null && required) {
            boolean tokenPresent = Boolean.TRUE.equals(request.getAttribute(JwtAuthFilter.TOKEN_PRESENT_ATTR));
            throw tokenPresent
                    ? ApiException.unauthorized("Invalid or expired token")
                    : ApiException.unauthorized("No token provided");
        }
        return user;
    }
}
