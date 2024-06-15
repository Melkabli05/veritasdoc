package com.veritas.domain;

import static com.veritas.domain.FileTestSamples.*;
import static com.veritas.domain.FileVersionTestSamples.*;
import static com.veritas.domain.FolderTestSamples.*;
import static com.veritas.domain.MetadataTestSamples.*;
import static com.veritas.domain.TagTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.veritas.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FileTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(File.class);
        File file1 = getFileSample1();
        File file2 = new File();
        assertThat(file1).isNotEqualTo(file2);

        file2.setId(file1.getId());
        assertThat(file1).isEqualTo(file2);

        file2 = getFileSample2();
        assertThat(file1).isNotEqualTo(file2);
    }

    @Test
    void fileVersionTest() {
        File file = getFileRandomSampleGenerator();
        FileVersion fileVersionBack = getFileVersionRandomSampleGenerator();

        file.setFileVersion(fileVersionBack);
        assertThat(file.getFileVersion()).isEqualTo(fileVersionBack);

        file.fileVersion(null);
        assertThat(file.getFileVersion()).isNull();
    }

    @Test
    void metadataTest() {
        File file = getFileRandomSampleGenerator();
        Metadata metadataBack = getMetadataRandomSampleGenerator();

        file.addMetadata(metadataBack);
        assertThat(file.getMetadata()).containsOnly(metadataBack);
        assertThat(metadataBack.getFile()).isEqualTo(file);

        file.removeMetadata(metadataBack);
        assertThat(file.getMetadata()).doesNotContain(metadataBack);
        assertThat(metadataBack.getFile()).isNull();

        file.metadata(new HashSet<>(Set.of(metadataBack)));
        assertThat(file.getMetadata()).containsOnly(metadataBack);
        assertThat(metadataBack.getFile()).isEqualTo(file);

        file.setMetadata(new HashSet<>());
        assertThat(file.getMetadata()).doesNotContain(metadataBack);
        assertThat(metadataBack.getFile()).isNull();
    }

    @Test
    void fileTagTest() {
        File file = getFileRandomSampleGenerator();
        Tag tagBack = getTagRandomSampleGenerator();

        file.addFileTag(tagBack);
        assertThat(file.getFileTags()).containsOnly(tagBack);
        assertThat(tagBack.getFile()).isEqualTo(file);

        file.removeFileTag(tagBack);
        assertThat(file.getFileTags()).doesNotContain(tagBack);
        assertThat(tagBack.getFile()).isNull();

        file.fileTags(new HashSet<>(Set.of(tagBack)));
        assertThat(file.getFileTags()).containsOnly(tagBack);
        assertThat(tagBack.getFile()).isEqualTo(file);

        file.setFileTags(new HashSet<>());
        assertThat(file.getFileTags()).doesNotContain(tagBack);
        assertThat(tagBack.getFile()).isNull();
    }

    @Test
    void fileFolderTest() {
        File file = getFileRandomSampleGenerator();
        Folder folderBack = getFolderRandomSampleGenerator();

        file.addFileFolder(folderBack);
        assertThat(file.getFileFolders()).containsOnly(folderBack);
        assertThat(folderBack.getFile()).isEqualTo(file);

        file.removeFileFolder(folderBack);
        assertThat(file.getFileFolders()).doesNotContain(folderBack);
        assertThat(folderBack.getFile()).isNull();

        file.fileFolders(new HashSet<>(Set.of(folderBack)));
        assertThat(file.getFileFolders()).containsOnly(folderBack);
        assertThat(folderBack.getFile()).isEqualTo(file);

        file.setFileFolders(new HashSet<>());
        assertThat(file.getFileFolders()).doesNotContain(folderBack);
        assertThat(folderBack.getFile()).isNull();
    }

    @Test
    void tagsTest() {
        File file = getFileRandomSampleGenerator();
        Tag tagBack = getTagRandomSampleGenerator();

        file.addTags(tagBack);
        assertThat(file.getTags()).containsOnly(tagBack);

        file.removeTags(tagBack);
        assertThat(file.getTags()).doesNotContain(tagBack);

        file.tags(new HashSet<>(Set.of(tagBack)));
        assertThat(file.getTags()).containsOnly(tagBack);

        file.setTags(new HashSet<>());
        assertThat(file.getTags()).doesNotContain(tagBack);
    }

    @Test
    void foldersTest() {
        File file = getFileRandomSampleGenerator();
        Folder folderBack = getFolderRandomSampleGenerator();

        file.addFolders(folderBack);
        assertThat(file.getFolders()).containsOnly(folderBack);

        file.removeFolders(folderBack);
        assertThat(file.getFolders()).doesNotContain(folderBack);

        file.folders(new HashSet<>(Set.of(folderBack)));
        assertThat(file.getFolders()).containsOnly(folderBack);

        file.setFolders(new HashSet<>());
        assertThat(file.getFolders()).doesNotContain(folderBack);
    }
}
