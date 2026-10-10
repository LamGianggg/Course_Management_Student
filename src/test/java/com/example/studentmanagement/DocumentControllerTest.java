package com.example.studentmanagement;

import com.example.studentmanagement.constants.CoreConstants;
import com.example.studentmanagement.entity.Document;
import com.example.studentmanagement.exception.GlobalExceptionHandler;
import com.example.studentmanagement.rest.DocumentController;
import com.example.studentmanagement.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class DocumentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentController documentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(documentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Controller: Upload documents endpoint")
    void testUploadEndpoint() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "my_doc.pdf", "application/pdf", "dummy pdf".getBytes());

        Document doc = new Document("uuid-123", "APP001", "my_doc.pdf", "APP001/uuid-123/my_doc.pdf",
                "system", 1.0, false, new Date(), new Date(), true);

        when(documentService.create(any(), eq("APP001"))).thenReturn(List.of(doc));

        mockMvc.perform(multipart("/api/documents")
                        .file(file)
                        .param("appId", "APP001"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("uuid-123"))
                .andExpect(jsonPath("$.data[0].name").value("my_doc.pdf"))
                .andExpect(jsonPath("$.data[0].appId").value("APP001"));
    }

    @Test
    @DisplayName("Controller: Download by query param endpoint")
    void testDownloadByParamEndpoint() throws Exception {
        byte[] content = "file data".getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        when(documentService.download("uuid-123"))
                .thenReturn(ResponseEntity.ok().headers(headers).body(content));

        mockMvc.perform(get("/api/documents/download")
                        .param("filePath", "uuid-123"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(content));
    }

    @Test
    @DisplayName("Controller: Download by path variable endpoint")
    void testDownloadByPathEndpoint() throws Exception {
        byte[] content = "file data".getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        when(documentService.download("uuid-123"))
                .thenReturn(ResponseEntity.ok().headers(headers).body(content));

        mockMvc.perform(get("/api/documents/uuid-123/download"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(content));
    }

    @Test
    @DisplayName("Controller: Delete document endpoint with Map body")
    void testDeleteEndpoint() throws Exception {
        when(documentService.delete(Map.of(CoreConstants.ID, "uuid-123")))
                .thenReturn(CoreConstants.SUCCESS_JSON);

        mockMvc.perform(delete("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"uuid-123\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(CoreConstants.SUCCESS_JSON));
    }

    @Test
    @DisplayName("Controller: Delete document endpoint by path variable ID")
    void testDeleteByIdPathVariableEndpoint() throws Exception {
        when(documentService.delete(Map.of(CoreConstants.ID, "uuid-123")))
                .thenReturn(CoreConstants.SUCCESS_JSON);

        mockMvc.perform(delete("/api/documents/uuid-123"))
                .andExpect(status().isOk())
                .andExpect(content().string(CoreConstants.SUCCESS_JSON));
    }

    @Test
    @DisplayName("Controller: Get document by ID endpoint")
    void testGetByIdEndpoint() throws Exception {
        Document doc = new Document("uuid-123", "APP001", "my_doc.pdf", "path",
                "system", 1.0, false, new Date(), new Date(), true);

        when(documentService.getById("uuid-123")).thenReturn(doc);

        mockMvc.perform(get("/api/documents/uuid-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("uuid-123"))
                .andExpect(jsonPath("$.data.name").value("my_doc.pdf"));
    }

    @Test
    @DisplayName("Controller: Get documents by appId endpoint")
    void testGetByAppIdEndpoint() throws Exception {
        Document doc = new Document("uuid-123", "APP001", "my_doc.pdf", "path",
                "system", 1.0, false, new Date(), new Date(), true);

        when(documentService.getByAppId("APP001")).thenReturn(List.of(doc));

        mockMvc.perform(get("/api/documents/app/APP001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("uuid-123"));
    }
}
