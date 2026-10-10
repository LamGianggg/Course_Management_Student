package com.example.studentmanagement.constants;

import java.util.Set;

/**
 * System-wide and Document module constants.
 */
public final class CoreConstants {

    private CoreConstants() {
    }

    public static final String SUCCESS_JSON = "{\"status\":\"success\"}";
    public static final String ID = "id";

    // Allowed file extensions
    public static final String PDF = "pdf";
    public static final String XLSX = "xlsx";
    public static final String XLS = "xls";
    public static final String DOC = "doc";
    public static final String DOCX = "docx";
    public static final String JPG = "jpg";
    public static final String JPEG = "jpeg";

    public static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            PDF, XLSX, XLS, DOC, DOCX, JPG, JPEG
    );

    // Business & Validation error messages / codes
    public static final String FILE_EXTENSION_NOT_SUPPORTED = "FILE_EXTENSION_NOT_SUPPORTED";
    public static final String INVALID_DOCUMENT = "INVALID_DOCUMENT";
    public static final String INVALID_APP_ID = "INVALID_APP_ID";
    public static final String FILE_EMPTY = "FILE_EMPTY";
    public static final String FILE_NOT_FOUND = "FILE_NOT_FOUND";
    public static final String MINIO_UPLOAD_FAILED = "MINIO_UPLOAD_FAILED";
    public static final String MINIO_DOWNLOAD_FAILED = "MINIO_DOWNLOAD_FAILED";
    public static final String MINIO_DELETE_FAILED = "MINIO_DELETE_FAILED";
}
