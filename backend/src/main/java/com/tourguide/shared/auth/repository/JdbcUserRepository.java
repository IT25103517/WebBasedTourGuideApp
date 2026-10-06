package com.tourguide.shared.auth.repository;

import com.tourguide.shared.model.Administrator;
import com.tourguide.shared.model.Tourist;
import com.tourguide.shared.model.TourGuide;
import com.tourguide.shared.model.User;
import com.tourguide.shared.util.Db;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

/** JDBC implementation of {@link UserRepository}, holding the raw SQL that AuthService used to run directly. */
@Repository
public class JdbcUserRepository implements UserRepository {

    private final Db db;

    public JdbcUserRepository(Db db) {
        this.db = db;
    }

    /** Builds the right User subtype from just the base dbo.Users columns (no profile columns present). */
    private static User mapBaseUser(ResultSet rs) throws SQLException {
        Integer userId = rs.getObject("user_id", Integer.class);
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String role = rs.getString("role");
        Boolean isActive = rs.getObject("is_active", Boolean.class);
        LocalDateTime dateCreated = rs.getObject("date_created", LocalDateTime.class);

        return switch (role) {
            case "TOURIST" -> new Tourist(userId, fullName, email, phone, role, isActive, dateCreated, null, null);
            case "GUIDE" -> new TourGuide(userId, fullName, email, phone, role, isActive, dateCreated,
                    null, null, null, null, null);
            default -> new Administrator(userId, fullName, email, phone, role, isActive, dateCreated);
        };
    }

    /** Builds the right subtype from the /me join (base columns + both profile tables, left joined). */
    private static final RowMapper<User> FULL_PROFILE_MAPPER = (rs, rowNum) -> {
        Integer userId = rs.getObject("user_id", Integer.class);
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String role = rs.getString("role");
        Boolean isActive = rs.getObject("is_active", Boolean.class);
        LocalDateTime dateCreated = rs.getObject("date_created", LocalDateTime.class);

        return switch (role) {
            case "TOURIST" -> new Tourist(userId, fullName, email, phone, role, isActive, dateCreated,
                    rs.getString("nationality"), rs.getString("preferred_language"));
            // note: the /me join does not select g.bio (the original AuthService query never did either),
            // so that field stays null here - it's populated by whichever future query actually needs it.
            case "GUIDE" -> new TourGuide(userId, fullName, email, phone, role, isActive, dateCreated,
                    rs.getObject("experience_years", Integer.class), rs.getString("verification_status"),
                    null, rs.getString("languages"), rs.getString("base_location"));
            default -> new Administrator(userId, fullName, email, phone, role, isActive, dateCreated);
        };
    };

    @Override
    public boolean existsByEmail(String email) {
        return db.queryOne("SELECT user_id FROM dbo.Users WHERE email = :email",
                new MapSqlParameterSource("email", email)) != null;
    }

    @Override
    public User insertUser(String fullName, String email, String passwordHash, String phone, String role) {
        return db.template().queryForObject(
                """
                INSERT INTO dbo.Users (full_name, email, password_hash, phone, role)
                OUTPUT INSERTED.*
                VALUES (:full_name, :email, :password_hash, :phone, :role)
                """,
                new MapSqlParameterSource()
                        .addValue("full_name", fullName)
                        .addValue("email", email)
                        .addValue("password_hash", passwordHash)
                        .addValue("phone", phone)
                        .addValue("role", role),
                (rs, rowNum) -> mapBaseUser(rs));
    }

    @Override
    public void insertTouristProfile(int userId, String nationality, String preferredLanguage) {
        db.update(
                """
                INSERT INTO dbo.Tourists (tourist_id, nationality, preferred_language)
                VALUES (:id, :nationality, :preferred_language)
                """,
                new MapSqlParameterSource()
                        .addValue("id", userId)
                        .addValue("nationality", nationality)
                        .addValue("preferred_language", preferredLanguage));
    }

    @Override
    public void insertGuideProfile(int userId, int experienceYears, String languages, String baseLocation) {
        db.update(
                """
                INSERT INTO dbo.TourGuides (guide_id, experience_years, languages, base_location)
                VALUES (:id, :experience_years, :languages, :base_location)
                """,
                new MapSqlParameterSource()
                        .addValue("id", userId)
                        .addValue("experience_years", experienceYears)
                        .addValue("languages", languages)
                        .addValue("base_location", baseLocation));
    }

    @Override
    public void insertAdminProfile(int userId) {
        db.update("INSERT INTO dbo.Administrators (admin_id) VALUES (:id)",
                new MapSqlParameterSource("id", userId));
    }

    @Override
    public Optional<UserCredential> findCredentialByActiveEmail(String email) {
        return db.template().query(
                        "SELECT * FROM dbo.Users WHERE email = :email AND is_active = 1",
                        new MapSqlParameterSource("email", email),
                        (rs, rowNum) -> new UserCredential(mapBaseUser(rs), rs.getString("password_hash")))
                .stream().findFirst();
    }

    @Override
    public Optional<User> findById(Integer userId) {
        return db.template().query(
                        """
                        SELECT u.*, g.experience_years, g.verification_status, g.languages, g.base_location,
                               t.nationality, t.preferred_language
                        FROM dbo.Users u
                        LEFT JOIN dbo.TourGuides g ON g.guide_id  = u.user_id
                        LEFT JOIN dbo.Tourists   t ON t.tourist_id = u.user_id
                        WHERE u.user_id = :id
                        """,
                        new MapSqlParameterSource("id", userId), FULL_PROFILE_MAPPER)
                .stream().findFirst();
    }
}
