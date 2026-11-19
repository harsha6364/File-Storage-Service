package com.freightfox.filestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FileMetaDataDto {

	private String fileName;
	
	private String key;
	
	private Long fileSize;
}
