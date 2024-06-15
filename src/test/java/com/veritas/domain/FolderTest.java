package com.veritas.domain;

import static com.veritas.domain.FileTestSamples.*;
import static com.veritas.domain.FolderTestSamples.*;
import static com.veritas.domain.PermissionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.veritas.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FolderTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Folder.class);
        Folder folder1 = getFolderSample1();
        Folder folder2 = new Folder();
        assertThat(folder1).isNotEqualTo(folder2);

        folder2.setId(folder1.getId());
        assertThat(folder1).isEqualTo(folder2);

        folder2 = getFolderSample2();
        assertThat(folder1).isNotEqualTo(folder2);
    }

    @Test
    void permissionTest() {
        Folder folder = getFolderRandomSampleGenerator();
        Permission permissionBack = getPermissionRandomSampleGenerator();

        folder.addPermission(permissionBack);
        assertThat(folder.getPermissions()).containsOnly(permissionBack);
        assertThat(permissionBack.getFolder()).isEqualTo(folder);

        folder.removePermission(permissionBack);
        assertThat(folder.getPermissions()).doesNotContain(permissionBack);
        assertThat(permissionBack.getFolder()).isNull();

        folder.permissions(new HashSet<>(Set.of(permissionBack)));
        assertThat(folder.getPermissions()).containsOnly(permissionBack);
        assertThat(permissionBack.getFolder()).isEqualTo(folder);

        folder.setPermissions(new HashSet<>());
        assertThat(folder.getPermissions()).doesNotContain(permissionBack);
        assertThat(permissionBack.getFolder()).isNull();
    }

    @Test
    void fileTest() {
        Folder folder = getFolderRandomSampleGenerator();
        File fileBack = getFileRandomSampleGenerator();

        folder.setFile(fileBack);
        assertThat(folder.getFile()).isEqualTo(fileBack);

        folder.file(null);
        assertThat(folder.getFile()).isNull();
    }

    @Test
    void filesTest() {
        Folder folder = getFolderRandomSampleGenerator();
        File fileBack = getFileRandomSampleGenerator();

        folder.addFiles(fileBack);
        assertThat(folder.getFiles()).containsOnly(fileBack);
        assertThat(fileBack.getFolders()).containsOnly(folder);

        folder.removeFiles(fileBack);
        assertThat(folder.getFiles()).doesNotContain(fileBack);
        assertThat(fileBack.getFolders()).doesNotContain(folder);

        folder.files(new HashSet<>(Set.of(fileBack)));
        assertThat(folder.getFiles()).containsOnly(fileBack);
        assertThat(fileBack.getFolders()).containsOnly(folder);

        folder.setFiles(new HashSet<>());
        assertThat(folder.getFiles()).doesNotContain(fileBack);
        assertThat(fileBack.getFolders()).doesNotContain(folder);
    }
}
