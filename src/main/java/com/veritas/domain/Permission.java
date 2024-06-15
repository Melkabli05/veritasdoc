package com.veritas.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Permission.
 */
@Entity
@Table(name = "permission")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "permission")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Permission implements Serializable {

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
    @Column(name = "user_id", nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String userId;

    @NotNull
    @Column(name = "permission", nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String permission;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "permissions", "file", "files" }, allowSetters = true)
    private Folder folder;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Permission id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileId() {
        return this.fileId;
    }

    public Permission fileId(String fileId) {
        this.setFileId(fileId);
        return this;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getUserId() {
        return this.userId;
    }

    public Permission userId(String userId) {
        this.setUserId(userId);
        return this;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPermission() {
        return this.permission;
    }

    public Permission permission(String permission) {
        this.setPermission(permission);
        return this;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public Permission createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public Permission updatedAt(ZonedDateTime updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Folder getFolder() {
        return this.folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public Permission folder(Folder folder) {
        this.setFolder(folder);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Permission)) {
            return false;
        }
        return getId() != null && getId().equals(((Permission) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Permission{" +
            "id=" + getId() +
            ", fileId='" + getFileId() + "'" +
            ", userId='" + getUserId() + "'" +
            ", permission='" + getPermission() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
}
