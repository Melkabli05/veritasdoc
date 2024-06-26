package com.veritas.domain;

import static com.veritas.domain.AssertUtils.zonedDataTimeSameInstant;
import static org.assertj.core.api.Assertions.assertThat;

public class FileVersionAsserts {

    /**
     * Asserts that the entity has all properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertFileVersionAllPropertiesEquals(FileVersion expected, FileVersion actual) {
        assertFileVersionAutoGeneratedPropertiesEquals(expected, actual);
        assertFileVersionAllUpdatablePropertiesEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all updatable properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertFileVersionAllUpdatablePropertiesEquals(FileVersion expected, FileVersion actual) {
        assertFileVersionUpdatableFieldsEquals(expected, actual);
        assertFileVersionUpdatableRelationshipsEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all the auto generated properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertFileVersionAutoGeneratedPropertiesEquals(FileVersion expected, FileVersion actual) {
        assertThat(expected)
            .as("Verify FileVersion auto generated properties")
            .satisfies(e -> assertThat(e.getId()).as("check id").isEqualTo(actual.getId()));
    }

    /**
     * Asserts that the entity has all the updatable fields set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertFileVersionUpdatableFieldsEquals(FileVersion expected, FileVersion actual) {
        assertThat(expected)
            .as("Verify FileVersion relevant properties")
            .satisfies(e -> assertThat(e.getFileId()).as("check fileId").isEqualTo(actual.getFileId()))
            .satisfies(e -> assertThat(e.getVersionNumber()).as("check versionNumber").isEqualTo(actual.getVersionNumber()))
            .satisfies(e -> assertThat(e.getObjectName()).as("check objectName").isEqualTo(actual.getObjectName()))
            .satisfies(
                e ->
                    assertThat(e.getCreatedAt())
                        .as("check createdAt")
                        .usingComparator(zonedDataTimeSameInstant)
                        .isEqualTo(actual.getCreatedAt())
            );
    }

    /**
     * Asserts that the entity has all the updatable relationships set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertFileVersionUpdatableRelationshipsEquals(FileVersion expected, FileVersion actual) {}
}
