package com.veritas.domain;

import static com.veritas.domain.FolderTestSamples.*;
import static com.veritas.domain.PermissionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.veritas.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PermissionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Permission.class);
        Permission permission1 = getPermissionSample1();
        Permission permission2 = new Permission();
        assertThat(permission1).isNotEqualTo(permission2);

        permission2.setId(permission1.getId());
        assertThat(permission1).isEqualTo(permission2);

        permission2 = getPermissionSample2();
        assertThat(permission1).isNotEqualTo(permission2);
    }

    @Test
    void folderTest() {
        Permission permission = getPermissionRandomSampleGenerator();
        Folder folderBack = getFolderRandomSampleGenerator();

        permission.setFolder(folderBack);
        assertThat(permission.getFolder()).isEqualTo(folderBack);

        permission.folder(null);
        assertThat(permission.getFolder()).isNull();
    }
}
