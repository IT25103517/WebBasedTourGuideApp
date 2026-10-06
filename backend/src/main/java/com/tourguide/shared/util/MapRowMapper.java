package com.tourguide.shared.util;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A RowMapper that behaves like Spring's ColumnMapRowMapper (keys = the SQL
 * column/alias names, exactly as returned by the driver - matching how the
 * Node mssql driver produced its recordset objects) but normalises JDBC's
 * legacy date/time types to java.time so the API never leaks a
 * java.sql.Date/Time/Timestamp (and its timezone quirks) into a JSON response.
 */
public final class MapRowMapper implements RowMapper<Map<String, Object>> {

    public static final MapRowMapper INSTANCE = new MapRowMapper();

    @Override
    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        Map<String, Object> row = new LinkedHashMap<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            String key = JdbcUtils.lookupColumnName(meta, i);
            row.put(key, normalize(rs.getObject(i)));
        }
        return row;
    }

    private Object normalize(Object value) {
        if (value instanceof java.sql.Date d) {
            return d.toLocalDate();
        }
        if (value instanceof Time t) {
            return t.toLocalTime();
        }
        if (value instanceof Timestamp ts) {
            return ts.toLocalDateTime();
        }
        return value;
    }

    /** Formats a TIME value the same way the Node code's toHHMM() helper did. */
    public static String toHHMM(Object value) {
        if (value == null) return null;
        if (value instanceof LocalTime lt) {
            return String.format("%02d:%02d", lt.getHour(), lt.getMinute());
        }
        if (value instanceof LocalDateTime dt) {
            return String.format("%02d:%02d", dt.getHour(), dt.getMinute());
        }
        String s = String.valueOf(value);
        return s.length() >= 5 ? s.substring(0, 5) : s;
    }

    private static final java.time.format.DateTimeFormatter ISO_DATE = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

    /** Formats a DATE value as yyyy-MM-dd regardless of whether it arrived as LocalDate/LocalDateTime/String. */
    public static String toISODate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate d) return d.format(ISO_DATE);
        if (value instanceof LocalDateTime dt) return dt.toLocalDate().format(ISO_DATE);
        return String.valueOf(value).substring(0, 10);
    }
}
