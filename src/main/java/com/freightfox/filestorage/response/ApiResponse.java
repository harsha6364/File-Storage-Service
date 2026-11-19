package com.freightfox.filestorage.response;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ApiResponse<T> {

	private String message;
	
	private T data;
	
	private HttpStatus httpStatus;
}
