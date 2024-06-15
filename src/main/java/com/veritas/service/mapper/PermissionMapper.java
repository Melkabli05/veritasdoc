package com.veritas.service.mapper;

import com.veritas.domain.Folder;
import com.veritas.domain.Permission;
import com.veritas.service.dto.FolderDTO;
import com.veritas.service.dto.PermissionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Permission} and its DTO {@link PermissionDTO}.
 */
@Mapper(componentModel = "spring")
public interface PermissionMapper extends EntityMapper<PermissionDTO, Permission> {
    @Mapping(target = "folder", source = "folder", qualifiedByName = "folderId")
    PermissionDTO toDto(Permission s);

    @Named("folderId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    FolderDTO toDtoFolderId(Folder folder);
}
