package dev.bradburylabs.homedrive.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import dev.bradburylabs.homedrive.entity.S3UserObject;
import dev.bradburylabs.homedrive.model.s3.EncodingType;
import dev.bradburylabs.homedrive.model.s3.ObjectContent;
import software.amazon.awssdk.utils.http.SdkHttpUtils;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ObjectContentMapper {
    @Mapping(target = "checksumAlgorithm", source = "checksumType")
    @Mapping(target = "key", source = "objectKey", qualifiedByName = "encodedKey")
    @Mapping(target = "lastModified", source = "lastUpdated")
    @Mapping(target = "size", source = "contentLength")
    ObjectContent map(S3UserObject userObject, @Context EncodingType encodingType);

    @Named("encodedKey")
    default String encodedKey(String objectKey, @Context EncodingType encodingType) {
        if (EncodingType.URL.equals(encodingType)) {
            return SdkHttpUtils.urlEncodeIgnoreSlashes(objectKey);
        }

        return objectKey;
    }
}
