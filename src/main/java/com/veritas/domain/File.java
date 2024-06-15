package com.veritas.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A File.
 */
@Entity
@Table(name = "file")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "file")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class File implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "filename", nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String filename;

    @NotNull
    @Column(name = "bucket_name", nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String bucketName;

    @NotNull
    @Column(name = "object_name", nullable = false, unique = true)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String objectName;

    @Column(name = "content_type")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "uploaded_by")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String uploadedBy;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "is_active")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Boolean)
    private Boolean isActive;

    @JsonIgnoreProperties(value = { "file" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private FileVersion fileVersion;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "file")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "file" }, allowSetters = true)
    private Set<Metadata> metadata = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "file")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "file", "files" }, allowSetters = true)
    private Set<Tag> fileTags = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "file")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "permissions", "file", "files" }, allowSetters = true)
    private Set<Folder> fileFolders = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "rel_file__tags", joinColumns = @JoinColumn(name = "file_id"), inverseJoinColumns = @JoinColumn(name = "tags_id"))
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "file", "files" }, allowSetters = true)
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_file__folders",
        joinColumns = @JoinColumn(name = "file_id"),
        inverseJoinColumns = @JoinColumn(name = "folders_id")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "permissions", "file", "files" }, allowSetters = true)
    private Set<Folder> folders = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public File id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return this.filename;
    }

    public File filename(String filename) {
        this.setFilename(filename);
        return this;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getBucketName() {
        return this.bucketName;
    }

    public File bucketName(String bucketName) {
        this.setBucketName(bucketName);
        return this;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectName() {
        return this.objectName;
    }

    public File objectName(String objectName) {
        this.setObjectName(objectName);
        return this;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getContentType() {
        return this.contentType;
    }

    public File contentType(String contentType) {
        this.setContentType(contentType);
        return this;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return this.fileSize;
    }

    public File fileSize(Long fileSize) {
        this.setFileSize(fileSize);
        return this;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getUploadedBy() {
        return this.uploadedBy;
    }

    public File uploadedBy(String uploadedBy) {
        this.setUploadedBy(uploadedBy);
        return this;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public File createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public File updatedAt(ZonedDateTime updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public File isActive(Boolean isActive) {
        this.setIsActive(isActive);
        return this;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public FileVersion getFileVersion() {
        return this.fileVersion;
    }

    public void setFileVersion(FileVersion fileVersion) {
        this.fileVersion = fileVersion;
    }

    public File fileVersion(FileVersion fileVersion) {
        this.setFileVersion(fileVersion);
        return this;
    }

    public Set<Metadata> getMetadata() {
        return this.metadata;
    }

    public void setMetadata(Set<Metadata> metadata) {
        if (this.metadata != null) {
            this.metadata.forEach(i -> i.setFile(null));
        }
        if (metadata != null) {
            metadata.forEach(i -> i.setFile(this));
        }
        this.metadata = metadata;
    }

    public File metadata(Set<Metadata> metadata) {
        this.setMetadata(metadata);
        return this;
    }

    public File addMetadata(Metadata metadata) {
        this.metadata.add(metadata);
        metadata.setFile(this);
        return this;
    }

    public File removeMetadata(Metadata metadata) {
        this.metadata.remove(metadata);
        metadata.setFile(null);
        return this;
    }

    public Set<Tag> getFileTags() {
        return this.fileTags;
    }

    public void setFileTags(Set<Tag> tags) {
        if (this.fileTags != null) {
            this.fileTags.forEach(i -> i.setFile(null));
        }
        if (tags != null) {
            tags.forEach(i -> i.setFile(this));
        }
        this.fileTags = tags;
    }

    public File fileTags(Set<Tag> tags) {
        this.setFileTags(tags);
        return this;
    }

    public File addFileTag(Tag tag) {
        this.fileTags.add(tag);
        tag.setFile(this);
        return this;
    }

    public File removeFileTag(Tag tag) {
        this.fileTags.remove(tag);
        tag.setFile(null);
        return this;
    }

    public Set<Folder> getFileFolders() {
        return this.fileFolders;
    }

    public void setFileFolders(Set<Folder> folders) {
        if (this.fileFolders != null) {
            this.fileFolders.forEach(i -> i.setFile(null));
        }
        if (folders != null) {
            folders.forEach(i -> i.setFile(this));
        }
        this.fileFolders = folders;
    }

    public File fileFolders(Set<Folder> folders) {
        this.setFileFolders(folders);
        return this;
    }

    public File addFileFolder(Folder folder) {
        this.fileFolders.add(folder);
        folder.setFile(this);
        return this;
    }

    public File removeFileFolder(Folder folder) {
        this.fileFolders.remove(folder);
        folder.setFile(null);
        return this;
    }

    public Set<Tag> getTags() {
        return this.tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public File tags(Set<Tag> tags) {
        this.setTags(tags);
        return this;
    }

    public File addTags(Tag tag) {
        this.tags.add(tag);
        return this;
    }

    public File removeTags(Tag tag) {
        this.tags.remove(tag);
        return this;
    }

    public Set<Folder> getFolders() {
        return this.folders;
    }

    public void setFolders(Set<Folder> folders) {
        this.folders = folders;
    }

    public File folders(Set<Folder> folders) {
        this.setFolders(folders);
        return this;
    }

    public File addFolders(Folder folder) {
        this.folders.add(folder);
        return this;
    }

    public File removeFolders(Folder folder) {
        this.folders.remove(folder);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof File)) {
            return false;
        }
        return getId() != null && getId().equals(((File) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "File{" +
            "id=" + getId() +
            ", filename='" + getFilename() + "'" +
            ", bucketName='" + getBucketName() + "'" +
            ", objectName='" + getObjectName() + "'" +
            ", contentType='" + getContentType() + "'" +
            ", fileSize=" + getFileSize() +
            ", uploadedBy='" + getUploadedBy() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", isActive='" + getIsActive() + "'" +
            "}";
    }
}
