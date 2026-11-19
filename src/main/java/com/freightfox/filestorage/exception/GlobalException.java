package com.freightfox.filestorage.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.freightfox.filestorage.response.ApiResponse;

@RestControllerAdvice
public class GlobalException {

	@ExceptionHandler(AppException.class)
	public ResponseEntity<?> handleException(AppException exception){
		return new ResponseEntity<>(new ApiResponse<>(exception.getMessage(), null, exception.getHttpStatus()),exception.getHttpStatus());
	}
}
