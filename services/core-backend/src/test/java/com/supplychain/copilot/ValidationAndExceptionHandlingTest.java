package com.supplychain.copilot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplychain.copilot.service.dto.AllocateStockRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class ValidationAndExceptionHandlingTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @DisplayName("Should return RFC 7807 Problem Detail on invalid allocation request payload")
        void testInvalidRequestPayloadReturnsRfc7807() throws Exception {
                AllocateStockRequest invalidRequest = AllocateStockRequest.builder()
                                .warehouseId(null) // Violation: @NotNull
                                .sku("") // Violation: @NotBlank
                                .quantity(0) // Violation: @Min(1)
                                .build();

                mockMvc.perform(post("/api/v1/inventory/allocate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.type").value("urn:problem:validation-failed"))
                                .andExpect(jsonPath("$.title").value("Validation Failed"))
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.errors.warehouseId").exists())
                                .andExpect(jsonPath("$.errors.sku").exists())
                                .andExpect(jsonPath("$.errors.quantity").exists());
        }

        @Test
        @DisplayName("Should return RFC 7807 400 when warehouse ID is not found during allocation")
        void testNonExistentWarehouseReturns400() throws Exception {
                AllocateStockRequest request = AllocateStockRequest.builder()
                                .warehouseId(UUID.randomUUID())
                                .sku("TEST-SKU")
                                .quantity(10)
                                .build();

                mockMvc.perform(post("/api/v1/inventory/allocate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.type").value("urn:problem:bad-request"))
                                .andExpect(jsonPath("$.status").value(400));
        }
}
