package com.veritas.domain;

import static com.veritas.domain.FileTestSamples.*;
import static com.veritas.domain.FileVersionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.veritas.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class FileVersionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(FileVersion.class);
        FileVersion fileVersion1 = getFileVersionSample1();
        FileVersion fileVersion2 = new FileVersion();
        assertThat(fileVersion1).isNotEqualTo(fileVersion2);

        fileVersion2.setId(fileVersion1.getId());
        assertThat(fileVersion1).isEqualTo(fileVersion2);

        fileVersion2 = getFileVersionSample2();
        assertThat(fileVersion1).isNotEqualTo(fileVersion2);
    }

    @Test
    void fileTest() {
        FileVersion fileVersion = getFileVersionRandomSampleGenerator();
        File fileBack = getFileRandomSampleGenerator();

        fileVersion.setFile(fileBack);
        assertThat(fileVersion.getFile()).isEqualTo(fileBack);
        assertThat(fileBack.getFileVersion()).isEqualTo(fileVersion);

        fileVersion.file(null);
        assertThat(fileVersion.getFile()).isNull();
        assertThat(fileBack.getFileVersion()).isNull();
    }
}
