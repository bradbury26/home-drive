package dev.bradburylabs.homedrive.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import dev.bradburylabs.homedrive.entity.AbstractUserObject;
import dev.bradburylabs.homedrive.model.object.ObjectModel;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ObjectModelMapper {
    @Mapping(target = "name", source = "objectName")
    @Mapping(target = "mediaType", source = "contentType")
    @Mapping(target = "lastModified", source = "lastUpdated")
    ObjectModel map(AbstractUserObject userObject);
}
