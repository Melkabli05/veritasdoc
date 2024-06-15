package com.veritas.domain;

import static com.veritas.domain.FileTestSamples.*;
import static com.veritas.domain.MetadataTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.veritas.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MetadataTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Metadata.class);
        Metadata metadata1 = getMetadataSample1();
        Metadata metadata2 = new Metadata();
        assertThat(metadata1).isNotEqualTo(metadata2);

        metadata2.setId(metadata1.getId());
        assertThat(metadata1).isEqualTo(metadata2);

        metadata2 = getMetadataSample2();
        assertThat(metadata1).isNotEqualTo(metadata2);
    }

    @Test
    void fileTest() {
        Metadata metadata = getMetadataRandomSampleGenerator();
        File fileBack = getFileRandomSampleGenerator();

        metadata.setFile(fileBack);
        assertThat(metadata.getFile()).isEqualTo(fileBack);

        metadata.file(null);
        assertThat(metadata.getFile()).isNull();
    }
}
