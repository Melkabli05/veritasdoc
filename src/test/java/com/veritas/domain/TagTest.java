package com.veritas.domain;

import static com.veritas.domain.FileTestSamples.*;
import static com.veritas.domain.TagTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.veritas.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TagTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Tag.class);
        Tag tag1 = getTagSample1();
        Tag tag2 = new Tag();
        assertThat(tag1).isNotEqualTo(tag2);

        tag2.setId(tag1.getId());
        assertThat(tag1).isEqualTo(tag2);

        tag2 = getTagSample2();
        assertThat(tag1).isNotEqualTo(tag2);
    }

    @Test
    void fileTest() {
        Tag tag = getTagRandomSampleGenerator();
        File fileBack = getFileRandomSampleGenerator();

        tag.setFile(fileBack);
        assertThat(tag.getFile()).isEqualTo(fileBack);

        tag.file(null);
        assertThat(tag.getFile()).isNull();
    }

    @Test
    void filesTest() {
        Tag tag = getTagRandomSampleGenerator();
        File fileBack = getFileRandomSampleGenerator();

        tag.addFiles(fileBack);
        assertThat(tag.getFiles()).containsOnly(fileBack);
        assertThat(fileBack.getTags()).containsOnly(tag);

        tag.removeFiles(fileBack);
        assertThat(tag.getFiles()).doesNotContain(fileBack);
        assertThat(fileBack.getTags()).doesNotContain(tag);

        tag.files(new HashSet<>(Set.of(fileBack)));
        assertThat(tag.getFiles()).containsOnly(fileBack);
        assertThat(fileBack.getTags()).containsOnly(tag);

        tag.setFiles(new HashSet<>());
        assertThat(tag.getFiles()).doesNotContain(fileBack);
        assertThat(fileBack.getTags()).doesNotContain(tag);
    }
}
