package dev.bradburylabs.homedrive.mapper;

import java.io.InputStream;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import dev.bradburylabs.homedrive.entity.AbstractUserObject;
import dev.bradburylabs.homedrive.model.object.Checksum;
import dev.bradburylabs.homedrive.model.object.RetrieveObjectResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ObjectRetrievalResponseMapper {
    @Mapping(target = "version", source = "userObject.objectVersion")
    @Mapping(target = "lastModified", source = "userObject.lastUpdated")
    @Mapping(target = "checksum", source = "userObject", qualifiedByName = "checksum")
    RetrieveObjectResponse map(AbstractUserObject userObject, InputStream inputStream);

    @Named("checksum")
    default Checksum mapChecksum(AbstractUserObject userObject) {
        return new Checksum(userObject.getChecksumType(), userObject.getChecksum());
    }
}
