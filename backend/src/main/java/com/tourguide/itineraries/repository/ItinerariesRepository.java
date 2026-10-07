package com.tourguide.itineraries.repository;

import com.tourguide.itineraries.dto.ActivityRequest;
import com.tourguide.itineraries.dto.CreateDayRequest;
import com.tourguide.itineraries.dto.UpdateActivityRequest;
import com.tourguide.itineraries.dto.UpdateDayRequest;
import com.tourguide.itineraries.model.ItineraryActivity;
import com.tourguide.itineraries.model.ItineraryDay;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction over itinerary data access: {@link com.tourguide.itineraries.service.ItinerariesServiceImpl}
 * depends on this interface only, never on SQL or JDBC directly - Spring injects the
 * concrete {@link JdbcItinerariesRepository} through constructor injection.
 */
public interface ItinerariesRepository {

    List<ItineraryDay> findDaysByPackage(int packageId);

    List<ItineraryActivity> findActivitiesByPackage(int packageId);

    /** The day row alone, without activities attached. */
    Optional<ItineraryDay> findById(Integer itineraryId);

    List<ItineraryActivity> findActivitiesByDay(int itineraryId);

    Optional<PackageOwnership> findPackageOwnership(int packageId);

    Optional<DayOwnership> findDayOwnership(int itineraryId);

    /** guide_id of the package that owns this activity, via itinerary -> package. */
    Optional<Integer> findActivityOwnerGuideId(int activityId);

    Optional<Integer> findDuplicateDayId(int packageId, int dayNumber);

    ItineraryDay insertDay(int packageId, int dayNumber, String dayTitle, String description);

    ItineraryDay updateDay(int itineraryId, UpdateDayRequest data);

    void deleteDay(int itineraryId);

    ItineraryActivity insertActivity(int itineraryId, ActivityRequest data);

    ItineraryActivity updateActivity(int activityId, UpdateActivityRequest data);

    void deleteActivity(int activityId);

    /** Bulk-replace support: wipe every day (activities cascade) for a package. */
    void deleteAllDaysForPackage(int packageId);
}
