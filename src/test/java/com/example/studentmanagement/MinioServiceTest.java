package com.example.studentmanagement;

import com.example.studentmanagement.exception.BusinessException;
import com.example.studentmanagement.service.impl.MinioServiceImpl;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MinioServiceTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioServiceImpl minioService;

    private static final String BUCKET = "test-bucket";
    private static final String PATH = "files/test.txt";

    @Test
    void testEnsureBucketExists_WhenBucketNotExists() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        doNothing().when(minioClient).makeBucket(any(MakeBucketArgs.class));

        assertDoesNotThrow(() -> minioService.ensureBucketExists(BUCKET));
        verify(minioClient, times(1)).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void testEnsureBucketExists_WhenBucketAlreadyExists() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        assertDoesNotThrow(() -> minioService.ensureBucketExists(BUCKET));
        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void testUploadFile_Success() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        InputStream stream = new ByteArrayInputStream("hello world".getBytes());
        assertDoesNotThrow(() -> minioService.uploadFile(BUCKET, PATH, stream, 11L, "text/plain"));
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void testUploadFile_Error() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(new RuntimeException("Upload err"));

        InputStream stream = new ByteArrayInputStream("hello world".getBytes());
        assertThrows(BusinessException.class, () -> minioService.uploadFile(BUCKET, PATH, stream, 11L, "text/plain"));
    }

    @Test
    void testDeleteFile_Success() throws Exception {
        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        assertDoesNotThrow(() -> minioService.deleteFile(BUCKET, PATH));
        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void testDeleteFile_Error() throws Exception {
        doThrow(new RuntimeException("Remove err")).when(minioClient).removeObject(any(RemoveObjectArgs.class));

        assertThrows(BusinessException.class, () -> minioService.deleteFile(BUCKET, PATH));
    }

    @Test
    void testEnsureBucketExists_Error() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenThrow(new RuntimeException("Network err"));
        assertDoesNotThrow(() -> minioService.ensureBucketExists(BUCKET));
    }

    @Test
    void testDownloadFile_Success() throws Exception {
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        byte[] expected = "file content".getBytes();
        when(mockResponse.readAllBytes()).thenReturn(expected);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        byte[] result = minioService.downloadFile(BUCKET, PATH);
        assertArrayEquals(expected, result);
    }

    @Test
    void testDownloadFile_NoSuchKey() throws Exception {
        ErrorResponseException ex = mock(ErrorResponseException.class);
        ErrorResponse errorResponse = mock(ErrorResponse.class);
        when(errorResponse.code()).thenReturn("NoSuchKey");
        when(ex.errorResponse()).thenReturn(errorResponse);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(ex);

        assertThrows(BusinessException.class, () -> minioService.downloadFile(BUCKET, PATH));
    }

    @Test
    void testDownloadFile_OtherErrorResponse() throws Exception {
        ErrorResponseException ex = mock(ErrorResponseException.class);
        ErrorResponse errorResponse = mock(ErrorResponse.class);
        when(errorResponse.code()).thenReturn("AccessDenied");
        when(ex.errorResponse()).thenReturn(errorResponse);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(ex);

        assertThrows(BusinessException.class, () -> minioService.downloadFile(BUCKET, PATH));
    }

    @Test
    void testDownloadFile_GenericException() throws Exception {
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new RuntimeException("Read error"));

        assertThrows(BusinessException.class, () -> minioService.downloadFile(BUCKET, PATH));
    }

    @Test
    void testDoesObjectExist_True() throws Exception {
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(null);
        assertTrue(minioService.doesObjectExist(BUCKET, PATH));
    }

    @Test
    void testDoesObjectExist_False() throws Exception {
        when(minioClient.statObject(any(StatObjectArgs.class))).thenThrow(new RuntimeException("Not found"));
        assertFalse(minioService.doesObjectExist(BUCKET, PATH));
    }
}
