package com.freightfox.filestorage.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import com.freightfox.filestorage.dto.FileMetaDataDto;
import com.freightfox.filestorage.exception.AppException;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceImplSearchTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private FileStorageServiceImpl fileStorageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                fileStorageService,
                "bucketName",
                "freightfox-file-storage-harsha"
        );
    }
    
    @Test
    void testSearchFile_Success() {

        // Arrange
        S3Object object1 = S3Object.builder()
                .key("sandy/logistics.pdf")
                .size(1024L)
                .build();

        S3Object object2 = S3Object.builder()
                .key("sandy/logistics-report.docx")
                .size(2048L)
                .build();

        S3Object object3 = S3Object.builder()
                .key("sandy/profile.jpg")
                .size(512L)
                .build();

        ListObjectsV2Response mockResponse = ListObjectsV2Response.builder()
                .contents(object1, object2, object3)
                .isTruncated(false)
                .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(mockResponse);

        // Act
        List<FileMetaDataDto> result =
                fileStorageService.searchFile("sandy", "logistics");

        // Assert
        assertEquals(2, result.size());
        assertEquals("logistics.pdf", result.get(0).getFileName());
        assertEquals("sandy/logistics.pdf", result.get(0).getKey());
        assertEquals(1024L, result.get(0).getFileSize());
    }
    
    @Test
    void testSearchFile_NoMatchingFiles() {

        S3Object object = S3Object.builder()
                .key("sandy/profile.jpg")
                .size(512L)
                .build();

        ListObjectsV2Response mockResponse = ListObjectsV2Response.builder()
                .contents(object)
                .isTruncated(false)
                .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(mockResponse);

        List<FileMetaDataDto> result =
                fileStorageService.searchFile("sandy", "logistics");

        assertTrue(result.isEmpty());
    }
    
    @Test
    void testSearchFile_EmptyUsername() {

        AppException exception = assertThrows(
                AppException.class,
                () -> fileStorageService.searchFile("", "logistics")
        );

        assertEquals("Username cannot be empty", exception.getMessage());

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getHttpStatus()
        );

        verifyNoInteractions(s3Client);
    }
    
    @Test
    void testSearchFile_InvalidUsername() {

        AppException exception = assertThrows(
                AppException.class,
                () -> fileStorageService.searchFile(
                        "sandy/john",
                        "logistics"
                )
        );

        assertEquals("Invalid username", exception.getMessage());

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getHttpStatus()
        );

        verifyNoInteractions(s3Client);
    }
    
    @Test
    void testSearchFile_EmptyFilename() {

        AppException exception = assertThrows(
                AppException.class,
                () -> fileStorageService.searchFile("sandy", "")
        );

        assertEquals(
                "File name search term cannot be empty",
                exception.getMessage()
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getHttpStatus()
        );

        verifyNoInteractions(s3Client);
    }
    
    @Test
    void testSearchFile_Pagination() {

        S3Object object1 = S3Object.builder()
                .key("sandy/logistics.pdf")
                .size(1024L)
                .build();

        S3Object object2 = S3Object.builder()
                .key("sandy/logistics-report.pdf")
                .size(2048L)
                .build();

        ListObjectsV2Response firstPage = ListObjectsV2Response.builder()
                .contents(object1)
                .isTruncated(true)
                .nextContinuationToken("next-page-token")
                .build();

        ListObjectsV2Response secondPage = ListObjectsV2Response.builder()
                .contents(object2)
                .isTruncated(false)
                .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(firstPage)
                .thenReturn(secondPage);

        List<FileMetaDataDto> result =
                fileStorageService.searchFile("sandy", "logistics");

        assertEquals(2, result.size());

        verify(s3Client, times(2))
                .listObjectsV2(any(ListObjectsV2Request.class));
    }
    
    @Test
    void testSearchFile_S3Failure() {

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenThrow(
                        S3Exception.builder()
                                .message("Internal server error")
                                .statusCode(500)
                                .build()
                );

        AppException exception = assertThrows(
                AppException.class,
                () -> fileStorageService.searchFile("sandy", "logistics")
        );

        assertEquals(
                "Failed to search files in S3",
                exception.getMessage()
        );

        assertEquals(
                HttpStatus.BAD_GATEWAY,
                exception.getHttpStatus()
        );
    }
    
    @Test
    void testSearchFile_AwsCommunicationFailure() {

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenThrow(SdkClientException.create("Connection failed"));

        AppException exception = assertThrows(
                AppException.class,
                () -> fileStorageService.searchFile("sandy", "logistics")
        );

        assertEquals(
                "Unable to communicate with AWS S3",
                exception.getMessage()
        );

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getHttpStatus()
        );
    }
}
