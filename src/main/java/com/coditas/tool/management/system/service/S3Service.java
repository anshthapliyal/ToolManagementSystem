package com.coditas.tool.management.system.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface S3Service {

    public Map<String,String> uploadPhoto(MultipartFile file, String key);
}