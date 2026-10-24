package com.example.studentmanagement;

import com.example.studentmanagement.config.OpenApiConfig;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class OpenApiExportTest {

    @Test
    void testCustomOpenAPI() {
        OpenApiConfig config = new OpenApiConfig();
        ReflectionTestUtils.setField(config, "version", "1.0.0");
        ReflectionTestUtils.setField(config, "title", "Test API");
        ReflectionTestUtils.setField(config, "description", "Test Description");

        OpenAPI openAPI = config.customOpenAPI();

        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertEquals("Test API REST API", openAPI.getInfo().getTitle());
        assertEquals("Test Description", openAPI.getInfo().getDescription());
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes().get("BearerAuth"));
        assertFalse(openAPI.getServers().isEmpty());
    }
}
