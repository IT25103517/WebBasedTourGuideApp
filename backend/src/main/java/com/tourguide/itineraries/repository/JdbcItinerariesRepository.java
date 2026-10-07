package com.tourguide.itineraries.repository;

import com.tourguide.itineraries.dto.ActivityRequest;
import com.tourguide.itineraries.dto.CreateDayRequest;
import com.tourguide.itineraries.dto.UpdateActivityRequest;
import com.tourguide.itineraries.dto.UpdateDayRequest;
import com.tourguide.itineraries.model.ItineraryActivity;
import com.tourguide.itineraries.model.ItineraryDay;
import com.tourguide.shared.util.Db;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC implementation of {@link ItinerariesRepository}, holding the raw SQL ported from the original ItinerariesService. */
@Repository
public class JdbcItinerariesRepository implements ItinerariesRepository {

    private static LocalTime parseTime(String hhmm) {
        return hhmm == null || hhmm.isBlank() ? null : LocalTime.parse(hhmm);
    }

    /** Formats a TIME value the same way the original toHHMM() helper did. */
    private static String toHHMM(Time t) {
        if (t == null) return null;
        LocalTime lt = t.toLocalTime();
        return String.format("%02d:%02d", lt.getHour(), lt.getMinute());
    }

    private static final RowMapper<ItineraryDay> DAY_MAPPER = (rs, rowNum) -> {
        ItineraryDay d = new ItineraryDay();
        d.setItineraryId(rs.getObject("itinerary_id", Integer.class));
        d.setPackageId(rs.getObject("package_id", Integer.class));
        d.setDayNumber(rs.getObject("day_number", Integer.class));
        d.setDayTitle(rs.getString("day_title"));
        d.setDescription(rs.getString("description"));
        d.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return d;
    };

    private static final RowMapper<ItineraryActivity> ACTIVITY_MAPPER = (rs, rowNum) -> new ItineraryActivity(
            rs.getObject("activity_id", Integer.class),
            rs.getObject("itinerary_id", Integer.class),
            rs.getString("activity_name"),
            rs.getString("location"),
            toHHMM(rs.getTime("start_time")),
            toHHMM(rs.getTime("end_time")),
            rs.getObject("sort_order", Integer.class));

    private final Db db;

    public JdbcItinerariesRepository(Db db) {
        this.db = db;
    }

    @Override
    public List<ItineraryDay> findDaysByPackage(int packageId) {
        return db.template().query(
                "SELECT itinerary_id, package_id, day_number, day_title, description, created_at " +
                        "FROM dbo.TourItineraries WHERE package_id = :id ORDER BY day_number",
                new MapSqlParameterSource("id", packageId), DAY_MAPPER);
    }

    @Override
    public List<ItineraryActivity> findActivitiesByPackage(int packageId) {
        return db.template().query(
                """
                SELECT a.*
                  FROM dbo.ItineraryActivities a
                  JOIN dbo.TourItineraries i ON i.itinerary_id = a.itinerary_id
                 WHERE i.package_id = :id
                 ORDER BY i.day_number, a.sort_order, a.start_time
                """,
                new MapSqlParameterSource("id", packageId), ACTIVITY_MAPPER);
    }

    @Override
    public Optional<ItineraryDay> findById(Integer itineraryId) {
        return db.template().query("SELECT * FROM dbo.TourItineraries WHERE itinerary_id = :id",
                        new MapSqlParameterSource("id", itineraryId), DAY_MAPPER)
                .stream().findFirst();
    }

    @Override
    public List<ItineraryActivity> findActivitiesByDay(int itineraryId) {
        return db.template().query(
                "SELECT * FROM dbo.ItineraryActivities WHERE itinerary_id = :id ORDER BY sort_order, start_time",
                new MapSqlParameterSource("id", itineraryId), ACTIVITY_MAPPER);
    }

    @Override
    public Optional<PackageOwnership> findPackageOwnership(int packageId) {
        return db.template().query(
                        "SELECT guide_id, duration_days, title FROM dbo.TourPackages WHERE package_id = :id",
                        new MapSqlParameterSource("id", packageId),
                        (rs, rowNum) -> new PackageOwnership(rs.getInt("guide_id"), rs.getInt("duration_days"), rs.getString("title")))
                .stream().findFirst();
    }

    @Override
    public Optional<DayOwnership> findDayOwnership(int itineraryId) {
        return db.template().query(
                        """
                        SELECT i.itinerary_id, i.package_id, i.day_number, p.guide_id, p.duration_days
                          FROM dbo.TourItineraries i
                          JOIN dbo.TourPackages p ON p.package_id = i.package_id
                         WHERE i.itinerary_id = :id
                        """,
                        new MapSqlParameterSource("id", itineraryId),
                        (rs, rowNum) -> new DayOwnership(rs.getInt("itinerary_id"), rs.getInt("package_id"),
                                rs.getInt("day_number"), rs.getInt("guide_id"), rs.getInt("duration_days")))
                .stream().findFirst();
    }

    @Override
    public Optional<Integer> findActivityOwnerGuideId(int activityId) {
        return db.template().query(
                        """
                        SELECT p.guide_id
                          FROM dbo.ItineraryActivities a
                          JOIN dbo.TourItineraries i ON i.itinerary_id = a.itinerary_id
                          JOIN dbo.TourPackages    p ON p.package_id   = i.package_id
                         WHERE a.activity_id = :id
                        """,
                        new MapSqlParameterSource("id", activityId),
                        (rs, rowNum) -> rs.getObject("guide_id", Integer.class))
                .stream().findFirst();
    }

    @Override
    public Optional<Integer> findDuplicateDayId(int packageId, int dayNumber) {
        return db.template().query(
                        "SELECT itinerary_id FROM dbo.TourItineraries WHERE package_id = :pid AND day_number = :day",
                        new MapSqlParameterSource().addValue("pid", packageId).addValue("day", dayNumber),
                        (rs, rowNum) -> rs.getObject("itinerary_id", Integer.class))
                .stream().findFirst();
    }

    @Override
    public ItineraryDay insertDay(int packageId, int dayNumber, String dayTitle, String description) {
        int id = db.template().queryForObject(
                """
                INSERT INTO dbo.TourItineraries (package_id, day_number, day_title, description)
                OUTPUT INSERTED.itinerary_id VALUES (:package_id, :day_number, :day_title, :description)
                """,
                new MapSqlParameterSource()
                        .addValue("package_id", packageId)
                        .addValue("day_number", dayNumber)
                        .addValue("day_title", dayTitle)
                        .addValue("description", description),
                Integer.class);
        return findById(id).orElseThrow();
    }

    @Override
    public ItineraryDay updateDay(int itineraryId, UpdateDayRequest data) {
        List<String> sets = new ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource("id", itineraryId);
        if (data.day_number() != null) { sets.add("day_number = :day_number"); params.addValue("day_number", data.day_number()); }
        if (data.day_title() != null) { sets.add("day_title = :day_title"); params.addValue("day_title", data.day_title()); }
        if (data.description() != null) { sets.add("description = :description"); params.addValue("description", data.description()); }

        db.update("UPDATE dbo.TourItineraries SET " + String.join(", ", sets) + " WHERE itinerary_id = :id", params);
        return findById(itineraryId).orElseThrow();
    }

    @Override
    public void deleteDay(int itineraryId) {
        db.update("DELETE FROM dbo.TourItineraries WHERE itinerary_id = :id", new MapSqlParameterSource("id", itineraryId));
    }

    @Override
    public ItineraryActivity insertActivity(int itineraryId, ActivityRequest data) {
        return db.template().queryForObject(
                """
                INSERT INTO dbo.ItineraryActivities
                  (itinerary_id, activity_name, location, start_time, end_time, sort_order)
                OUTPUT INSERTED.*
                VALUES (:itinerary_id, :activity_name, :location, :start_time, :end_time, :sort_order)
                """,
                new MapSqlParameterSource()
                        .addValue("itinerary_id", itineraryId)
                        .addValue("activity_name", data.activity_name())
                        .addValue("location", data.location())
                        .addValue("start_time", parseTime(data.start_time()))
                        .addValue("end_time", parseTime(data.end_time()))
                        .addValue("sort_order", data.sort_order() == null ? 1 : data.sort_order()),
                ACTIVITY_MAPPER);
    }

    @Override
    public ItineraryActivity updateActivity(int activityId, UpdateActivityRequest data) {
        List<String> sets = new ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource("id", activityId);
        if (data.activity_name() != null) { sets.add("activity_name = :activity_name"); params.addValue("activity_name", data.activity_name()); }
        if (data.location() != null) { sets.add("location = :location"); params.addValue("location", data.location()); }
        if (data.start_time() != null) { sets.add("start_time = :start_time"); params.addValue("start_time", parseTime(data.start_time())); }
        if (data.end_time() != null) { sets.add("end_time = :end_time"); params.addValue("end_time", parseTime(data.end_time())); }
        if (data.sort_order() != null) { sets.add("sort_order = :sort_order"); params.addValue("sort_order", data.sort_order()); }

        return db.template().queryForObject(
                "UPDATE dbo.ItineraryActivities SET " + String.join(", ", sets) + " OUTPUT INSERTED.* WHERE activity_id = :id",
                params, ACTIVITY_MAPPER);
    }

    @Override
    public void deleteActivity(int activityId) {
        db.update("DELETE FROM dbo.ItineraryActivities WHERE activity_id = :id", new MapSqlParameterSource("id", activityId));
    }

    @Override
    public void deleteAllDaysForPackage(int packageId) {
        db.update("DELETE FROM dbo.TourItineraries WHERE package_id = :pid", new MapSqlParameterSource("pid", packageId));
    }
}
