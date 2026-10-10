package com.example.studentmanagement.service.impl;

import com.example.studentmanagement.Dao.IAppDetailDao;
import com.example.studentmanagement.Dao.IDocumentDao;
import com.example.studentmanagement.constants.CoreConstants;
import com.example.studentmanagement.entity.Document;
import com.example.studentmanagement.exception.BusinessException;
import com.example.studentmanagement.exception.ValidationException;
import com.example.studentmanagement.service.DocumentService;
import com.example.studentmanagement.service.MinioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);

    private final IDocumentDao documentDao;
    private final IAppDetailDao appDetailDao;
    private final MinioService minioService;

    @Value("${minio.bucket.name:tailieu}")
    private String bucketName;

    @Autowired
    public DocumentServiceImpl(IDocumentDao documentDao,
                               IAppDetailDao appDetailDao,
                               MinioService minioService) {
        this.documentDao = documentDao;
        this.appDetailDao = appDetailDao;
        this.minioService = minioService;
    }

    @Override
    @Transactional
    public List<Document> create(List<MultipartFile> files, String appId) {
        logger.info("Starting upload flow for appId: {}, files count: {}", appId, files != null ? files.size() : 0);

        // 1. Validate appId
        if (appId == null || appId.trim().isEmpty()) {
            logger.warn("Upload failed: appId is null or empty");
            throw new ValidationException(CoreConstants.INVALID_APP_ID);
        }
        String cleanAppId = appId.trim();

        // 2. Check business application exists in Database
        if (!appDetailDao.existsById(cleanAppId)) {
            logger.warn("Upload failed: appId '{}' does not exist in Database", cleanAppId);
            throw new ValidationException(CoreConstants.INVALID_APP_ID);
        }

        // 3. Validate files list
        if (files == null || files.isEmpty()) {
            logger.warn("Upload failed: files list is null or empty");
            throw new ValidationException(CoreConstants.FILE_EMPTY);
        }

        // 4. Pre-validate all files before any MinIO upload
        for (MultipartFile file : files) {
            validateFile(file);
        }

        // 5. Upload to MinIO and Save to DB with compensation rollback
        List<String> uploadedMinioPaths = new ArrayList<>();
        List<Document> savedDocuments = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                String docId = UUID.randomUUID().toString();
                String originalFilename = StringUtils.cleanPath(
                        Objects.requireNonNull(file.getOriginalFilename())
                );
                String objectPath = String.format("%s/%s/%s", cleanAppId, docId, originalFilename);

                logger.info("Uploading file to MinIO: bucket={}, path={}, size={}",
                        bucketName, objectPath, file.getSize());

                try (InputStream inputStream = file.getInputStream()) {
                    minioService.uploadFile(
                            bucketName,
                            objectPath,
                            inputStream,
                            file.getSize(),
                            file.getContentType()
                    );
                }
                uploadedMinioPaths.add(objectPath);

                // Build Document metadata
                Document document = new Document();
                document.setId(docId);
                document.setAppId(cleanAppId);
                document.setName(originalFilename);
                document.setPath(objectPath);
                document.setOwner(resolveOwner());
                document.setVersion(1.0);
                document.setDeleted(false);
                document.setActive(true);
                Date now = new Date();
                document.setCreationTime(now);
                document.setModificationTime(now);

                // Save metadata to Database
                logger.info("Saving Document metadata to DB: id={}, name={}", docId, originalFilename);
                Document savedDoc = documentDao.save(document);
                savedDocuments.add(savedDoc);
            }

            logger.info("Upload completed successfully for appId={}, total docs={}", cleanAppId, savedDocuments.size());
            return savedDocuments;

        } catch (Exception e) {
            // COMPENSATION: Clean up any objects already uploaded to MinIO
            logger.error("Error occurred during document creation, executing compensation cleanup on MinIO: {}", e.getMessage());
            for (String path : uploadedMinioPaths) {
                try {
                    minioService.deleteFile(bucketName, path);
                    logger.info("Compensated: removed MinIO object at '{}'", path);
                } catch (Exception cleanupEx) {
                    logger.error("Failed to clean up MinIO object '{}': {}", path, cleanupEx.getMessage());
                }
            }

            if (e instanceof ValidationException) {
                throw (ValidationException) e;
            }
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }
            throw new BusinessException(CoreConstants.MINIO_UPLOAD_FAILED, e);
        }
    }

    @Override
    public ResponseEntity<byte[]> download(String filePath) {
        logger.info("Starting download flow for filePath/id: {}", filePath);

        if (filePath == null || filePath.trim().isEmpty()) {
            throw new ValidationException(CoreConstants.INVALID_DOCUMENT);
        }
        String cleanIdentifier = filePath.trim();

        // 1. Find document by PK (ID) first, fallback to path
        Document document = documentDao.findByPk(cleanIdentifier);
        if (document == null) {
            document = documentDao.findByPath(cleanIdentifier);
        }

        // 2. Validate document exists
        if (document == null) {
            logger.warn("Document not found for identifier: {}", cleanIdentifier);
            throw new ValidationException(CoreConstants.INVALID_DOCUMENT);
        }

        // 3. Check soft-deleted
        if (Boolean.TRUE.equals(document.getDeleted())) {
            logger.warn("Document id={} is soft-deleted, rejecting download", document.getId());
            throw new ValidationException(CoreConstants.INVALID_DOCUMENT);
        }

        // 4. Check active
        if (Boolean.FALSE.equals(document.getActive())) {
            logger.warn("Document id={} is inactive, rejecting download", document.getId());
            throw new ValidationException(CoreConstants.INVALID_DOCUMENT);
        }

        // 5. Get file object from MinIO
        logger.info("Fetching object from MinIO: bucket={}, path={}", bucketName, document.getPath());
        byte[] fileBytes = minioService.downloadFile(bucketName, document.getPath());

        if (fileBytes == null || fileBytes.length == 0) {
            logger.error("File content is empty on MinIO: path={}", document.getPath());
            throw new BusinessException(CoreConstants.FILE_NOT_FOUND);
        }

        // 6. Build HTTP response with proper headers
        MediaType mediaType = resolveMediaType(document.getName());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentLength(fileBytes.length);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(document.getName(), StandardCharsets.UTF_8)
                        .build()
        );

        logger.info("Download ready: documentId={}, name={}, bytes={}", document.getId(), document.getName(), fileBytes.length);
        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }

    @Override
    @Transactional
    public String delete(Map<String, String> id) {
        Document document = fetchAndValidateDocumentById(id);

        logger.info("document name :{}", document.getName());

        // Soft delete: mark deleted = true, keep MinIO file for audit/recovery
        document.setDeleted(true);
        document.setModificationTime(new Date());

        documentDao.update(document);

        logger.info("Document successfully soft-deleted: id={}", document.getId());
        return CoreConstants.SUCCESS_JSON;
    }

    public Document fetchAndValidateDocumentById(Map<String, String> id) {
        if (id == null) {
            throw new ValidationException(CoreConstants.INVALID_DOCUMENT);
        }

        String docId = id.get(CoreConstants.ID);

        if (docId != null && !docId.trim().isEmpty()) {
            Document document = documentDao.findByPk(docId.trim());

            if (document != null) {
                if (Boolean.TRUE.equals(document.getDeleted())) {
                    logger.warn("Document id={} is already deleted", docId);
                    throw new ValidationException(CoreConstants.INVALID_DOCUMENT);
                }
                if (Boolean.FALSE.equals(document.getActive())) {
                    logger.warn("Document id={} is inactive", docId);
                    throw new ValidationException(CoreConstants.INVALID_DOCUMENT);
                }
                return document;
            }
        }

        throw new ValidationException(CoreConstants.INVALID_DOCUMENT);
    }

    @Override
    public Document getById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException(CoreConstants.INVALID_DOCUMENT);
        }
        Document doc = documentDao.findByPk(id.trim());
        if (doc == null || Boolean.TRUE.equals(doc.getDeleted()) || Boolean.FALSE.equals(doc.getActive())) {
            throw new ValidationException(CoreConstants.INVALID_DOCUMENT);
        }
        return doc;
    }

    @Override
    public List<Document> getByAppId(String appId) {
        if (appId == null || appId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return documentDao.findByAppIdAndDeletedFalse(appId.trim());
    }

    @Override
    public List<Document> getAllActive() {
        return documentDao.findAllActive();
    }

    @Override
    public Page<Document> filterDocuments(Specification<Document> spec, Pageable pageable) {
        logger.info("Executing document filter by specification and pageable");
        return documentDao.filter(spec, pageable);
    }

    @Override
    public Page<Document> filterDocuments(String search, int page, int size, List<String> sortParams) {
        logger.info("Executing document filter by search expression: {}, page: {}, size: {}", search, page, size);
        return documentDao.filter(search, page, size, sortParams);
    }

    /**
     * Validate single file: null, empty, filename, and extension.
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() <= 0) {
            throw new ValidationException(CoreConstants.FILE_EMPTY);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new ValidationException(CoreConstants.FILE_EXTENSION_NOT_SUPPORTED);
        }

        String trimmed = originalFilename.trim();
        int lastDotIndex = trimmed.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == trimmed.length() - 1) {
            throw new ValidationException(CoreConstants.FILE_EXTENSION_NOT_SUPPORTED);
        }

        String extension = trimmed.substring(lastDotIndex + 1).toLowerCase();
        logger.info("Extension from multipart: {}", extension);

        if (!CoreConstants.ALLOWED_EXTENSIONS.contains(extension)) {
            logger.warn("Unsupported file extension: '{}' in file '{}'", extension, originalFilename);
            throw new BusinessException(CoreConstants.FILE_EXTENSION_NOT_SUPPORTED);
        }
    }

    /**
     * Resolve current authenticated user/owner.
     * Note: Spring Security auto-configuration is excluded by default in application.properties.
     * When JWT/Security is active, SecurityContextHolder automatically supplies current username.
     */
    private String resolveOwner() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                return authentication.getName();
            }
        } catch (Exception ignored) {
        }
        return "system";
    }

    /**
     * Resolve HTTP MediaType from filename extension.
     */
    private MediaType resolveMediaType(String filename) {
        if (filename == null) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (lower.endsWith(".xlsx")) {
            return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } else if (lower.endsWith(".xls")) {
            return MediaType.parseMediaType("application/vnd.ms-excel");
        } else if (lower.endsWith(".docx")) {
            return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        } else if (lower.endsWith(".doc")) {
            return MediaType.parseMediaType("application/msword");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}