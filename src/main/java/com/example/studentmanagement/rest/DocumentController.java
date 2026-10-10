package com.example.studentmanagement.rest;

import com.example.studentmanagement.HibernateDao.HibernateDao;
import com.example.studentmanagement.constants.CoreConstants;
import com.example.studentmanagement.dto.ApiResponse;
import com.example.studentmanagement.dto.PagedResponse;
import com.example.studentmanagement.entity.Document;
import com.example.studentmanagement.service.DocumentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping({"/api/documents", "/documents"})
@CrossOrigin(origins = "*")
@Api(tags = {"Document Management"})
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    /**
     * Whitelist of fields that clients are allowed to sort by for Document.
     */
    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "appId", "owner", "version", "creationTime", "modificationTime", "active", "deleted"
    );

    /**
     * Whitelist of fields that clients are allowed to search by for Document.
     */
    public static final Set<String> ALLOWED_SEARCH_FIELDS = Set.of(
            "id", "name", "appId", "owner", "path", "version", "deleted", "active", "creationTime"
    );

    public static final String DEFAULT_SORT_FIELD = "creationTime";
    public static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.DESC;

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Upload one or more documents linked to a specific business application (appId).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "Upload one or more documents for an appId")
    public ResponseEntity<ApiResponse<List<Document>>> create(
            @ApiParam(value = "List of files to upload", required = true)
            @RequestParam("files") List<MultipartFile> files,
            @ApiParam(value = "Application/Business detail ID", required = true)
            @RequestParam("appId") String appId) {
        logger.info("REST request to upload documents: appId={}, file count={}",
                appId, files != null ? files.size() : 0);
        List<Document> documents = documentService.create(files, appId);
        return new ResponseEntity<>(ApiResponse.ok("Documents uploaded successfully", documents), HttpStatus.CREATED);
    }

    /**
     * Download document file by filePath or documentId query param.
     */
    @GetMapping("/download")
    @ApiOperation(value = "Download document by filePath or ID query parameter")
    public ResponseEntity<byte[]> download(
            @ApiParam(value = "File path or Document ID", required = true)
            @RequestParam("filePath") String filePath) {
        logger.info("REST request to download document: filePath={}", filePath);
        return documentService.download(filePath);
    }

    /**
     * Download document file by path variable ID.
     */
    @GetMapping("/{id}/download")
    @ApiOperation(value = "Download document by Document ID path variable")
    public ResponseEntity<byte[]> downloadById(
            @ApiParam(value = "Document ID", required = true)
            @PathVariable("id") String id) {
        logger.info("REST request to download document by ID: {}", id);
        return documentService.download(id);
    }

    /**
     * Soft-delete document using Map<String, String> body (e.g. {"id": "UUID"}).
     */
    @DeleteMapping
    @ApiOperation(value = "Soft delete document by ID map")
    public ResponseEntity<String> delete(
            @ApiParam(value = "Map containing 'id'", required = true)
            @RequestBody Map<String, String> id) {
        logger.info("REST request to soft delete document: {}", id);
        String result = documentService.delete(id);
        return ResponseEntity.ok(result);
    }

    /**
     * Soft-delete document by path variable ID.
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "Soft delete document by ID path variable")
    public ResponseEntity<String> deleteById(
            @ApiParam(value = "Document ID", required = true)
            @PathVariable("id") String id) {
        logger.info("REST request to soft delete document by path ID: {}", id);
        Map<String, String> idMap = new HashMap<>();
        idMap.put(CoreConstants.ID, id);
        String result = documentService.delete(idMap);
        return ResponseEntity.ok(result);
    }

    /**
     * Get document metadata by ID.
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "Get document metadata by ID")
    public ResponseEntity<ApiResponse<Document>> getById(
            @ApiParam(value = "Document ID", required = true)
            @PathVariable("id") String id) {
        Document document = documentService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok("Document retrieved successfully", document));
    }

    /**
     * Get all active documents for a given appId.
     */
    @GetMapping("/app/{appId}")
    @ApiOperation(value = "Get all active documents by appId")
    public ResponseEntity<ApiResponse<List<Document>>> getByAppId(
            @ApiParam(value = "App ID", required = true)
            @PathVariable("appId") String appId) {
        List<Document> documents = documentService.getByAppId(appId);
        return ResponseEntity.ok(ApiResponse.ok("Documents retrieved successfully", documents));
    }

    /**
     * Get all active documents in the system.
     */
    @GetMapping
    @ApiOperation(value = "Get all active documents")
    public ResponseEntity<ApiResponse<List<Document>>> getAllActive() {
        List<Document> documents = documentService.getAllActive();
        return ResponseEntity.ok(ApiResponse.ok("All active documents retrieved successfully", documents));
    }

    /**
     * Advanced filter, search, pagination, and sorting endpoint for documents.
     * Uses unified HibernateDao helper for Specification parsing, bounds validation, and sorting.
     *
     * Search syntax: field{operator}value (operators: :, >, <, !), comma-separated for AND.
     * Allowed search fields: id, name, appId, owner, path, version, deleted, active, creationTime
     *
     * Pagination: page (0-based, default 0), size (default 10, between 1 and 100)
     * Sorting: sort=field,direction (e.g. sort=creationTime,desc or sort=name,asc). Default: creationTime,desc
     * Allowed sort fields: id, name, appId, owner, version, creationTime, modificationTime, active, deleted
     *
     * Examples:
     *   GET /api/documents/filter?search=name:report&page=0&size=10
     *   GET /api/documents/filter?search=appId:APP001,active:true&sort=creationTime,desc
     *   GET /api/documents/filter?search=version>1.0&page=0&size=5&sort=name,asc
     *   GET /api/documents/filter?page=0&size=10&sort=creationTime,desc (no search = all documents)
     */
    @GetMapping("/filter")
    @ApiOperation(value = "Filter documents with dynamic search, pagination, and sorting")
    public ResponseEntity<ApiResponse<PagedResponse<Document>>> filterDocuments(
            @ApiParam(value = "Search expression (e.g. name:report,appId:APP001)")
            @RequestParam(required = false) String search,
            @ApiParam(value = "Page index (0-based)", defaultValue = "0")
            @RequestParam(defaultValue = "0") int page,
            @ApiParam(value = "Page size (1-100)", defaultValue = "10")
            @RequestParam(defaultValue = "10") int size,
            @ApiParam(value = "Sort parameters (e.g. creationTime,desc)")
            @RequestParam(required = false) List<String> sort) {

        logger.info("REST request to filter documents: search='{}', page={}, size={}, sort={}", search, page, size, sort);

        Pageable pageable = HibernateDao.createPageable(page, size, sort, ALLOWED_SORT_FIELDS, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
        Specification<Document> spec = HibernateDao.parse(search, ALLOWED_SEARCH_FIELDS);
        Page<Document> resultPage = documentService.filterDocuments(spec, pageable);
        PagedResponse<Document> pagedResponse = new PagedResponse<>(resultPage);

        return ResponseEntity.ok(ApiResponse.ok("Documents filtered successfully", pagedResponse));
    }

    /**
     * Search documents endpoint alias for convenience.
     */
    @GetMapping("/search")
    @ApiOperation(value = "Search documents with dynamic search, pagination, and sorting")
    public ResponseEntity<ApiResponse<PagedResponse<Document>>> searchDocuments(
            @ApiParam(value = "Search expression (e.g. name:report,appId:APP001)")
            @RequestParam(required = false) String search,
            @ApiParam(value = "Page index (0-based)", defaultValue = "0")
            @RequestParam(defaultValue = "0") int page,
            @ApiParam(value = "Page size (1-100)", defaultValue = "10")
            @RequestParam(defaultValue = "10") int size,
            @ApiParam(value = "Sort parameters (e.g. creationTime,desc)")
            @RequestParam(required = false) List<String> sort) {
        return filterDocuments(search, page, size, sort);
    }
}
