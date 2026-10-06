package com.tourguide.shared.auth.repository;

import com.tourguide.shared.model.User;

import java.util.Optional;

/**
 * Data access for dbo.Users and its ISA tables (Tourists / TourGuides / Administrators).
 * Abstraction: {@link com.tourguide.shared.auth.service.AuthServiceImpl} depends on this
 * interface only, never on SQL or JDBC directly - Spring injects the concrete
 * {@link JdbcUserRepository} through constructor injection.
 */
public interface UserRepository {

    boolean existsByEmail(String email);

    /** Inserts the base Users row and returns it as the right User subtype (role-only, no profile columns yet). */
    User insertUser(String fullName, String email, String passwordHash, String phone, String role);

    void insertTouristProfile(int userId, String nationality, String preferredLanguage);

    void insertGuideProfile(int userId, int experienceYears, String languages, String baseLocation);

    void insertAdminProfile(int userId);

    /** The row needed to verify a login attempt: only active accounts are considered. */
    Optional<UserCredential> findCredentialByActiveEmail(String email);

    /** Full profile (base columns + whichever subtype's columns apply), used by /me. */
    Optional<User> findById(Integer userId);
}
