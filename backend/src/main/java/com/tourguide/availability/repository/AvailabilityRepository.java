package com.tourguide.availability.repository;

import com.tourguide.availability.dto.CreateAvailabilityRequest;
import com.tourguide.availability.dto.UpdateAvailabilityRequest;
import com.tourguide.availability.model.AvailabilityWindow;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Abstraction over availability data access: {@link com.tourguide.availability.service.AvailabilityServiceImpl}
 * depends on this interface only, never on SQL or JDBC directly - Spring injects the
 * concrete {@link JdbcAvailabilityRepository} through constructor injection.
 */
public interface AvailabilityRepository {

    List<AvailabilityWindow> findWindowsByGuide(int guideId);

    /** Windows overlapping [from, to], ordered by availability_id ASC so later-created windows win ties. */
    List<AvailabilityWindow> findWindowsOverlapping(int guideId, LocalDate from, LocalDate to);

    /** tour_date of every live (PENDING/CONFIRMED, not deleted) booking for this guide within [from, to]. */
    List<LocalDate> findBookedDatesInRange(int guideId, LocalDate from, LocalDate to);

    /** booking_id keyed by tour_date, for the live bookings within [from, to]. */
    java.util.Map<LocalDate, Integer> findBookingIdsByDate(int guideId, LocalDate from, LocalDate to);

    long countOpenWindows(int guideId, LocalDate date);

    long countBlockedWindows(int guideId, LocalDate date);

    long countClashingBookings(int guideId, LocalDate date);

    Optional<Integer> findPackageGuideId(int packageId);

    Optional<AvailabilityWindow> findById(Integer availabilityId);

    AvailabilityWindow create(int guideId, CreateAvailabilityRequest data);

    AvailabilityWindow update(int availabilityId, UpdateAvailabilityRequest data);

    void delete(int availabilityId);
}
