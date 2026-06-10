package dev.bradburylabs.homedrive.model.s3;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

@JsonRootName("ListBucketResult")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ListObjectsResult(@JsonProperty("Name") String name, @JsonProperty("Prefix") String prefix, @JsonProperty("Delimiter") String delimiter,
        @JsonProperty("MaxKeys") int maxKeys, @JsonProperty("EncodingType") String encodingType, @JsonProperty("Marker") String marker,
        @JsonProperty("NextMarker") String nextMarker, @JsonProperty("IsTruncated") boolean isTruncated,
        @JsonProperty("Contents") @JacksonXmlElementWrapper(useWrapping = false) List<ObjectContent> contents,
        @JsonProperty("CommonPrefixes") @JacksonXmlElementWrapper(useWrapping = false) List<CommonPrefix> commonPrefixes) {
}
