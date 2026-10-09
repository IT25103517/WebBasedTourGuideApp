package com.tourguide.availability.service;

import com.tourguide.availability.dto.CreateAvailabilityRequest;
import com.tourguide.availability.dto.UpdateAvailabilityRequest;
import com.tourguide.availability.model.AvailabilityWindow;
import com.tourguide.availability.model.CalendarResult;
import com.tourguide.availability.model.DateAvailability;
import com.tourguide.shared.model.MessageResult;

import java.time.LocalDate;
import java.util.List;

/** Abstraction: AvailabilityController depends on this interface, not on the AvailabilityServiceImpl class directly - Spring injects the implementation (constructor injection). */
public interface AvailabilityService {

    List<AvailabilityWindow> listWindows(int guideId);

    CalendarResult getCalendar(int guideId, LocalDate from, LocalDate to);

    DateAvailability isDateAvailable(int guideId, LocalDate date);

    AvailabilityWindow create(int guideId, CreateAvailabilityRequest data);

    AvailabilityWindow update(int availabilityId, int guideId, UpdateAvailabilityRequest data);

    MessageResult remove(int availabilityId, int guideId);
}
