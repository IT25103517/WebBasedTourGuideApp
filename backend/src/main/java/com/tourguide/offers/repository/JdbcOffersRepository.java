package com.tourguide.offers.repository;

import com.tourguide.offers.dto.CreateOfferRequest;
import com.tourguide.offers.dto.UpdateOfferRequest;
import com.tourguide.offers.model.ExpiredOffersResult;
import com.tourguide.offers.model.OfferPackageLink;
import com.tourguide.offers.model.OfferPreview;
import com.tourguide.offers.model.SpecialOffer;
import com.tourguide.shared.util.Db;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** JDBC implementation of {@link OffersRepository}, holding the raw SQL ported from the original OffersService. */
@Repository
public class JdbcOffersRepository implements OffersRepository {

    private static final String OFFER_SELECT = """
            SELECT o.offer_id, o.guide_id, o.title, o.description, o.discount_type,
                   o.discount_value, o.start_date, o.end_date, o.status, o.created_at,
                   u.full_name AS guide_name,
                   CASE WHEN o.status = 'ACTIVE'
                             AND CAST(GETDATE() AS DATE) BETWEEN o.start_date AND o.end_date
                        THEN 1 ELSE 0 END AS is_live
            FROM dbo.SpecialOffers o
            JOIN dbo.Users u ON u.user_id = o.guide_id""";

    private static final RowMapper<SpecialOffer> OFFER_MAPPER = (rs, rowNum) -> {
        SpecialOffer o = new SpecialOffer();
        o.setOfferId(rs.getObject("offer_id", Integer.class));
        o.setGuideId(rs.getObject("guide_id", Integer.class));
        o.setTitle(rs.getString("title"));
        o.setDescription(rs.getString("description"));
        o.setDiscountType(rs.getString("discount_type"));
        o.setDiscountValue(rs.getBigDecimal("discount_value"));
        o.setStartDate(rs.getObject("start_date", LocalDate.class));
        o.setEndDate(rs.getObject("end_date", LocalDate.class));
        o.setStatus(rs.getString("status"));
        o.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        o.setGuideName(rs.getString("guide_name"));
        o.setIsLive(rs.getObject("is_live", Integer.class));
        return o;
    };

    private final Db db;

    public JdbcOffersRepository(Db db) {
        this.db = db;
    }

    private List<SpecialOffer> attachPackages(List<SpecialOffer> offers) {
        if (offers.isEmpty()) return offers;
        List<Integer> ids = offers.stream().map(SpecialOffer::getOfferId).toList();
        List<OfferPackageLink> links = findPackageLinks(ids);
        for (SpecialOffer o : offers) {
            List<OfferPackageLink> forOffer = links.stream()
                    .filter(l -> l.getOfferId().equals(o.getOfferId()))
                    .toList();
            o.setPackages(forOffer);
        }
        return offers;
    }

    @Override
    public List<OfferPackageLink> findPackageLinks(List<Integer> offerIds) {
        if (offerIds == null || offerIds.isEmpty()) return List.of();
        return db.template().query(
                """
                SELECT op.offer_id, p.package_id, p.title, p.base_price, p.destination
                  FROM dbo.OfferPackages op
                  JOIN dbo.TourPackages p ON p.package_id = op.package_id
                 WHERE op.offer_id IN (:ids)
                """,
                new MapSqlParameterSource("ids", offerIds),
                (rs, rowNum) -> new OfferPackageLink(
                        rs.getObject("offer_id", Integer.class),
                        rs.getObject("package_id", Integer.class),
                        rs.getString("title"),
                        rs.getBigDecimal("base_price"),
                        rs.getString("destination")));
    }

    @Override
    public List<SpecialOffer> findActive(Integer packageId, Integer guideId) {
        List<String> where = new ArrayList<>(List.of("o.status = 'ACTIVE'",
                "CAST(GETDATE() AS DATE) BETWEEN o.start_date AND o.end_date"));
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (guideId != null) { where.add("o.guide_id = :guideId"); params.addValue("guideId", guideId); }
        if (packageId != null) {
            where.add("EXISTS (SELECT 1 FROM dbo.OfferPackages op WHERE op.offer_id = o.offer_id AND op.package_id = :packageId)");
            params.addValue("packageId", packageId);
        }
        List<SpecialOffer> rows = db.template().query(
                OFFER_SELECT + " WHERE " + String.join(" AND ", where) + " ORDER BY o.end_date", params, OFFER_MAPPER);
        return attachPackages(rows);
    }

    @Override
    public List<SpecialOffer> findByGuide(int guideId) {
        List<SpecialOffer> rows = db.template().query(
                OFFER_SELECT + " WHERE o.guide_id = :guideId ORDER BY o.created_at DESC",
                new MapSqlParameterSource("guideId", guideId), OFFER_MAPPER);
        return attachPackages(rows);
    }

    @Override
    public Optional<SpecialOffer> findById(Integer offerId) {
        List<SpecialOffer> rows = db.template().query(OFFER_SELECT + " WHERE o.offer_id = :id",
                new MapSqlParameterSource("id", offerId), OFFER_MAPPER);
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(attachPackages(rows).get(0));
    }

    @Override
    public Optional<OfferOwnership> findOwnership(int offerId) {
        return db.template().query(
                        "SELECT * FROM dbo.SpecialOffers WHERE offer_id = :id",
                        new MapSqlParameterSource("id", offerId),
                        (rs, rowNum) -> new OfferOwnership(
                                rs.getInt("offer_id"), rs.getInt("guide_id"),
                                rs.getObject("start_date", LocalDate.class), rs.getObject("end_date", LocalDate.class),
                                rs.getString("discount_type"), rs.getBigDecimal("discount_value")))
                .stream().findFirst();
    }

    @Override
    public Map<Integer, Integer> findPackageOwners(List<Integer> packageIds) {
        if (packageIds == null || packageIds.isEmpty()) return Map.of();
        List<Map.Entry<Integer, Integer>> rows = db.template().query(
                "SELECT package_id, guide_id FROM dbo.TourPackages WHERE package_id IN (:ids)",
                new MapSqlParameterSource("ids", packageIds),
                (rs, rowNum) -> Map.entry(rs.getInt("package_id"), rs.getInt("guide_id")));
        Map<Integer, Integer> result = new LinkedHashMap<>();
        rows.forEach(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }

    @Override
    public int create(int guideId, CreateOfferRequest data, List<Integer> packageIds) {
        int offerId = db.template().queryForObject(
                """
                INSERT INTO dbo.SpecialOffers
                  (guide_id, title, description, discount_type, discount_value, start_date, end_date, status)
                OUTPUT INSERTED.offer_id
                VALUES (:guide_id, :title, :description, :discount_type, :discount_value, :start_date, :end_date, :status)
                """,
                new MapSqlParameterSource()
                        .addValue("guide_id", guideId)
                        .addValue("title", data.title())
                        .addValue("description", data.description())
                        .addValue("discount_type", data.discount_type())
                        .addValue("discount_value", data.discount_value())
                        .addValue("start_date", data.start_date())
                        .addValue("end_date", data.end_date())
                        .addValue("status", data.status() == null ? "ACTIVE" : data.status()),
                Integer.class);

        for (Integer packageId : packageIds) {
            db.update("INSERT INTO dbo.OfferPackages (offer_id, package_id) VALUES (:offer_id, :package_id)",
                    new MapSqlParameterSource().addValue("offer_id", offerId).addValue("package_id", packageId));
        }
        return offerId;
    }

    @Override
    public void update(int offerId, UpdateOfferRequest data, List<Integer> packageIds) {
        List<String> sets = new ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource("id", offerId);
        if (data.title() != null) { sets.add("title = :title"); params.addValue("title", data.title()); }
        if (data.description() != null) { sets.add("description = :description"); params.addValue("description", data.description()); }
        if (data.discount_type() != null) { sets.add("discount_type = :discount_type"); params.addValue("discount_type", data.discount_type()); }
        if (data.discount_value() != null) { sets.add("discount_value = :discount_value"); params.addValue("discount_value", data.discount_value()); }
        if (data.start_date() != null) { sets.add("start_date = :start_date"); params.addValue("start_date", data.start_date()); }
        if (data.end_date() != null) { sets.add("end_date = :end_date"); params.addValue("end_date", data.end_date()); }
        if (data.status() != null) { sets.add("status = :status"); params.addValue("status", data.status()); }

        if (!sets.isEmpty()) {
            db.update("UPDATE dbo.SpecialOffers SET " + String.join(", ", sets) + " WHERE offer_id = :id", params);
        }

        if (packageIds != null) {
            db.update("DELETE FROM dbo.OfferPackages WHERE offer_id = :id", new MapSqlParameterSource("id", offerId));
            for (Integer packageId : packageIds) {
                db.update("INSERT INTO dbo.OfferPackages (offer_id, package_id) VALUES (:offer_id, :package_id)",
                        new MapSqlParameterSource().addValue("offer_id", offerId).addValue("package_id", packageId));
            }
        }
    }

    @Override
    public long countBookingsUsingOffer(int offerId) {
        Integer n = db.template().queryForObject("SELECT COUNT(*) FROM dbo.Bookings WHERE offer_id = :id",
                new MapSqlParameterSource("id", offerId), Integer.class);
        return n == null ? 0 : n;
    }

    @Override
    public void markExpired(int offerId) {
        db.update("UPDATE dbo.SpecialOffers SET status = 'EXPIRED' WHERE offer_id = :id",
                new MapSqlParameterSource("id", offerId));
    }

    @Override
    public void delete(int offerId) {
        db.update("DELETE FROM dbo.SpecialOffers WHERE offer_id = :id", new MapSqlParameterSource("id", offerId));
    }

    @Override
    public ExpiredOffersResult expireOutdated(int guideId) {
        List<ExpiredOffersResult.ExpiredOffer> rows = db.template().query(
                """
                UPDATE dbo.SpecialOffers SET status = 'EXPIRED'
                OUTPUT INSERTED.offer_id, INSERTED.title
                 WHERE guide_id = :guideId AND status = 'ACTIVE' AND end_date < CAST(GETDATE() AS DATE)
                """,
                new MapSqlParameterSource("guideId", guideId),
                (rs, rowNum) -> new ExpiredOffersResult.ExpiredOffer(
                        rs.getObject("offer_id", Integer.class), rs.getString("title")));
        return new ExpiredOffersResult(rows.size(), rows);
    }

    @Override
    public Optional<OfferPreview> preview(int offerId, int packageId, int groupSize) {
        return db.template().query(
                        """
                        SELECT o.*, p.base_price
                          FROM dbo.SpecialOffers o
                          JOIN dbo.OfferPackages op ON op.offer_id = o.offer_id
                          JOIN dbo.TourPackages  p  ON p.package_id = op.package_id
                         WHERE o.offer_id = :offerId AND p.package_id = :packageId
                        """,
                        new MapSqlParameterSource().addValue("offerId", offerId).addValue("packageId", packageId),
                        (rs, rowNum) -> {
                            BigDecimal basePrice = rs.getBigDecimal("base_price");
                            BigDecimal subtotal = basePrice.multiply(BigDecimal.valueOf(groupSize <= 0 ? 1 : groupSize));
                            BigDecimal discountValue = rs.getBigDecimal("discount_value");
                            BigDecimal discount = "PERCENT".equals(rs.getString("discount_type"))
                                    ? subtotal.multiply(discountValue).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                                    : discountValue;
                            if (discount.compareTo(subtotal) > 0) discount = subtotal;
                            return new OfferPreview(
                                    subtotal.setScale(2, RoundingMode.HALF_UP),
                                    discount.setScale(2, RoundingMode.HALF_UP),
                                    subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP),
                                    rs.getString("title"));
                        })
                .stream().findFirst();
    }
}
