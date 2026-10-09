package com.tourguide.availability.repository;

import com.tourguide.availability.dto.CreateAvailabilityRequest;
import com.tourguide.availability.dto.UpdateAvailabilityRequest;
import com.tourguide.availability.model.AvailabilityWindow;
import com.tourguide.shared.util.Db;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** JDBC implementation of {@link AvailabilityRepository}, holding the raw SQL ported from the original AvailabilityService. */
@Repository
public class JdbcAvailabilityRepository implements AvailabilityRepository {

    /** Common columns only - findWindowsOverlapping's query doesn't select guide_id, so that field is set by callers that do. */
    private static final RowMapper<AvailabilityWindow> ROW_MAPPER = (rs, rowNum) -> {
        AvailabilityWindow w = new AvailabilityWindow();
        w.setAvailabilityId(rs.getObject("availability_id", Integer.class));
        w.setPackageId(rs.getObject("package_id", Integer.class));
        w.setStartDate(rs.getObject("start_date", LocalDate.class));
        w.setEndDate(rs.getObject("end_date", LocalDate.class));
        w.setMaxGroupSize(rs.getObject("max_group_size", Integer.class));
        w.setPriceOverride(rs.getBigDecimal("price_override"));
        w.setStatus(rs.getString("status"));
        w.setNote(rs.getString("note"));
        return w;
    };

    private final Db db;

    public JdbcAvailabilityRepository(Db db) {
        this.db = db;
    }

    @Override
    public List<AvailabilityWindow> findWindowsByGuide(int guideId) {
        return db.template().query(
                """
                SELECT a.*, p.title AS package_title
                  FROM dbo.Availability a
                  LEFT JOIN dbo.TourPackages p ON p.package_id = a.package_id
                 WHERE a.guide_id = :guideId
                 ORDER BY a.start_date
                """,
                new MapSqlParameterSource("guideId", guideId),
                (rs, rowNum) -> {
                    AvailabilityWindow w = ROW_MAPPER.mapRow(rs, rowNum);
                    w.setGuideId(rs.getObject("guide_id", Integer.class));
                    w.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
                    w.setPackageTitle(rs.getString("package_title"));
                    return w;
                });
    }

    @Override
    public List<AvailabilityWindow> findWindowsOverlapping(int guideId, LocalDate from, LocalDate to) {
        // Explicit ORDER BY (the Node version had none, making "last window wins"
        // arbitrary) - availability_id ASC makes later-created windows deterministically win.
        return db.template().query(
                """
                SELECT availability_id, package_id, start_date, end_date, max_group_size,
                       price_override, status, note
                  FROM dbo.Availability
                 WHERE guide_id = :guideId AND start_date <= :to AND end_date >= :from
                 ORDER BY availability_id ASC
                """,
                new MapSqlParameterSource().addValue("guideId", guideId).addValue("from", from).addValue("to", to),
                ROW_MAPPER);
    }

    @Override
    public List<LocalDate> findBookedDatesInRange(int guideId, LocalDate from, LocalDate to) {
        return db.template().query(
                """
                SELECT tour_date FROM dbo.Bookings
                 WHERE guide_id = :guideId AND is_deleted = 0
                   AND status IN ('PENDING','CONFIRMED')
                   AND tour_date BETWEEN :from AND :to
                """,
                new MapSqlParameterSource().addValue("guideId", guideId).addValue("from", from).addValue("to", to),
                (rs, rowNum) -> rs.getObject("tour_date", LocalDate.class));
    }

    @Override
    public Map<LocalDate, Integer> findBookingIdsByDate(int guideId, LocalDate from, LocalDate to) {
        List<Map.Entry<LocalDate, Integer>> rows = db.template().query(
                """
                SELECT booking_id, tour_date FROM dbo.Bookings
                 WHERE guide_id = :guideId AND is_deleted = 0
                   AND status IN ('PENDING','CONFIRMED')
                   AND tour_date BETWEEN :from AND :to
                """,
                new MapSqlParameterSource().addValue("guideId", guideId).addValue("from", from).addValue("to", to),
                (rs, rowNum) -> Map.entry(
                        rs.getObject("tour_date", LocalDate.class),
                        rs.getObject("booking_id", Integer.class)));
        Map<LocalDate, Integer> result = new LinkedHashMap<>();
        rows.forEach(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }

    private long countWhere(String statusClause, int guideId, LocalDate date) {
        Integer count = db.template().queryForObject(
                "SELECT COUNT(*) FROM dbo.Availability WHERE guide_id = :guideId AND status = " + statusClause +
                        " AND :date BETWEEN start_date AND end_date",
                new MapSqlParameterSource().addValue("guideId", guideId).addValue("date", date), Integer.class);
        return count == null ? 0 : count;
    }

    @Override
    public long countOpenWindows(int guideId, LocalDate date) {
        return countWhere("'AVAILABLE'", guideId, date);
    }

    @Override
    public long countBlockedWindows(int guideId, LocalDate date) {
        return countWhere("'BLOCKED'", guideId, date);
    }

    @Override
    public long countClashingBookings(int guideId, LocalDate date) {
        Integer count = db.template().queryForObject(
                "SELECT COUNT(*) FROM dbo.Bookings WHERE guide_id = :guideId AND tour_date = :date " +
                        "AND status IN ('PENDING','CONFIRMED') AND is_deleted = 0",
                new MapSqlParameterSource().addValue("guideId", guideId).addValue("date", date), Integer.class);
        return count == null ? 0 : count;
    }

    @Override
    public Optional<Integer> findPackageGuideId(int packageId) {
        return db.template().query(
                        "SELECT guide_id FROM dbo.TourPackages WHERE package_id = :id",
                        new MapSqlParameterSource("id", packageId),
                        (rs, rowNum) -> rs.getObject("guide_id", Integer.class))
                .stream().findFirst();
    }

    @Override
    public Optional<AvailabilityWindow> findById(Integer availabilityId) {
        return db.template().query(
                        "SELECT * FROM dbo.Availability WHERE availability_id = :id",
                        new MapSqlParameterSource("id", availabilityId),
                        (rs, rowNum) -> {
                            AvailabilityWindow w = ROW_MAPPER.mapRow(rs, rowNum);
                            w.setGuideId(rs.getObject("guide_id", Integer.class));
                            w.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
                            return w;
                        })
                .stream().findFirst();
    }

    @Override
    public AvailabilityWindow create(int guideId, CreateAvailabilityRequest data) {
        String status = data.status() == null ? "AVAILABLE" : data.status();
        int id = db.template().queryForObject(
                """
                INSERT INTO dbo.Availability
                  (guide_id, package_id, start_date, end_date, max_group_size, price_override, status, note)
                OUTPUT INSERTED.availability_id
                VALUES (:guide_id, :package_id, :start_date, :end_date, :max_group_size, :price_override, :status, :note)
                """,
                new MapSqlParameterSource()
                        .addValue("guide_id", guideId)
                        .addValue("package_id", data.package_id())
                        .addValue("start_date", data.start_date())
                        .addValue("end_date", data.end_date())
                        .addValue("max_group_size", data.max_group_size() == null ? 10 : data.max_group_size())
                        .addValue("price_override", data.price_override())
                        .addValue("status", status)
                        .addValue("note", data.note()),
                Integer.class);
        return findById(id).orElseThrow();
    }

    @Override
    public AvailabilityWindow update(int availabilityId, UpdateAvailabilityRequest data) {
        List<String> sets = new ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource("id", availabilityId);
        if (data.package_id() != null) { sets.add("package_id = :package_id"); params.addValue("package_id", data.package_id()); }
        if (data.start_date() != null) { sets.add("start_date = :start_date"); params.addValue("start_date", data.start_date()); }
        if (data.end_date() != null) { sets.add("end_date = :end_date"); params.addValue("end_date", data.end_date()); }
        if (data.max_group_size() != null) { sets.add("max_group_size = :max_group_size"); params.addValue("max_group_size", data.max_group_size()); }
        if (data.price_override() != null) { sets.add("price_override = :price_override"); params.addValue("price_override", data.price_override()); }
        if (data.status() != null) { sets.add("status = :status"); params.addValue("status", data.status()); }
        if (data.note() != null) { sets.add("note = :note"); params.addValue("note", data.note()); }

        db.update("UPDATE dbo.Availability SET " + String.join(", ", sets) + " WHERE availability_id = :id", params);
        return findById(availabilityId).orElseThrow();
    }

    @Override
    public void delete(int availabilityId) {
        db.update("DELETE FROM dbo.Availability WHERE availability_id = :id", new MapSqlParameterSource("id", availabilityId));
    }
}
