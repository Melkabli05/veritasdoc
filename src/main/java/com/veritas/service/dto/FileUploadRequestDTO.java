package com.veritas.service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public record FileUploadRequestDTO(
    @NotNull @NotEmpty String bucketName,
    @NotNull @NotEmpty Set<@NotNull MultipartFile> files,
    @NotNull @NotEmpty String folder
) {
}
