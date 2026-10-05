package com.bookingsystem.resource.controller;

import com.bookingsystem.resource.dto.ResourceAvailabilityResponse;
import com.bookingsystem.resource.dto.ResourceRequest;
import com.bookingsystem.resource.dto.ResourceResponse;
import com.bookingsystem.resource.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/resources")
@Tag(name = "Resources", description = "Endpoints for viewing and managing bookable resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    @Operation(summary = "List bookable resources", description = "Returns a paginated list of active resources (ADMINs can optionally view inactive resources)")
    public ResponseEntity<Page<ResourceResponse>> getAllResources(
        @PageableDefault(size = 20, sort = "name") Pageable pageable,
        @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        Page<ResourceResponse> page = resourceService.getAllResources(pageable, includeInactive);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get resource details", description = "Returns details and opening hours of a specific resource")
    public ResponseEntity<ResourceResponse> getResourceById(@PathVariable Long id) {
        ResourceResponse resource = resourceService.getResourceById(id);
        return ResponseEntity.ok(resource);
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Get resource availability", description = "Returns free and booked time slots for a resource within a given UTC date range")
    public ResponseEntity<ResourceAvailabilityResponse> getAvailability(
        @PathVariable Long id,
        @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.Instant start,
        @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.Instant end
    ) {
        ResourceAvailabilityResponse response = resourceService.getAvailability(id, start, end);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new resource (ADMIN only)", description = "Creates a new resource with opening hours")
    public ResponseEntity<ResourceResponse> createResource(@Valid @RequestBody ResourceRequest request) {
        ResourceResponse created = resourceService.createResource(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing resource (ADMIN only)", description = "Updates a resource. Uses optimistic locking to prevent concurrent overwrite issues.")
    public ResponseEntity<ResourceResponse> updateResource(
        @PathVariable Long id,
        @Valid @RequestBody ResourceRequest request
    ) {
        ResourceResponse updated = resourceService.updateResource(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a resource (ADMIN only)", description = "Removes a resource from the system")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}
