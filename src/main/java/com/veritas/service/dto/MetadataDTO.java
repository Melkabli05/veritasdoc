package com.veritas.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the {@link com.veritas.domain.Metadata} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MetadataDTO implements Serializable {

    private Long id;

    @NotNull
    private String fileId;

    @NotNull
    private String key;

    @NotNull
    private String value;

    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;

    private FileDTO file;

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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public FileDTO getFile() {
        return file;
    }

    public void setFile(FileDTO file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MetadataDTO)) {
            return false;
        }

        MetadataDTO metadataDTO = (MetadataDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, metadataDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MetadataDTO{" +
            "id=" + getId() +
            ", fileId='" + getFileId() + "'" +
            ", key='" + getKey() + "'" +
            ", value='" + getValue() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", file=" + getFile() +
            "}";
    }
}
