package com.veritas.service.mapper;

import com.veritas.domain.File;
import com.veritas.domain.Tag;
import com.veritas.service.dto.FileDTO;
import com.veritas.service.dto.TagDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Tag} and its DTO {@link TagDTO}.
 */
@Mapper(componentModel = "spring")
public interface TagMapper extends EntityMapper<TagDTO, Tag> {
    @Mapping(target = "file", source = "file", qualifiedByName = "fileId")
    @Mapping(target = "files", source = "files", qualifiedByName = "fileIdSet")
    TagDTO toDto(Tag s);

    @Mapping(target = "files", ignore = true)
    @Mapping(target = "removeFiles", ignore = true)
    Tag toEntity(TagDTO tagDTO);

    @Named("fileId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    FileDTO toDtoFileId(File file);

    @Named("fileIdSet")
    default Set<FileDTO> toDtoFileIdSet(Set<File> file) {
        return file.stream().map(this::toDtoFileId).collect(Collectors.toSet());
    }
}
