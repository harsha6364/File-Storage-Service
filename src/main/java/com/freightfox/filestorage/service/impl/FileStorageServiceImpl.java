package com.freightfox.filestorage.service.impl;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.freightfox.filestorage.dto.FileMetaDataDto;
import com.freightfox.filestorage.exception.AppException;
import com.freightfox.filestorage.service.FileStorageService;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class FileStorageServiceImpl implements FileStorageService{

	private final S3Client s3Client;
	
	@Value("${aws.s3.bucket-name}")
	private String bucketName;
	
	public FileStorageServiceImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

	@Override
	public String upload(String userName, MultipartFile file) {
		
		if(userName == null || userName.isBlank()) {
			throw new AppException("User name cannot be empty", HttpStatus.BAD_REQUEST);
		}
		
		if(file == null || file.isEmpty()) {
			throw new AppException("File cannot be empty",HttpStatus.BAD_REQUEST);
		}
		
		 String originalFilename = file.getOriginalFilename();
		
		if(originalFilename == null || originalFilename.isBlank()) {
			throw new AppException("Invalid filename", HttpStatus.BAD_REQUEST);
		}
		
		originalFilename = originalFilename
		        .replace("/", "_")
		        .replace("\\", "_")
		        .trim();
		
		String key = userName + "/" + originalFilename;
		
		try {
			
			PutObjectRequest request = PutObjectRequest.builder()
					.bucket(bucketName)
					.key(key)
					.contentType(file.getContentType())
					.build();
			
			s3Client.putObject(request,
					RequestBody.fromInputStream(
							file.getInputStream(), 
							file.getSize()
					)
			);
			
			return key;
			
		} catch (S3Exception e) {
			
			throw new AppException("Failed to upload file to S3 :" + e.awsErrorDetails().errorMessage(), HttpStatus.BAD_GATEWAY);
			
		} catch (SdkClientException e) {

	        throw new AppException(
	                "Unable to communicate with AWS S3",
	                HttpStatus.SERVICE_UNAVAILABLE
	        );
	    } catch (IOException e) {
	        throw new AppException(
	                "Failed to read uploaded file",
	                HttpStatus.INTERNAL_SERVER_ERROR
	            );
	        }
		
	}

	@Override
	public List<FileMetaDataDto> searchFile(String userId, String filename) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] downloadFile(String userId, String filename) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
}
