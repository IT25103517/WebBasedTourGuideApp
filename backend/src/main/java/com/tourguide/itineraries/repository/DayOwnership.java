package com.tourguide.itineraries.repository;

/** An itinerary day joined with its package's guide id, duration and id - enough for ownership + bounds checks. */
public record DayOwnership(int itineraryId, int packageId, int dayNumber, int guideId, int durationDays) {
}
