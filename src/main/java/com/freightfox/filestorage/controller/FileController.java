package com.freightfox.filestorage.controller;

import org.springframework.http.MediaType;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.freightfox.filestorage.response.ApiResponse;
import com.freightfox.filestorage.service.FileStorageService;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {
	
	private final FileStorageService fileService;
	
	public FileController(FileStorageService fileService) {
		this.fileService = fileService;
	}

	@PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> uploadFile(
			@RequestParam String userName,
			@RequestPart("file") MultipartFile file){
		String key = fileService.upload(userName, file);
		return new ResponseEntity<>(new ApiResponse<>("File uploaded successfully",key,HttpStatus.OK),HttpStatus.OK);
	}
}
