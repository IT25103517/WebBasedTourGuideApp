package com.tourguide.shared.util;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Thin convenience wrapper around NamedParameterJdbcTemplate, the Java
 * equivalent of config/db.js's query() helper: run parameterised T-SQL and
 * get back List/Map of column-name -> value, exactly like the Node mssql
 * driver's recordset.
 */
@Component
public class Db {

    private final NamedParameterJdbcTemplate jdbc;

    public Db(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public NamedParameterJdbcTemplate template() {
        return jdbc;
    }

    public List<Map<String, Object>> query(String sql) {
        return jdbc.query(sql, MapRowMapper.INSTANCE);
    }

    public List<Map<String, Object>> query(String sql, Map<String, ?> params) {
        return jdbc.query(sql, params, MapRowMapper.INSTANCE);
    }

    public List<Map<String, Object>> query(String sql, MapSqlParameterSource params) {
        return jdbc.query(sql, params, MapRowMapper.INSTANCE);
    }

    /** First row, or null if the query returned no rows. */
    public Map<String, Object> queryOne(String sql, Map<String, ?> params) {
        List<Map<String, Object>> rows = query(sql, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public Map<String, Object> queryOne(String sql, MapSqlParameterSource params) {
        List<Map<String, Object>> rows = query(sql, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int update(String sql, Map<String, ?> params) {
        return jdbc.update(sql, params);
    }

    public int update(String sql, MapSqlParameterSource params) {
        return jdbc.update(sql, params);
    }
}
