package com.veritas.service;

import io.minio.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    private final MinioClient minioClient;

    public StorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public StatObjectResponse uploadFile(MultipartFile file, String bucketName, String folder) {
        try {
            var fileName = String.format("%s/%s", folder, file.getOriginalFilename());
            minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());

            return getObjectState(bucketName, fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void downloadFile(String bucketName, String fileName, String filePath) {
        try {
            minioClient.downloadObject(DownloadObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .filename(filePath)
                .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(String bucketName, String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createBucket(String bucketName) {
        try {
            if (minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                return;
            }
            minioClient.makeBucket(MakeBucketArgs.builder()
                .bucket(bucketName)
                .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public StatObjectResponse getObjectState(String bucketName, String fileName) {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .build());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public String getFileUrl(String bucketName, String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .build());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
