package com.veritas.service.mapper;

import com.veritas.domain.FileVersion;
import com.veritas.service.dto.FileVersionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link FileVersion} and its DTO {@link FileVersionDTO}.
 */
@Mapper(componentModel = "spring")
public interface FileVersionMapper extends EntityMapper<FileVersionDTO, FileVersion> {}
