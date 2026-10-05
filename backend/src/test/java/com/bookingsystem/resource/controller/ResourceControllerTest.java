package com.bookingsystem.resource.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bookingsystem.resource.dto.ResourceRequest;
import com.bookingsystem.resource.dto.ResourceResponse;
import com.bookingsystem.resource.service.ResourceService;
import com.bookingsystem.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResourceService resourceService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private ResourceResponse sampleResponse() {
        return new ResourceResponse(
            1L, "Meeting Room 1", "Nice room", 8, "ROOM", true,
            30, 30, 240, 0L, List.of(), Instant.now(), Instant.now()
        );
    }

    @Test
    void getResources_Returns401_WhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/resources"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getResources_Returns200_WhenUserRole() throws Exception {
        when(resourceService.getAllResources(any(Pageable.class), eq(false)))
            .thenReturn(new PageImpl<>(List.of(sampleResponse())));

        mockMvc.perform(get("/api/v1/resources"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Meeting Room 1"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createResource_Returns403Forbidden_WhenUserRole() throws Exception {
        ResourceRequest request = new ResourceRequest(
            "Room A", "Desc", 4, "ROOM", true, 30, 30, 120, List.of()
        );

        mockMvc.perform(post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createResource_Returns201Created_WhenAdminRole() throws Exception {
        ResourceRequest request = new ResourceRequest(
            "Room A", "Desc", 4, "ROOM", true, 30, 30, 120, List.of()
        );

        when(resourceService.createResource(any(ResourceRequest.class)))
            .thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Meeting Room 1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteResource_Returns204NoContent_WhenAdminRole() throws Exception {
        mockMvc.perform(delete("/api/v1/resources/1"))
            .andExpect(status().isNoContent());
    }
}
