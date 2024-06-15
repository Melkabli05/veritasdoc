package com.veritas.domain;

import static com.veritas.domain.AssertUtils.zonedDataTimeSameInstant;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataAsserts {

    /**
     * Asserts that the entity has all properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertMetadataAllPropertiesEquals(Metadata expected, Metadata actual) {
        assertMetadataAutoGeneratedPropertiesEquals(expected, actual);
        assertMetadataAllUpdatablePropertiesEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all updatable properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertMetadataAllUpdatablePropertiesEquals(Metadata expected, Metadata actual) {
        assertMetadataUpdatableFieldsEquals(expected, actual);
        assertMetadataUpdatableRelationshipsEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all the auto generated properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertMetadataAutoGeneratedPropertiesEquals(Metadata expected, Metadata actual) {
        assertThat(expected)
            .as("Verify Metadata auto generated properties")
            .satisfies(e -> assertThat(e.getId()).as("check id").isEqualTo(actual.getId()));
    }

    /**
     * Asserts that the entity has all the updatable fields set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertMetadataUpdatableFieldsEquals(Metadata expected, Metadata actual) {
        assertThat(expected)
            .as("Verify Metadata relevant properties")
            .satisfies(e -> assertThat(e.getFileId()).as("check fileId").isEqualTo(actual.getFileId()))
            .satisfies(e -> assertThat(e.getKey()).as("check key").isEqualTo(actual.getKey()))
            .satisfies(e -> assertThat(e.getValue()).as("check value").isEqualTo(actual.getValue()))
            .satisfies(
                e ->
                    assertThat(e.getCreatedAt())
                        .as("check createdAt")
                        .usingComparator(zonedDataTimeSameInstant)
                        .isEqualTo(actual.getCreatedAt())
            )
            .satisfies(
                e ->
                    assertThat(e.getUpdatedAt())
                        .as("check updatedAt")
                        .usingComparator(zonedDataTimeSameInstant)
                        .isEqualTo(actual.getUpdatedAt())
            );
    }

    /**
     * Asserts that the entity has all the updatable relationships set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertMetadataUpdatableRelationshipsEquals(Metadata expected, Metadata actual) {
        assertThat(expected)
            .as("Verify Metadata relationships")
            .satisfies(e -> assertThat(e.getFile()).as("check file").isEqualTo(actual.getFile()));
    }
}
