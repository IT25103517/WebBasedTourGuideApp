package com.tourguide.availability.controller;

import com.tourguide.availability.dto.CreateAvailabilityRequest;
import com.tourguide.availability.dto.UpdateAvailabilityRequest;

import com.tourguide.availability.model.AvailabilityWindow;
import com.tourguide.availability.model.DateAvailability;
import com.tourguide.availability.service.AvailabilityService;
import com.tourguide.shared.error.ApiException;
import com.tourguide.shared.model.MessageResult;
import com.tourguide.shared.security.CurrentUser;
import com.tourguide.shared.security.CurrentUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Port of modules/availability/availability.routes.js + availability.controller.js. */
@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final AvailabilityService service;

    public AvailabilityController(AvailabilityService service) {
        this.service = service;
    }

    /** Defaults the calendar range to the current month when none is given. */
    private LocalDate[] resolveRange(String from, String to, Integer year, Integer month) {
        if (from != null && to != null) {
            try {
                return new LocalDate[]{LocalDate.parse(from), LocalDate.parse(to)};
            } catch (Exception e) {
                throw ApiException.badRequest("from and to must be valid dates (YYYY-MM-DD)");
            }
        }
        LocalDate now = LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        YearMonth ym = YearMonth.of(y, m);
        return new LocalDate[]{ym.atDay(1), ym.atEndOfMonth()};
    }

    @GetMapping("/my")
    public Map<String, Object> listMine(@CurrentUser CurrentUserDetails user) {
        user.requireRole("GUIDE");
        List<AvailabilityWindow> data = service.listWindows(user.id());
        return Map.of("success", true, "count", data.size(), "data", data);
    }

    @GetMapping("/my/calendar")
    public Map<String, Object> getMyCalendar(@CurrentUser CurrentUserDetails user,
                                              @RequestParam(required = false) String from,
                                              @RequestParam(required = false) String to,
                                              @RequestParam(required = false) Integer year,
                                              @RequestParam(required = false) Integer month) {
        user.requireRole("GUIDE");
        LocalDate[] range = resolveRange(from, to, year, month);
        return Map.of("success", true, "data", service.getCalendar(user.id(), range[0], range[1]));
    }

    @GetMapping("/calendar/{guideId}")
    public Map<String, Object> getCalendar(@PathVariable int guideId,
                                            @RequestParam(required = false) String from,
                                            @RequestParam(required = false) String to,
                                            @RequestParam(required = false) Integer year,
                                            @RequestParam(required = false) Integer month) {
        LocalDate[] range = resolveRange(from, to, year, month);
        return Map.of("success", true, "data", service.getCalendar(guideId, range[0], range[1]));
    }

    @GetMapping("/check/{guideId}")
    public Map<String, Object> checkDate(@PathVariable int guideId, @RequestParam(required = false) String date) {
        if (date == null || date.isBlank()) throw ApiException.badRequest("A valid ?date=YYYY-MM-DD is required");
        LocalDate parsed;
        try {
            parsed = LocalDate.parse(date.length() >= 10 ? date.substring(0, 10) : date);
        } catch (Exception e) {
            throw ApiException.badRequest("A valid ?date=YYYY-MM-DD is required");
        }
        DateAvailability result = service.isDateAvailable(guideId, parsed);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("guide_id", guideId);
        data.put("date", parsed);
        data.put("available", result.isAvailable());
        data.put("reason", result.getReason());
        return Map.of("success", true, "data", data);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@CurrentUser CurrentUserDetails user,
                                                        @Valid @RequestBody CreateAvailabilityRequest body) {
        user.requireRole("GUIDE");
        AvailabilityWindow created = service.create(user.id(), body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "message", "Availability saved", "data", created));
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable int id, @CurrentUser CurrentUserDetails user,
                                       @Valid @RequestBody UpdateAvailabilityRequest body) {
        user.requireRole("GUIDE");
        AvailabilityWindow updated = service.update(id, user.id(), body);
        return Map.of("success", true, "message", "Availability updated", "data", updated);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> remove(@PathVariable int id, @CurrentUser CurrentUserDetails user) {
        user.requireRole("GUIDE");
        MessageResult result = service.remove(id, user.id());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", result.getMessage());
        return body;
    }
}
