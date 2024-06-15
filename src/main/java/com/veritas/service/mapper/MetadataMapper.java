package com.veritas.service.mapper;

import com.veritas.domain.File;
import com.veritas.domain.Metadata;
import com.veritas.service.dto.FileDTO;
import com.veritas.service.dto.MetadataDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Metadata} and its DTO {@link MetadataDTO}.
 */
@Mapper(componentModel = "spring")
public interface MetadataMapper extends EntityMapper<MetadataDTO, Metadata> {
    @Mapping(target = "file", source = "file", qualifiedByName = "fileId")
    MetadataDTO toDto(Metadata s);

    @Named("fileId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    FileDTO toDtoFileId(File file);
}
