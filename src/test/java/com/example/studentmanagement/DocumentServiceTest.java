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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {

    @Mock
    private IDocumentDao documentDao;

    @Mock
    private IAppDetailDao appDetailDao;

    @Mock
    private MinioService minioService;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private final String validAppId = "APP001";
    private final String bucketName = "tailieu";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentService, "bucketName", bucketName);
    }

    // =========================================================================
    // 1. UPLOAD TESTS (13 REQUIRED TEST CASES)
    // =========================================================================

    @Test
    @DisplayName("Upload 1: Upload PDF thành công")
    void testUploadPdfSuccess() {
        when(appDetailDao.existsById(validAppId)).thenReturn(true);
        when(documentDao.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "files", "sample.pdf", "application/pdf", "%PDF-1.4 test content".getBytes());

        List<Document> result = documentService.create(List.of(file), validAppId);

        assertNotNull(result);
        assertEquals(1, result.size());
        Document doc = result.get(0);
        assertEquals("sample.pdf", doc.getName());
        assertEquals(validAppId, doc.getAppId());
        assertTrue(doc.getPath().contains("sample.pdf"));
        assertFalse(doc.getDeleted());
        assertTrue(doc.getActive());
        assertEquals(1.0, doc.getVersion());

        verify(minioService, times(1)).uploadFile(eq(bucketName), anyString(), any(InputStream.class), eq(file.getSize()), eq(file.getContentType()));
        verify(documentDao, times(1)).save(any(Document.class));
    }

    @Test
    @DisplayName("Upload 2: Upload XLSX thành công")
    void testUploadXlsxSuccess() {
        when(appDetailDao.existsById(validAppId)).thenReturn(true);
        when(documentDao.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "files", "report.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "excel content".getBytes());

        List<Document> result = documentService.create(List.of(file), validAppId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("report.xlsx", result.get(0).getName());
        verify(minioService, times(1)).uploadFile(eq(bucketName), anyString(), any(InputStream.class), eq(file.getSize()), anyString());
    }

    @Test
    @DisplayName("Upload 3: Upload DOCX thành công")
    void testUploadDocxSuccess() {
        when(appDetailDao.existsById(validAppId)).thenReturn(true);
        when(documentDao.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "files", "resume.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "word content".getBytes());

        List<Document> result = documentService.create(List.of(file), validAppId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("resume.docx", result.get(0).getName());
        verify(minioService, times(1)).uploadFile(eq(bucketName), anyString(), any(InputStream.class), eq(file.getSize()), anyString());
    }

    @Test
    @DisplayName("Upload 4: Upload JPG thành công")
    void testUploadJpgSuccess() {
        when(appDetailDao.existsById(validAppId)).thenReturn(true);
        when(documentDao.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "files", "avatar.jpg", "image/jpeg", "jpg binary data".getBytes());

        List<Document> result = documentService.create(List.of(file), validAppId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("avatar.jpg", result.get(0).getName());
        verify(minioService, times(1)).uploadFile(eq(bucketName), anyString(), any(InputStream.class), eq(file.getSize()), anyString());
    }

    @Test
    @DisplayName("Upload 5: Upload JPEG thành công (và hoa thường hoa lộn xộn .JpEg)")
    void testUploadJpegSuccess() {
        when(appDetailDao.existsById(validAppId)).thenReturn(true);
        when(documentDao.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "files", "photo.JpEg", "image/jpeg", "jpeg binary data".getBytes());

        List<Document> result = documentService.create(List.of(file), validAppId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("photo.JpEg", result.get(0).getName());
        verify(minioService, times(1)).uploadFile(eq(bucketName), anyString(), any(InputStream.class), eq(file.getSize()), anyString());
    }

    @Test
    @DisplayName("Upload 6: Extension không hợp lệ (exe, tar.gz)")
    void testUploadInvalidExtension() {
        when(appDetailDao.existsById(validAppId)).thenReturn(true);

        MockMultipartFile file = new MockMultipartFile(
                "files", "malicious.exe", "application/octet-stream", "dummy".getBytes());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                documentService.create(List.of(file), validAppId)
        );
        assertEquals(CoreConstants.FILE_EXTENSION_NOT_SUPPORTED, ex.getMessage());
        verifyNoInteractions(minioService);
        verify(documentDao, never()).save(any());
    }

    @Test
    @DisplayName("Upload 7: Filename không có extension hoặc có dấu chấm kết thúc")
    void testUploadFilenameNoExtension() {
        when(appDetailDao.existsById(validAppId)).thenReturn(true);

        MockMultipartFile fileNoExt = new MockMultipartFile(
                "files", "readme", "text/plain", "dummy".getBytes());

        ValidationException ex1 = assertThrows(ValidationException.class, () ->
                documentService.create(List.of(fileNoExt), validAppId)
        );
        assertEquals(CoreConstants.FILE_EXTENSION_NOT_SUPPORTED, ex1.getMessage());

        MockMultipartFile fileDotOnly = new MockMultipartFile(
                "files", "readme.", "text/plain", "dummy".getBytes());

        ValidationException ex2 = assertThrows(ValidationException.class, () ->
                documentService.create(List.of(fileDotOnly), validAppId)
        );
        assertEquals(CoreConstants.FILE_EXTENSION_NOT_SUPPORTED, ex2.getMessage());
    }

    @Test
    @DisplayName("Upload 8: File empty (size 0 hoặc null)")
    void testUploadEmptyFile() {
        when(appDetailDao.existsById(validAppId)).thenReturn(true);

        MockMultipartFile emptyFile = new MockMultipartFile(
                "files", "empty.pdf", "application/pdf", new byte[0]);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                documentService.create(List.of(emptyFile), validAppId)
        );
        assertEquals(CoreConstants.FILE_EMPTY, ex.getMessage());
        verifyNoInteractions(minioService);
    }

    @Test
    @DisplayName("Upload 9: appId không tồn tại trong Database")
    void testUploadAppIdNotFound() {
        when(appDetailDao.existsById("NON_EXISTING")).thenReturn(false);

        MockMultipartFile file = new MockMultipartFile(
                "files", "sample.pdf", "application/pdf", "data".getBytes());

        ValidationException ex = assertThrows(ValidationException.class, () ->
                documentService.create(List.of(file), "NON_EXISTING")
        );
        assertEquals(CoreConstants.INVALID_APP_ID, ex.getMessage());
        verifyNoInteractions(minioService);
    }

    @Test
    @DisplayName("Upload 10: Nhiều file cùng lúc")
    void testUploadMultipleFilesSuccess() {
        when(appDetailDao.existsById(validAppId)).thenReturn(true);
        when(documentDao.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file1 = new MockMultipartFile("files", "f1.pdf", "application/pdf", "f1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "f2.docx", "application/msword", "f2".getBytes());
        MockMultipartFile file3 = new MockMultipartFile("files", "f3.xlsx", "application/vnd.ms-excel", "f3".getBytes());

        List<Document> result = documentService.create(List.of(file1, file2, file3), validAppId);

        assertEquals(3, result.size());
        verify(minioService, times(3)).uploadFile(eq(bucketName), anyString(), any(InputStream.class), anyLong(), anyString());
        verify(documentDao, times(3)).save(any(Document.class));
    }

    @Test
    @DisplayName("Upload 11: Hai file cùng tên được upload đồng thời mà không bị collision")
    void testUploadTwoFilesWithSameName() {
        when(appDetailDao.existsById(validAppId)).thenReturn(true);
        when(documentDao.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file1 = new MockMultipartFile("files", "document.pdf", "application/pdf", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "document.pdf", "application/pdf", "content2".getBytes());

        List<Document> result = documentService.create(List.of(file1, file2), validAppId);

        assertEquals(2, result.size());
        assertNotEquals(result.get(0).getId(), result.get(1).getId());
        assertNotEquals(result.get(0).getPath(), result.get(1).getPath());
    }

    @Test
    @DisplayName("Upload 12: MinIO upload fail -> Exception ném ra")
    void testUploadMinioFail() {
        when(appDetailDao.existsById(validAppId)).thenReturn(true);
        doThrow(new BusinessException(CoreConstants.MINIO_UPLOAD_FAILED))
                .when(minioService).uploadFile(anyString(), anyString(), any(InputStream.class), anyLong(), any());

        MockMultipartFile file = new MockMultipartFile(
                "files", "doc.pdf", "application/pdf", "content".getBytes());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                documentService.create(List.of(file), validAppId)
        );
        assertEquals(CoreConstants.MINIO_UPLOAD_FAILED, ex.getMessage());
        verify(documentDao, never()).save(any());
    }

    @Test
    @DisplayName("Upload 13: DB save fail sau khi MinIO upload -> Compensation xóa object khỏi MinIO")
    void testUploadDbSaveFailAfterMinioUpload() {
        when(appDetailDao.existsById(validAppId)).thenReturn(true);
        when(documentDao.save(any(Document.class))).thenThrow(new RuntimeException("DB connection dropped"));

        MockMultipartFile file = new MockMultipartFile(
                "files", "doc.pdf", "application/pdf", "content".getBytes());

        assertThrows(RuntimeException.class, () ->
                documentService.create(List.of(file), validAppId)
        );

        // Compensation must have been invoked
        verify(minioService, times(1)).uploadFile(eq(bucketName), anyString(), any(InputStream.class), anyLong(), any());
        verify(minioService, times(1)).deleteFile(eq(bucketName), anyString());
    }

    // =========================================================================
    // 2. DOWNLOAD TESTS (5 REQUIRED TEST CASES)
    // =========================================================================

    @Test
    @DisplayName("Download 1: Download thành công")
    void testDownloadSuccess() {
        Document doc = new Document("doc-1", validAppId, "passport.pdf", "APP001/doc-1/passport.pdf",
                "user1", 1.0, false, new Date(), new Date(), true);

        when(documentDao.findByPk("doc-1")).thenReturn(doc);
        byte[] expectedBytes = "PDF file content".getBytes();
        when(minioService.downloadFile(bucketName, doc.getPath())).thenReturn(expectedBytes);

        ResponseEntity<byte[]> response = documentService.download("doc-1");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(expectedBytes, response.getBody());
        assertTrue(response.getHeaders().getContentDisposition().toString().contains("passport.pdf"));
    }

    @Test
    @DisplayName("Download 2: Document không tồn tại")
    void testDownloadDocumentNotFound() {
        when(documentDao.findByPk("non-existent")).thenReturn(null);
        when(documentDao.findByPath("non-existent")).thenReturn(null);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                documentService.download("non-existent")
        );
        assertEquals(CoreConstants.INVALID_DOCUMENT, ex.getMessage());
    }

    @Test
    @DisplayName("Download 3: Document đã bị soft-deleted -> Không cho download")
    void testDownloadDocumentDeleted() {
        Document doc = new Document("doc-1", validAppId, "deleted.pdf", "path",
                "user1", 1.0, true, new Date(), new Date(), true);

        when(documentDao.findByPk("doc-1")).thenReturn(doc);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                documentService.download("doc-1")
        );
        assertEquals(CoreConstants.INVALID_DOCUMENT, ex.getMessage());
        verify(minioService, never()).downloadFile(anyString(), anyString());
    }

    @Test
    @DisplayName("Download 4: Document inactive -> Không cho download")
    void testDownloadDocumentInactive() {
        Document doc = new Document("doc-1", validAppId, "inactive.pdf", "path",
                "user1", 1.0, false, new Date(), new Date(), false);

        when(documentDao.findByPk("doc-1")).thenReturn(doc);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                documentService.download("doc-1")
        );
        assertEquals(CoreConstants.INVALID_DOCUMENT, ex.getMessage());
        verify(minioService, never()).downloadFile(anyString(), anyString());
    }

    @Test
    @DisplayName("Download 5: Object không tồn tại trên MinIO -> Ném exception")
    void testDownloadObjectNotExistOnMinio() {
        Document doc = new Document("doc-1", validAppId, "file.pdf", "path",
                "user1", 1.0, false, new Date(), new Date(), true);

        when(documentDao.findByPk("doc-1")).thenReturn(doc);
        when(minioService.downloadFile(bucketName, doc.getPath()))
                .thenThrow(new BusinessException(CoreConstants.FILE_NOT_FOUND));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                documentService.download("doc-1")
        );
        assertEquals(CoreConstants.FILE_NOT_FOUND, ex.getMessage());
    }

    // =========================================================================
    // 3. DELETE TESTS (5 REQUIRED TEST CASES)
    // =========================================================================

    @Test
    @DisplayName("Delete 1: Delete thành công (Soft delete)")
    void testDeleteSuccess() {
        Document doc = new Document("doc-1", validAppId, "file.pdf", "path",
                "user1", 1.0, false, new Date(), new Date(), true);

        when(documentDao.findByPk("doc-1")).thenReturn(doc);
        when(documentDao.update(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, String> request = Map.of(CoreConstants.ID, "doc-1");
        String result = documentService.delete(request);

        assertEquals(CoreConstants.SUCCESS_JSON, result);
        assertTrue(doc.getDeleted());
        verify(documentDao, times(1)).update(doc);
        // MinIO file is kept for audit/recovery
        verify(minioService, never()).deleteFile(anyString(), anyString());
    }

    @Test
    @DisplayName("Delete 2: Document không tồn tại")
    void testDeleteDocumentNotFound() {
        when(documentDao.findByPk("unknown")).thenReturn(null);

        Map<String, String> request = Map.of(CoreConstants.ID, "unknown");
        ValidationException ex = assertThrows(ValidationException.class, () ->
                documentService.delete(request)
        );
        assertEquals(CoreConstants.INVALID_DOCUMENT, ex.getMessage());
        verify(documentDao, never()).update(any());
    }

    @Test
    @DisplayName("Delete 3: Document đã deleted trước đó -> Báo lỗi")
    void testDeleteDocumentAlreadyDeleted() {
        Document doc = new Document("doc-1", validAppId, "file.pdf", "path",
                "user1", 1.0, true, new Date(), new Date(), true);

        when(documentDao.findByPk("doc-1")).thenReturn(doc);

        Map<String, String> request = Map.of(CoreConstants.ID, "doc-1");
        ValidationException ex = assertThrows(ValidationException.class, () ->
                documentService.delete(request)
        );
        assertEquals(CoreConstants.INVALID_DOCUMENT, ex.getMessage());
        verify(documentDao, never()).update(any());
    }

    @Test
    @DisplayName("Delete 4: id null trong map")
    void testDeleteIdNull() {
        Map<String, String> request = new HashMap<>();
        request.put(CoreConstants.ID, null);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                documentService.delete(request)
        );
        assertEquals(CoreConstants.INVALID_DOCUMENT, ex.getMessage());
    }

    @Test
    @DisplayName("Delete 5: id empty hoặc blank trong map")
    void testDeleteIdEmpty() {
        Map<String, String> request = Map.of(CoreConstants.ID, "   ");

        ValidationException ex = assertThrows(ValidationException.class, () ->
                documentService.delete(request)
        );
        assertEquals(CoreConstants.INVALID_DOCUMENT, ex.getMessage());
    }

    // =========================================================================
    // 5. FILTER / PAGINATION / SORTING TESTS (HIBERNATEDAO INTEGRATION)
    // =========================================================================

    @Test
    @DisplayName("Filter 1: filterDocuments với Specification và Pageable")
    void testFilterDocumentsWithSpecAndPageable() {
        Document doc1 = new Document("doc-1", validAppId, "thesis.pdf", "path1",
                "user1", 1.0, false, new Date(), new Date(), true);
        Pageable pageable = PageRequest.of(0, 10);
        Specification<Document> spec = (root, query, cb) -> cb.conjunction();
        Page<Document> expectedPage = new PageImpl<>(List.of(doc1), pageable, 1);

        when(documentDao.filter(spec, pageable)).thenReturn(expectedPage);

        Page<Document> actualPage = documentService.filterDocuments(spec, pageable);

        assertNotNull(actualPage);
        assertEquals(1, actualPage.getTotalElements());
        assertEquals("thesis.pdf", actualPage.getContent().get(0).getName());
        verify(documentDao).filter(spec, pageable);
    }

    @Test
    @DisplayName("Filter 2: filterDocuments với search expression, page, size, sortParams")
    void testFilterDocumentsWithSearchParams() {
        Document doc1 = new Document("doc-1", validAppId, "thesis.pdf", "path1",
                "user1", 1.0, false, new Date(), new Date(), true);
        Page<Document> expectedPage = new PageImpl<>(List.of(doc1), PageRequest.of(0, 10), 1);

        when(documentDao.filter("name:thesis", 0, 10, List.of("creationTime,desc")))
                .thenReturn(expectedPage);

        Page<Document> actualPage = documentService.filterDocuments("name:thesis", 0, 10, List.of("creationTime,desc"));

        assertNotNull(actualPage);
        assertEquals(1, actualPage.getTotalElements());
        assertEquals("doc-1", actualPage.getContent().get(0).getId());
        verify(documentDao).filter("name:thesis", 0, 10, List.of("creationTime,desc"));
    }
}
