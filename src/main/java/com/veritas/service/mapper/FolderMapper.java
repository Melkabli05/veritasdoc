package com.veritas.service.mapper;

import com.veritas.domain.File;
import com.veritas.domain.Folder;
import com.veritas.service.dto.FileDTO;
import com.veritas.service.dto.FolderDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Folder} and its DTO {@link FolderDTO}.
 */
@Mapper(componentModel = "spring")
public interface FolderMapper extends EntityMapper<FolderDTO, Folder> {
    @Mapping(target = "file", source = "file", qualifiedByName = "fileId")
    @Mapping(target = "files", source = "files", qualifiedByName = "fileIdSet")
    FolderDTO toDto(Folder s);

    @Mapping(target = "files", ignore = true)
    @Mapping(target = "removeFiles", ignore = true)
    Folder toEntity(FolderDTO folderDTO);

    @Named("fileId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    FileDTO toDtoFileId(File file);

    @Named("fileIdSet")
    default Set<FileDTO> toDtoFileIdSet(Set<File> file) {
        return file.stream().map(this::toDtoFileId).collect(Collectors.toSet());
    }
}
