package com.bookingsystem.resource.service;

import com.bookingsystem.common.exception.AppException;
import com.bookingsystem.common.exception.ResourceNotFoundException;
import com.bookingsystem.resource.dto.ResourceRequest;
import com.bookingsystem.resource.dto.ResourceResponse;
import com.bookingsystem.resource.model.Resource;
import com.bookingsystem.resource.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private com.bookingsystem.booking.repository.BookingRepository bookingRepository;

    @InjectMocks
    private ResourceService resourceService;

    private Resource sampleResource;

    @BeforeEach
    void setUp() {
        sampleResource = new Resource();
        sampleResource.setId(1L);
        sampleResource.setName("Conference Room A");
        sampleResource.setDescription("Main hall");
        sampleResource.setCapacity(10);
        sampleResource.setType("ROOM");
        sampleResource.setActive(true);
        sampleResource.setSlotGranularityMinutes(30);
        sampleResource.setMinBookingDurationMinutes(30);
        sampleResource.setMaxBookingDurationMinutes(240);
        sampleResource.setVersion(0L);
    }

    @Test
    void getAllResources_ActiveOnly_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(resourceRepository.findAllByActiveTrue(pageable))
            .thenReturn(new PageImpl<>(List.of(sampleResource)));

        Page<ResourceResponse> result = resourceService.getAllResources(pageable, false);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Conference Room A");
    }

    @Test
    void getResourceById_Success() {
        when(resourceRepository.findByIdWithOpeningHours(1L))
            .thenReturn(Optional.of(sampleResource));

        ResourceResponse result = resourceService.getResourceById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Conference Room A");
    }

    @Test
    void getResourceById_NotFound_ThrowsException() {
        when(resourceRepository.findByIdWithOpeningHours(99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> resourceService.getResourceById(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createResource_Success() {
        ResourceRequest request = new ResourceRequest(
            "New Room", "Desc", 5, "ROOM", true, 30, 30, 120, List.of()
        );

        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> {
            Resource r = invocation.getArgument(0);
            r.setId(2L);
            return r;
        });

        ResourceResponse created = resourceService.createResource(request);

        assertThat(created).isNotNull();
        assertThat(created.name()).isEqualTo("New Room");
        verify(resourceRepository, times(1)).save(any(Resource.class));
    }

    @Test
    void createResource_InvalidMinMaxDuration_ThrowsException() {
        ResourceRequest request = new ResourceRequest(
            "Room", "Desc", 5, "ROOM", true, 30, 120, 30, List.of() // min > max
        );

        assertThatThrownBy(() -> resourceService.createResource(request))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Minimum booking duration cannot exceed maximum");
    }

    @Test
    void getAvailability_CalculatesFreeAndBookedSlotsCorrectly() {
        java.time.ZonedDateTime nextMonday = java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC"))
            .plusDays(7)
            .with(java.time.DayOfWeek.MONDAY)
            .withHour(0).withMinute(0).withSecond(0).withNano(0);

        java.time.Instant queryStart = nextMonday.toInstant();
        java.time.Instant queryEnd = nextMonday.plusDays(1).toInstant();

        sampleResource.clearOpeningHours();
        sampleResource.addOpeningHours(new com.bookingsystem.resource.model.OpeningHours(java.time.DayOfWeek.MONDAY, java.time.LocalTime.of(8, 0), java.time.LocalTime.of(18, 0), false));

        when(resourceRepository.findByIdWithOpeningHours(1L)).thenReturn(Optional.of(sampleResource));
        when(bookingRepository.findActiveBookingsInDateRange(eq(1L), any(), any())).thenReturn(List.of());

        com.bookingsystem.resource.dto.ResourceAvailabilityResponse response = resourceService.getAvailability(1L, queryStart, queryEnd);

        assertThat(response).isNotNull();
        assertThat(response.freeSlots()).isNotEmpty();
        assertThat(response.bookedSlots()).isEmpty();
    }
}
