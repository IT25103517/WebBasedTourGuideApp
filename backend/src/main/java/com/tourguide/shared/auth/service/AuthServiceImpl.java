package com.tourguide.shared.auth.service;

import com.tourguide.shared.auth.LoginRequest;
import com.tourguide.shared.auth.RegisterRequest;
import com.tourguide.shared.auth.model.AuthResult;
import com.tourguide.shared.auth.model.PublicUserView;
import com.tourguide.shared.auth.repository.UserCredential;
import com.tourguide.shared.auth.repository.UserRepository;
import com.tourguide.shared.error.ApiException;
import com.tourguide.shared.model.User;
import com.tourguide.shared.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Port of the original AuthService - registration, login and /me, now backed by {@link UserRepository}. */
@Service
public class AuthServiceImpl implements AuthService {

    // cost factor 10, matching bcrypt.hash(password, 10) in Node and the seed data hashes
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(10);

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    private String signToken(User user) {
        return jwtService.sign(user.getUserId(), user.getRole(), user.getFullName());
    }

    @Override
    @Transactional
    public AuthResult register(RegisterRequest data) {
        if (userRepository.existsByEmail(data.email())) {
            throw ApiException.conflict("That email address is already registered");
        }

        String hash = ENCODER.encode(data.password());
        User user = userRepository.insertUser(data.full_name(), data.email(), hash, data.phone(), data.role());
        int userId = user.getUserId();

        switch (data.role()) {
            case "TOURIST" -> userRepository.insertTouristProfile(userId, data.nationality(), data.preferred_language());
            case "GUIDE" -> userRepository.insertGuideProfile(userId,
                    data.experience_years() == null ? 0 : data.experience_years(),
                    data.languages(), data.base_location());
            default -> userRepository.insertAdminProfile(userId);
        }

        return new AuthResult(PublicUserView.of(user), signToken(user));
    }

    @Override
    public AuthResult login(LoginRequest data) {
        UserCredential credential = userRepository.findCredentialByActiveEmail(data.email())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));

        if (!ENCODER.matches(data.password(), credential.passwordHash())) {
            throw ApiException.unauthorized("Invalid email or password");
        }

        User user = credential.user();
        return new AuthResult(PublicUserView.of(user), signToken(user));
    }

    @Override
    public User me(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
    }
}
