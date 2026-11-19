package com.freightfox.filestorage.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.freightfox.filestorage.dto.FileMetaDataDto;
import com.freightfox.filestorage.exception.AppException;
import com.freightfox.filestorage.service.FileStorageService;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
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
		
		userName = userName.trim();
		
		if (userName.contains("/") || userName.contains("\\")) {
		    throw new AppException(
		            "Invalid username",
		            HttpStatus.BAD_REQUEST
		    );
		}
		
		if(file == null || file.isEmpty()) {
			throw new AppException("File cannot be empty",HttpStatus.BAD_REQUEST);
		}
		
		
		
		String originalFilename = file.getOriginalFilename();
		
		if(originalFilename == null || originalFilename.isBlank()) {
			throw new AppException("Invalid filename", HttpStatus.BAD_REQUEST);
		}
		
		originalFilename = originalFilename.trim();

		if (originalFilename.contains("/") ||originalFilename.contains("\\")) {

		    throw new AppException("Invalid filename",HttpStatus.BAD_REQUEST);
		}
		
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

		    String errorMessage = e.awsErrorDetails() != null
		            ? e.awsErrorDetails().errorMessage()
		            : "Unknown S3 error";

		    throw new AppException(
		            "Failed to upload file to S3: " + errorMessage,
		            HttpStatus.BAD_GATEWAY
		    );
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
	public List<FileMetaDataDto> searchFile(String userName, String fileName) {
		
		if (userName == null || userName.isBlank()) {
		    throw new AppException("Username cannot be empty",HttpStatus.BAD_REQUEST);
		}
		
		if (userName.contains("/") || userName.contains("\\")) {
		    throw new AppException(
		            "Invalid username",
		            HttpStatus.BAD_REQUEST
		    );
		}

		
		if(fileName == null || fileName.isBlank()) {
			throw new AppException("File name search term cannot be empty", HttpStatus.BAD_REQUEST);
		}
		
		String trimmedUserName = userName.trim();
		String searchTerm = fileName.trim();
				
		List<FileMetaDataDto> files = new ArrayList<>();

		String continuationToken = null;
		
		try {
			while(true) {
				ListObjectsV2Request request = ListObjectsV2Request.builder()
						.bucket(bucketName)
						.prefix(trimmedUserName + "/")
						.continuationToken(continuationToken)
						.build();
				
				ListObjectsV2Response response = s3Client.listObjectsV2(request);
				
				List<FileMetaDataDto> pageFiles = response.contents().stream()
						.filter(object ->{
							String objectFilename = object.key()
									.substring(object.key().lastIndexOf("/")+1);
							
							return objectFilename.toLowerCase(Locale.ROOT)
									.contains(searchTerm.toLowerCase(Locale.ROOT));
						})
						.map(object ->new FileMetaDataDto(
								object.key().substring(object.key().lastIndexOf("/") + 1),
								object.key(),
								object.size()
								))
						.toList();
				files.addAll(pageFiles);
				
				if(!response.isTruncated()) {
					break;
				}
				
				continuationToken = response.nextContinuationToken();
			}
		} catch (S3Exception e) {
		    throw new AppException("Failed to search files in S3",HttpStatus.BAD_GATEWAY);

		} catch (SdkClientException e) {
		    throw new AppException("Unable to communicate with AWS S3",HttpStatus.SERVICE_UNAVAILABLE);
		}
		
		
		return files;
	}

	@Override
	public byte[] downloadFile(String userName, String fileName) {
		
		if(userName == null || userName.isBlank()) {
			throw new AppException("User name cannot be empty",HttpStatus.BAD_REQUEST);
		}
		
		if (userName.contains("/") || userName.contains("\\")) {
		    throw new AppException("Invalid username",HttpStatus.BAD_REQUEST);
		}
		
		if(fileName == null || fileName.isBlank()) {
			throw new AppException("File name cannot be empty",HttpStatus.BAD_REQUEST);
		}
		
		if (fileName.contains("/") || fileName.contains("\\")) {
		    throw new AppException("Invalid filename",HttpStatus.BAD_REQUEST);
		}
		
		userName = userName.trim();
		fileName = fileName.trim();
		
		try {
			
			GetObjectRequest request = GetObjectRequest.builder()
					.bucket(bucketName)
					.key(userName+"/"+fileName)
					.build();
			
			ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);
			
			return response.asByteArray();
			
		} catch (S3Exception e) {

		    if (e.statusCode() == 404) {
		        throw new AppException(
		                "File not found",
		                HttpStatus.NOT_FOUND
		        );
		    }

		    throw new AppException(
		            "Failed to download file from S3",
		            HttpStatus.BAD_GATEWAY
		    );

		} catch (SdkClientException e) {
		    throw new AppException(
		            "Unable to communicate with AWS S3",
		            HttpStatus.SERVICE_UNAVAILABLE
		    );
		}
		
		
	}
	
	
	
	
}
