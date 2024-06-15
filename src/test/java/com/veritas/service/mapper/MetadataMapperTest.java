package com.veritas.service.mapper;

import static com.veritas.domain.MetadataAsserts.*;
import static com.veritas.domain.MetadataTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetadataMapperTest {

    private MetadataMapper metadataMapper;

    @BeforeEach
    void setUp() {
        metadataMapper = new MetadataMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getMetadataSample1();
        var actual = metadataMapper.toEntity(metadataMapper.toDto(expected));
        assertMetadataAllPropertiesEquals(expected, actual);
    }
}
