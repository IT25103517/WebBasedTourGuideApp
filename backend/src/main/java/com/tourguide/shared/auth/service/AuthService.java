package com.tourguide.shared.auth.service;

import com.tourguide.shared.auth.LoginRequest;
import com.tourguide.shared.auth.RegisterRequest;
import com.tourguide.shared.auth.model.AuthResult;
import com.tourguide.shared.model.User;

/**
 * Registration, login and profile lookup - shared by every role.
 * Abstraction: AuthController depends on this interface, not on the AuthServiceImpl class
 * directly - Spring injects the implementation (constructor injection).
 */
public interface AuthService {

    AuthResult register(RegisterRequest data);

    AuthResult login(LoginRequest data);

    User me(int userId);
}
