package com.tourguide.offers.controller;

import com.tourguide.offers.dto.CreateOfferRequest;
import com.tourguide.offers.dto.UpdateOfferRequest;

import com.tourguide.offers.model.ExpiredOffersResult;
import com.tourguide.offers.model.OfferPreview;
import com.tourguide.offers.model.SpecialOffer;
import com.tourguide.offers.service.OffersService;
import com.tourguide.shared.error.ApiException;
import com.tourguide.shared.model.DeletionResult;
import com.tourguide.shared.security.CurrentUser;
import com.tourguide.shared.security.CurrentUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Port of modules/offers/offers.routes.js + offers.controller.js. */
@RestController
@RequestMapping("/api/offers")
public class OffersController {

    private final OffersService service;

    public OffersController(OffersService service) {
        this.service = service;
    }

    @GetMapping
    public Map<String, Object> listActive(@RequestParam(required = false) Integer package_id,
                                           @RequestParam(required = false) Integer guide_id) {
        List<SpecialOffer> data = service.listActive(package_id, guide_id);
        return Map.of("success", true, "count", data.size(), "data", data);
    }

    @GetMapping("/my")
    public Map<String, Object> listMine(@CurrentUser CurrentUserDetails user) {
        user.requireRole("GUIDE");
        List<SpecialOffer> data = service.listByGuide(user.id());
        return Map.of("success", true, "count", data.size(), "data", data);
    }

    @PostMapping("/expire")
    public Map<String, Object> expireOutdated(@CurrentUser CurrentUserDetails user) {
        user.requireRole("GUIDE");
        ExpiredOffersResult result = service.expireOutdated(user.id());
        return Map.of("success", true, "message", result.getExpiredCount() + " offer(s) marked EXPIRED", "data", result);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOne(@PathVariable int id) {
        return Map.of("success", true, "data", service.getById(id));
    }

    @GetMapping("/{id}/preview")
    public Map<String, Object> preview(@PathVariable int id,
                                        @RequestParam(required = false) Integer package_id,
                                        @RequestParam(required = false) Integer group_size) {
        if (package_id == null) throw ApiException.badRequest("package_id query parameter is required");
        OfferPreview data = service.preview(id, package_id, group_size == null ? 1 : group_size);
        return Map.of("success", true, "data", data);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@CurrentUser CurrentUserDetails user,
                                                        @Valid @RequestBody CreateOfferRequest body) {
        user.requireRole("GUIDE");
        SpecialOffer created = service.create(user.id(), body, body.package_ids());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "message", "Offer created", "data", created));
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable int id, @CurrentUser CurrentUserDetails user,
                                       @Valid @RequestBody UpdateOfferRequest body) {
        user.requireRole("GUIDE");
        SpecialOffer updated = service.update(id, user.id(), body, body.package_ids());
        return Map.of("success", true, "message", "Offer updated", "data", updated);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> remove(@PathVariable int id, @CurrentUser CurrentUserDetails user) {
        user.requireRole("GUIDE");
        DeletionResult result = service.remove(id, user.id());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("deleted", result.isDeleted());
        body.put("message", result.getMessage());
        return body;
    }
}
