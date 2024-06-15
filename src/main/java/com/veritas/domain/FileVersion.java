package com.veritas.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A FileVersion.
 */
@Entity
@Table(name = "file_version")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "fileversion")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FileVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "file_id", nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String fileId;

    @NotNull
    @Column(name = "version_number", nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Integer)
    private Integer versionNumber;

    @NotNull
    @Column(name = "object_name", nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String objectName;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @JsonIgnoreProperties(value = { "fileVersion", "metadata", "fileTags", "fileFolders", "tags", "folders" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "fileVersion")
    @org.springframework.data.annotation.Transient
    private File file;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public FileVersion id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileId() {
        return this.fileId;
    }

    public FileVersion fileId(String fileId) {
        this.setFileId(fileId);
        return this;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Integer getVersionNumber() {
        return this.versionNumber;
    }

    public FileVersion versionNumber(Integer versionNumber) {
        this.setVersionNumber(versionNumber);
        return this;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getObjectName() {
        return this.objectName;
    }

    public FileVersion objectName(String objectName) {
        this.setObjectName(objectName);
        return this;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public FileVersion createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public File getFile() {
        return this.file;
    }

    public void setFile(File file) {
        if (this.file != null) {
            this.file.setFileVersion(null);
        }
        if (file != null) {
            file.setFileVersion(this);
        }
        this.file = file;
    }

    public FileVersion file(File file) {
        this.setFile(file);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileVersion)) {
            return false;
        }
        return getId() != null && getId().equals(((FileVersion) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FileVersion{" +
            "id=" + getId() +
            ", fileId='" + getFileId() + "'" +
            ", versionNumber=" + getVersionNumber() +
            ", objectName='" + getObjectName() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
