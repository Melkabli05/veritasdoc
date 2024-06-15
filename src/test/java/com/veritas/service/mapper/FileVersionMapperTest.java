package com.veritas.service.mapper;

import static com.veritas.domain.FileVersionAsserts.*;
import static com.veritas.domain.FileVersionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileVersionMapperTest {

    private FileVersionMapper fileVersionMapper;

    @BeforeEach
    void setUp() {
        fileVersionMapper = new FileVersionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getFileVersionSample1();
        var actual = fileVersionMapper.toEntity(fileVersionMapper.toDto(expected));
        assertFileVersionAllPropertiesEquals(expected, actual);
    }
}
