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
 * A Folder.
 */
@Entity
@Table(name = "folder")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "folder")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Folder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String name;

    @Column(name = "parent_folder_id")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String parentFolderId;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "is_active")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Boolean)
    private Boolean isActive;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "folder")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "folder" }, allowSetters = true)
    private Set<Permission> permissions = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "fileVersion", "metadata", "fileTags", "fileFolders", "tags", "folders" }, allowSetters = true)
    private File file;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "folders")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "fileVersion", "metadata", "fileTags", "fileFolders", "tags", "folders" }, allowSetters = true)
    private Set<File> files = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Folder id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Folder name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentFolderId() {
        return this.parentFolderId;
    }

    public Folder parentFolderId(String parentFolderId) {
        this.setParentFolderId(parentFolderId);
        return this;
    }

    public void setParentFolderId(String parentFolderId) {
        this.parentFolderId = parentFolderId;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public Folder createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public Folder updatedAt(ZonedDateTime updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public Folder isActive(Boolean isActive) {
        this.setIsActive(isActive);
        return this;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Set<Permission> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        if (this.permissions != null) {
            this.permissions.forEach(i -> i.setFolder(null));
        }
        if (permissions != null) {
            permissions.forEach(i -> i.setFolder(this));
        }
        this.permissions = permissions;
    }

    public Folder permissions(Set<Permission> permissions) {
        this.setPermissions(permissions);
        return this;
    }

    public Folder addPermission(Permission permission) {
        this.permissions.add(permission);
        permission.setFolder(this);
        return this;
    }

    public Folder removePermission(Permission permission) {
        this.permissions.remove(permission);
        permission.setFolder(null);
        return this;
    }

    public File getFile() {
        return this.file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Folder file(File file) {
        this.setFile(file);
        return this;
    }

    public Set<File> getFiles() {
        return this.files;
    }

    public void setFiles(Set<File> files) {
        if (this.files != null) {
            this.files.forEach(i -> i.removeFolders(this));
        }
        if (files != null) {
            files.forEach(i -> i.addFolders(this));
        }
        this.files = files;
    }

    public Folder files(Set<File> files) {
        this.setFiles(files);
        return this;
    }

    public Folder addFiles(File file) {
        this.files.add(file);
        file.getFolders().add(this);
        return this;
    }

    public Folder removeFiles(File file) {
        this.files.remove(file);
        file.getFolders().remove(this);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Folder)) {
            return false;
        }
        return getId() != null && getId().equals(((Folder) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Folder{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", parentFolderId='" + getParentFolderId() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", isActive='" + getIsActive() + "'" +
            "}";
    }
}
