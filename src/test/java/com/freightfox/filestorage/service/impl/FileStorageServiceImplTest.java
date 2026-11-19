package com.freightfox.filestorage.service.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import com.freightfox.filestorage.exception.AppException;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(MockitoExtension.class)
public class FileStorageServiceImplTest {

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
	    void testDownloadFile_Success() {

	        // Arrange
	        byte[] expectedData = "Hello FreightFox".getBytes();

	        ResponseBytes<GetObjectResponse> mockResponse =
	                ResponseBytes.fromByteArray(
	                        GetObjectResponse.builder().build(),
	                        expectedData
	                );

	        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
	                .thenReturn(mockResponse);

	        // Act
	        byte[] actualData = fileStorageService.downloadFile(
	                "sandy",
	                "sample.txt"
	        );

	        // Assert
	        assertArrayEquals(expectedData, actualData);
	    }
	    
	    @Test
	    void testDownloadFile_EmptyUsername() {

	        AppException exception = assertThrows(
	                AppException.class,
	                () -> fileStorageService.downloadFile("", "sample.txt")
	        );

	        assertEquals("User name cannot be empty", exception.getMessage());
	        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
	    }
	    
	    @Test
	    void testDownloadFile_EmptyFilename() {

	        AppException exception = assertThrows(
	                AppException.class,
	                () -> fileStorageService.downloadFile("sandy", "")
	        );

	        assertEquals("File name cannot be empty", exception.getMessage());
	        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
	    }
	    
	    @Test
	    void testDownloadFile_InvalidUsername() {

	        AppException exception = assertThrows(
	                AppException.class,
	                () -> fileStorageService.downloadFile(
	                        "sandy/john",
	                        "sample.txt"
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
	    void testDownloadFile_InvalidFilename() {

	        AppException exception = assertThrows(
	                AppException.class,
	                () -> fileStorageService.downloadFile(
	                        "sandy",
	                        "folder/sample.txt"
	                )
	        );

	        assertEquals("Invalid filename", exception.getMessage());

	        assertEquals(
	                HttpStatus.BAD_REQUEST,
	                exception.getHttpStatus()
	        );

	        verifyNoInteractions(s3Client);
	    }
	    
	    @Test
	    void testDownloadFile_FileNotFound() {

	        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
	                .thenThrow(
	                        NoSuchKeyException.builder()
	                                .message("The specified key does not exist")
	                                .statusCode(404)
	                                .build()
	                );

	        AppException exception = assertThrows(
	                AppException.class,
	                () -> fileStorageService.downloadFile("sandy", "missing.txt")
	        );

	        assertEquals("File not found", exception.getMessage());

	        assertEquals(
	                HttpStatus.NOT_FOUND,
	                exception.getHttpStatus()
	        );
	    }
	    
	    @Test
	    void testDownloadFile_S3Failure() {

	        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
	                .thenThrow(
	                        S3Exception.builder()
	                                .message("Internal server error")
	                                .statusCode(500)
	                                .build()
	                );

	        AppException exception = assertThrows(
	                AppException.class,
	                () -> fileStorageService.downloadFile("sandy", "sample.txt")
	        );

	        assertEquals(
	                "Failed to download file from S3",
	                exception.getMessage()
	        );

	        assertEquals(
	                HttpStatus.BAD_GATEWAY,
	                exception.getHttpStatus()
	        );
	    }
	    
	    @Test
	    void testDownloadFile_AwsCommunicationFailure() {

	        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
	                .thenThrow(SdkClientException.create("Connection failed"));

	        AppException exception = assertThrows(
	                AppException.class,
	                () -> fileStorageService.downloadFile("sandy", "sample.txt")
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
