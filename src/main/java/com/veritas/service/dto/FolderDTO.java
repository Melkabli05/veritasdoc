package com.veritas.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link com.veritas.domain.Folder} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FolderDTO implements Serializable {

    private Long id;

    @NotNull
    private String name;

    private String parentFolderId;

    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;

    private Boolean isActive;

    private FileDTO file;

    private Set<FileDTO> files = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentFolderId() {
        return parentFolderId;
    }

    public void setParentFolderId(String parentFolderId) {
        this.parentFolderId = parentFolderId;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public FileDTO getFile() {
        return file;
    }

    public void setFile(FileDTO file) {
        this.file = file;
    }

    public Set<FileDTO> getFiles() {
        return files;
    }

    public void setFiles(Set<FileDTO> files) {
        this.files = files;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FolderDTO)) {
            return false;
        }

        FolderDTO folderDTO = (FolderDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, folderDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FolderDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", parentFolderId='" + getParentFolderId() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", isActive='" + getIsActive() + "'" +
            ", file=" + getFile() +
            ", files=" + getFiles() +
            "}";
    }
}
