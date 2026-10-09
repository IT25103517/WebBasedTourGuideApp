package com.tourguide.availability.model;

import java.time.LocalDate;
import java.util.List;

/** The day-by-day calendar returned by GET /api/availability/.../calendar. */
public class CalendarResult {

    private final LocalDate from;
    private final LocalDate to;
    private final List<CalendarDay> days;

    public CalendarResult(LocalDate from, LocalDate to, List<CalendarDay> days) {
        this.from = from;
        this.to = to;
        this.days = days;
    }

    public LocalDate getFrom() {
        return from;
    }

    public LocalDate getTo() {
        return to;
    }

    public List<CalendarDay> getDays() {
        return days;
    }
}
