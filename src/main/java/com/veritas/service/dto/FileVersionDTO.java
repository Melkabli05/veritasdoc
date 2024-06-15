package com.veritas.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the {@link com.veritas.domain.FileVersion} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FileVersionDTO implements Serializable {

    private Long id;

    @NotNull
    private String fileId;

    @NotNull
    private Integer versionNumber;

    @NotNull
    private String objectName;

    private ZonedDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileVersionDTO)) {
            return false;
        }

        FileVersionDTO fileVersionDTO = (FileVersionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, fileVersionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FileVersionDTO{" +
            "id=" + getId() +
            ", fileId='" + getFileId() + "'" +
            ", versionNumber=" + getVersionNumber() +
            ", objectName='" + getObjectName() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
