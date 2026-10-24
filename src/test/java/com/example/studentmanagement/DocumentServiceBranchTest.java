package com.example.studentmanagement;

import com.example.studentmanagement.Dao.IAppDetailDao;
import com.example.studentmanagement.Dao.IDocumentDao;
import com.example.studentmanagement.constants.CoreConstants;
import com.example.studentmanagement.entity.Document;
import com.example.studentmanagement.exception.BusinessException;
import com.example.studentmanagement.exception.ValidationException;
import com.example.studentmanagement.service.MinioService;
import com.example.studentmanagement.service.impl.DocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceBranchTest {

    @Mock
    private IDocumentDao documentDao;

    @Mock
    private IAppDetailDao appDetailDao;

    @Mock
    private MinioService minioService;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentService, "bucketName", "tailieu");
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCreateValidationBranches() {
        // 1. appId null or empty
        assertThrows(ValidationException.class, () -> documentService.create(List.of(), null));
        assertThrows(ValidationException.class, () -> documentService.create(List.of(), "   "));

        // 2. appId not exists
        when(appDetailDao.existsById("APP_UNKNOWN")).thenReturn(false);
        assertThrows(ValidationException.class, () -> documentService.create(List.of(), "APP_UNKNOWN"));

        // 3. files null or empty
        when(appDetailDao.existsById("APP1")).thenReturn(true);
        assertThrows(ValidationException.class, () -> documentService.create(null, "APP1"));
        assertThrows(ValidationException.class, () -> documentService.create(Collections.emptyList(), "APP1"));

        // 4. validateFile branches
        // empty file
        MockMultipartFile emptyFile = new MockMultipartFile("files", "test.pdf", "application/pdf", new byte[0]);
        assertThrows(ValidationException.class, () -> documentService.create(List.of(emptyFile), "APP1"));

        // null filename
        MockMultipartFile nullFilename = new MockMultipartFile("files", null, "application/pdf", "content".getBytes());
        assertThrows(ValidationException.class, () -> documentService.create(List.of(nullFilename), "APP1"));

        // blank filename
        MockMultipartFile blankFilename = new MockMultipartFile("files", "   ", "application/pdf", "content".getBytes());
        assertThrows(ValidationException.class, () -> documentService.create(List.of(blankFilename), "APP1"));

        // no dot in filename
        MockMultipartFile noDot = new MockMultipartFile("files", "nodotfile", "application/pdf", "content".getBytes());
        assertThrows(ValidationException.class, () -> documentService.create(List.of(noDot), "APP1"));

        // dot at end
        MockMultipartFile dotEnd = new MockMultipartFile("files", "dotend.", "application/pdf", "content".getBytes());
        assertThrows(ValidationException.class, () -> documentService.create(List.of(dotEnd), "APP1"));

        // unsupported extension
        MockMultipartFile badExt = new MockMultipartFile("files", "virus.exe", "application/octet-stream", "content".getBytes());
        assertThrows(BusinessException.class, () -> documentService.create(List.of(badExt), "APP1"));
    }

    @Test
    void testCreateWithSecurityOwners() {
        when(appDetailDao.existsById("APP1")).thenReturn(true);
        when(documentDao.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("files", "doc.pdf", "application/pdf", "content".getBytes());

        // 1. Authenticated user
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", "pass", List.of()));
        List<Document> res1 = documentService.create(List.of(file), "APP1");
        assertEquals("alice", res1.get(0).getOwner());

        // 2. Anonymous user
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", "pass", List.of()));
        List<Document> res2 = documentService.create(List.of(file), "APP1");
        assertEquals("system", res2.get(0).getOwner());

        // 3. Null auth
        SecurityContextHolder.clearContext();
        List<Document> res3 = documentService.create(List.of(file), "APP1");
        assertEquals("system", res3.get(0).getOwner());
    }

    @Test
    void testCreateCompensationExceptions() {
        when(appDetailDao.existsById("APP1")).thenReturn(true);
        MockMultipartFile file1 = new MockMultipartFile("files", "doc1.pdf", "application/pdf", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "doc2.pdf", "application/pdf", "content2".getBytes());

        // When second upload fails, first is deleted (and cleanup throws exception)
        doNothing().doThrow(new RuntimeException("Upload failed on 2nd")).when(minioService)
                .uploadFile(anyString(), anyString(), any(InputStream.class), anyLong(), anyString());
        doThrow(new RuntimeException("Cleanup fail")).when(minioService).deleteFile(anyString(), anyString());

        assertThrows(BusinessException.class, () -> documentService.create(List.of(file1, file2), "APP1"));
    }

    @Test
    void testDownloadBranches() {
        // null or blank filePath
        assertThrows(ValidationException.class, () -> documentService.download(null));
        assertThrows(ValidationException.class, () -> documentService.download("   "));

        // not found by PK or path
        when(documentDao.findByPk("notfound")).thenReturn(null);
        when(documentDao.findByPath("notfound")).thenReturn(null);
        assertThrows(ValidationException.class, () -> documentService.download("notfound"));

        // found by path fallback
        Document docPath = new Document("d-1", "APP1", "photo.jpg", "path/photo.jpg", "alice", 1.0, false, new Date(), new Date(), true);
        when(documentDao.findByPk("path/photo.jpg")).thenReturn(null);
        when(documentDao.findByPath("path/photo.jpg")).thenReturn(docPath);
        when(minioService.downloadFile(anyString(), anyString())).thenReturn("image-bytes".getBytes());

        ResponseEntity<byte[]> resp1 = documentService.download("path/photo.jpg");
        assertEquals(200, resp1.getStatusCode().value());

        // soft-deleted document
        Document deletedDoc = new Document("d-del", "APP1", "del.pdf", "p", "u", 1.0, true, new Date(), new Date(), true);
        when(documentDao.findByPk("d-del")).thenReturn(deletedDoc);
        assertThrows(ValidationException.class, () -> documentService.download("d-del"));

        // inactive document
        Document inactiveDoc = new Document("d-inact", "APP1", "in.pdf", "p", "u", 1.0, false, new Date(), new Date(), false);
        when(documentDao.findByPk("d-inact")).thenReturn(inactiveDoc);
        assertThrows(ValidationException.class, () -> documentService.download("d-inact"));

        // empty file bytes from minio
        Document validDoc = new Document("d-valid", "APP1", "v.pdf", "p", "u", 1.0, false, new Date(), new Date(), true);
        when(documentDao.findByPk("d-valid")).thenReturn(validDoc);
        when(minioService.downloadFile(anyString(), anyString())).thenReturn(new byte[0]);
        assertThrows(BusinessException.class, () -> documentService.download("d-valid"));

        when(minioService.downloadFile(anyString(), anyString())).thenReturn(null);
        assertThrows(BusinessException.class, () -> documentService.download("d-valid"));

        // MediaTypes branches
        when(minioService.downloadFile(anyString(), anyString())).thenReturn("data".getBytes());
        String[] testFiles = {"test.pdf", "test.jpg", "test.jpeg", "test.xlsx", "test.xls", "test.docx", "test.doc", "test.txt"};
        for (String fn : testFiles) {
            validDoc.setName(fn);
            ResponseEntity<byte[]> r = documentService.download("d-valid");
            assertNotNull(r);
        }

        // null filename mediaType
        validDoc.setName(null);
        ResponseEntity<byte[]> rNull = documentService.download("d-valid");
        assertNotNull(rNull);
    }

    @Test
    void testDeleteAndGetByIdBranches() {
        // delete branches
        assertThrows(ValidationException.class, () -> documentService.delete(null));
        assertThrows(ValidationException.class, () -> documentService.delete(Map.of()));
        assertThrows(ValidationException.class, () -> documentService.delete(Map.of(CoreConstants.ID, "   ")));

        when(documentDao.findByPk("not-exist")).thenReturn(null);
        assertThrows(ValidationException.class, () -> documentService.delete(Map.of(CoreConstants.ID, "not-exist")));

        Document inactive = new Document("d-in", "APP1", "n.pdf", "p", "u", 1.0, false, new Date(), new Date(), false);
        when(documentDao.findByPk("d-in")).thenReturn(inactive);
        assertThrows(ValidationException.class, () -> documentService.delete(Map.of(CoreConstants.ID, "d-in")));

        // getById branches
        assertThrows(ValidationException.class, () -> documentService.getById(null));
        assertThrows(ValidationException.class, () -> documentService.getById("   "));
        when(documentDao.findByPk("non-id")).thenReturn(null);
        assertThrows(ValidationException.class, () -> documentService.getById("non-id"));

        when(documentDao.findByPk("d-in")).thenReturn(inactive);
        assertThrows(ValidationException.class, () -> documentService.getById("d-in"));

        Document activeDoc = new Document("d-ok", "APP1", "ok.pdf", "p", "u", 1.0, false, new Date(), new Date(), true);
        when(documentDao.findByPk("d-ok")).thenReturn(activeDoc);
        assertEquals(activeDoc, documentService.getById("d-ok"));

        // getByAppId branches
        assertTrue(documentService.getByAppId(null).isEmpty());
        assertTrue(documentService.getByAppId("   ").isEmpty());
        when(documentDao.findByAppIdAndDeletedFalse("APP1")).thenReturn(List.of(activeDoc));
        assertEquals(1, documentService.getByAppId("APP1").size());

        // getAllActive
        when(documentDao.findAllActive()).thenReturn(List.of(activeDoc));
        assertEquals(1, documentService.getAllActive().size());
    }
}
