package com.freightfox.filestorage.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.freightfox.filestorage.dto.FileMetaDataDto;

public interface FileStorageService {

	public String upload(String userName, MultipartFile file) ;
	
	public List<FileMetaDataDto> searchFile(String userId, String filename);
	
	public byte[] downloadFile(String userId, String filename);
}
