package dev.bradburylabs.homedrive.model.s3;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

@JsonRootName("Delete")
public record S3DeleteObjectsRequest(@JsonProperty("Object") @JacksonXmlElementWrapper(useWrapping = false) List<ObjectIdentifier> objectIdentifiers,
        @JsonProperty(value = "Quiet", defaultValue = "false") boolean quiet) {
}
