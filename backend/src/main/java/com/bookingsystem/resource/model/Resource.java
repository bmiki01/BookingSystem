package com.bookingsystem.resource.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer capacity = 1;

    @Column(nullable = false, length = 100)
    private String type = "ROOM";

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "slot_granularity_minutes", nullable = false)
    private Integer slotGranularityMinutes = 30;

    @Column(name = "min_booking_duration_minutes", nullable = false)
    private Integer minBookingDurationMinutes = 30;

    @Column(name = "max_booking_duration_minutes", nullable = false)
    private Integer maxBookingDurationMinutes = 240;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OpeningHours> openingHours = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Resource() {
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addOpeningHours(OpeningHours hours) {
        hours.setResource(this);
        this.openingHours.add(hours);
    }

    public void clearOpeningHours() {
        this.openingHours.forEach(oh -> oh.setResource(null));
        this.openingHours.clear();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getSlotGranularityMinutes() {
        return slotGranularityMinutes;
    }

    public void setSlotGranularityMinutes(Integer slotGranularityMinutes) {
        this.slotGranularityMinutes = slotGranularityMinutes;
    }

    public Integer getMinBookingDurationMinutes() {
        return minBookingDurationMinutes;
    }

    public void setMinBookingDurationMinutes(Integer minBookingDurationMinutes) {
        this.minBookingDurationMinutes = minBookingDurationMinutes;
    }

    public Integer getMaxBookingDurationMinutes() {
        return maxBookingDurationMinutes;
    }

    public void setMaxBookingDurationMinutes(Integer maxBookingDurationMinutes) {
        this.maxBookingDurationMinutes = maxBookingDurationMinutes;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public List<OpeningHours> getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(List<OpeningHours> openingHours) {
        this.openingHours = openingHours;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
