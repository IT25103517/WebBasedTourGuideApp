package com.tourguide.itineraries.repository;

/** Just enough of a TourPackages row to check ownership and day-number bounds. */
public record PackageOwnership(int guideId, int durationDays, String title) {
}
