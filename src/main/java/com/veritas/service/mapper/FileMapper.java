package com.veritas.service.mapper;

import com.veritas.domain.File;
import com.veritas.domain.FileVersion;
import com.veritas.domain.Folder;
import com.veritas.domain.Tag;
import com.veritas.service.dto.FileDTO;
import com.veritas.service.dto.FileVersionDTO;
import com.veritas.service.dto.FolderDTO;
import com.veritas.service.dto.TagDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link File} and its DTO {@link FileDTO}.
 */
@Mapper(componentModel = "spring")
public interface FileMapper extends EntityMapper<FileDTO, File> {
    @Mapping(target = "fileVersion", source = "fileVersion", qualifiedByName = "fileVersionId")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "tagIdSet")
    @Mapping(target = "folders", source = "folders", qualifiedByName = "folderIdSet")
    FileDTO toDto(File s);

    @Mapping(target = "removeTags", ignore = true)
    @Mapping(target = "removeFolders", ignore = true)
    File toEntity(FileDTO fileDTO);

    @Named("fileVersionId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    FileVersionDTO toDtoFileVersionId(FileVersion fileVersion);

    @Named("tagId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    TagDTO toDtoTagId(Tag tag);

    @Named("tagIdSet")
    default Set<TagDTO> toDtoTagIdSet(Set<Tag> tag) {
        return tag.stream().map(this::toDtoTagId).collect(Collectors.toSet());
    }

    @Named("folderId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    FolderDTO toDtoFolderId(Folder folder);

    @Named("folderIdSet")
    default Set<FolderDTO> toDtoFolderIdSet(Set<Folder> folder) {
        return folder.stream().map(this::toDtoFolderId).collect(Collectors.toSet());
    }
}
