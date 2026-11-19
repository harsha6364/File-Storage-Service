
package com.freightfox.filestorage.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.freightfox.filestorage.exception.AppException;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceImplUploadTest {

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

    // 1. Successful upload
    @Test
    void testUploadFile_Success() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.txt",
                "text/plain",
                "Hello FreightFox".getBytes()
        );

        String result = fileStorageService.upload("sandy", file);

        assertEquals("sandy/sample.txt", result);

        verify(s3Client).putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        );
    }

    // 2. Empty username
    @Test
    void testUploadFile_EmptyUsername() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.txt",
                "text/plain",
                "Hello".getBytes()
        );

        AppException exception = assertThrows(
                AppException.class,
                () -> fileStorageService.upload("", file)
        );

        assertEquals("User name cannot be empty", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());

        verifyNoInteractions(s3Client);
    }

    // 3. Empty file
    @Test
    void testUploadFile_EmptyFile() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.txt",
                "text/plain",
                new byte[0]
        );

        AppException exception = assertThrows(
                AppException.class,
                () -> fileStorageService.upload("sandy", file)
        );

        assertEquals("File cannot be empty", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());

        verifyNoInteractions(s3Client);
    }

    // 4. Filename sanitization
    @Test
    void testUploadFile_RejectsInvalidFilename() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../sample.txt",
                "text/plain",
                "Hello".getBytes()
        );

        AppException exception = assertThrows(
                AppException.class,
                () -> fileStorageService.upload("sandy", file)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());

        verify(s3Client, never()).putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        );
    }
    // 5. S3 service failure
    @Test
    void testUploadFile_S3Failure() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.txt",
                "text/plain",
                "Hello".getBytes()
        );

        S3Exception s3Exception = org.mockito.Mockito.mock(S3Exception.class);

        AwsErrorDetails errorDetails = AwsErrorDetails.builder()
                .errorMessage("Internal server error")
                .build();

        when(s3Exception.awsErrorDetails()).thenReturn(errorDetails);

        when(s3Client.putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        )).thenThrow(s3Exception);

        AppException exception = assertThrows(
                AppException.class,
                () -> fileStorageService.upload("sandy", file)
        );

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getHttpStatus());
    }

    // 6. AWS communication failure
    @Test
    void testUploadFile_AwsCommunicationFailure() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.txt",
                "text/plain",
                "Hello".getBytes()
        );

        when(s3Client.putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        )).thenThrow(
                SdkClientException.create("Connection failed")
        );

        AppException exception = assertThrows(
                AppException.class,
                () -> fileStorageService.upload("sandy", file)
        );

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getHttpStatus()
        );
    }

    // 7. File input stream failure
    @Test
    void testUploadFile_InputStreamFailure() throws IOException {

        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("sample.txt");

        when(file.getInputStream()).thenThrow(
                new IOException("Unable to read file")
        );

        AppException exception = assertThrows(
                AppException.class,
                () -> fileStorageService.upload("sandy", file)
        );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getHttpStatus()
        );

        verifyNoInteractions(s3Client);
    }
}
