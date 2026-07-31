package dev.bradburylabs.homedrive.model.s3;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

@JsonRootName("DeleteResult")
public record S3DeleteObjectsResponse(@JsonProperty("Deleted") @JacksonXmlElementWrapper(useWrapping = false) List<DeletedObject> deletedObjects,
        @JsonProperty("Error") @JacksonXmlElementWrapper(useWrapping = false) List<Error> errors) {
}
