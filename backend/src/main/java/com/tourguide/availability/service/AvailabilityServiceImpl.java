package com.tourguide.availability.service;

import com.tourguide.availability.dto.CreateAvailabilityRequest;
import com.tourguide.availability.dto.UpdateAvailabilityRequest;
import com.tourguide.availability.model.AvailabilityWindow;
import com.tourguide.availability.model.CalendarDay;
import com.tourguide.availability.model.CalendarResult;
import com.tourguide.availability.model.DateAvailability;
import com.tourguide.availability.repository.AvailabilityRepository;
import com.tourguide.shared.error.ApiException;
import com.tourguide.shared.model.MessageResult;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Port of the original AvailabilityService.
 *
 * A guide stores date WINDOWS (start_date..end_date) that are AVAILABLE or
 * BLOCKED. The calendar view expands those windows into single days and
 * overlays confirmed/pending bookings, because a date with a live booking
 * must stay blocked and can never be reported as available.
 */
@Service
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityRepository repository;

    public AvailabilityServiceImpl(AvailabilityRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<AvailabilityWindow> listWindows(int guideId) {
        return repository.findWindowsByGuide(guideId);
    }

    @Override
    public CalendarResult getCalendar(int guideId, LocalDate from, LocalDate to) {
        List<AvailabilityWindow> windows = repository.findWindowsOverlapping(guideId, from, to);
        Map<LocalDate, Integer> bookingIdsByDate = repository.findBookingIdsByDate(guideId, from, to);

        List<CalendarDay> days = new ArrayList<>();
        for (LocalDate cursor = from; !cursor.isAfter(to); cursor = cursor.plusDays(1)) {
            Integer bookingId = bookingIdsByDate.get(cursor);

            String state = "UNSET";
            AvailabilityWindow detail = null;
            for (AvailabilityWindow w : windows) {
                if (!cursor.isBefore(w.getStartDate()) && !cursor.isAfter(w.getEndDate())) {
                    state = w.getStatus();
                    detail = w;
                }
            }
            if (bookingId != null) state = "BOOKED";

            days.add(new CalendarDay(
                    cursor, state,
                    detail != null ? detail.getAvailabilityId() : null,
                    detail != null ? detail.getMaxGroupSize() : null,
                    detail != null ? detail.getPriceOverride() : null,
                    detail != null ? detail.getNote() : null,
                    bookingId));
        }
        return new CalendarResult(from, to, days);
    }

    @Override
    public DateAvailability isDateAvailable(int guideId, LocalDate date) {
        if (repository.countOpenWindows(guideId, date) == 0) {
            return new DateAvailability(false, "The guide has not opened this date");
        }
        if (repository.countBlockedWindows(guideId, date) > 0) {
            return new DateAvailability(false, "The guide blocked this date");
        }
        if (repository.countClashingBookings(guideId, date) > 0) {
            return new DateAvailability(false, "The guide already has a booking on this date");
        }
        return new DateAvailability(true, null);
    }

    private void assertNoConfirmedBookingInRange(int guideId, LocalDate start, LocalDate end, String action) {
        List<LocalDate> dates = repository.findBookedDatesInRange(guideId, start, end);
        if (!dates.isEmpty()) {
            List<String> iso = dates.stream().map(LocalDate::toString).toList();
            throw ApiException.conflict(
                    "Cannot " + action + ": " + dates.size() + " date(s) in this range already have a booking",
                    Map.of("booked_dates", iso));
        }
    }

    @Override
    public AvailabilityWindow create(int guideId, CreateAvailabilityRequest data) {
        if (data.end_date().isBefore(data.start_date())) {
            throw ApiException.badRequest("Validation failed", Map.of("end_date", "end_date cannot be before start_date"));
        }

        if (data.package_id() != null) {
            int owner = repository.findPackageGuideId(data.package_id())
                    .orElseThrow(() -> ApiException.notFound("Tour package not found"));
            if (owner != guideId) throw ApiException.forbidden("That package belongs to another guide");
        }

        String status = data.status() == null ? "AVAILABLE" : data.status();
        if ("BLOCKED".equals(status)) {
            assertNoConfirmedBookingInRange(guideId, data.start_date(), data.end_date(), "block these dates");
        }

        return repository.create(guideId, data);
    }

    private AvailabilityWindow assertOwnership(int availabilityId, int guideId) {
        AvailabilityWindow row = repository.findById(availabilityId)
                .orElseThrow(() -> ApiException.notFound("Availability entry not found"));
        if (!row.getGuideId().equals(guideId)) {
            throw ApiException.forbidden("That entry belongs to another guide");
        }
        return row;
    }

    @Override
    public AvailabilityWindow update(int availabilityId, int guideId, UpdateAvailabilityRequest data) {
        AvailabilityWindow current = assertOwnership(availabilityId, guideId);

        LocalDate start = data.start_date() != null ? data.start_date() : current.getStartDate();
        LocalDate end = data.end_date() != null ? data.end_date() : current.getEndDate();
        String status = data.status() != null ? data.status() : current.getStatus();
        if (end.isBefore(start)) throw ApiException.badRequest("end_date cannot be before start_date");
        if ("BLOCKED".equals(status)) {
            assertNoConfirmedBookingInRange(guideId, start, end, "block these dates");
        }

        boolean anyField = data.package_id() != null || data.start_date() != null || data.end_date() != null
                || data.max_group_size() != null || data.price_override() != null || data.status() != null
                || data.note() != null;
        if (!anyField) throw ApiException.badRequest("No fields supplied to update");

        return repository.update(availabilityId, data);
    }

    @Override
    public MessageResult remove(int availabilityId, int guideId) {
        AvailabilityWindow current = assertOwnership(availabilityId, guideId);

        if ("AVAILABLE".equals(current.getStatus())) {
            assertNoConfirmedBookingInRange(guideId, current.getStartDate(), current.getEndDate(),
                    "remove this availability window");
        }
        repository.delete(availabilityId);
        return new MessageResult("Availability entry removed");
    }
}
