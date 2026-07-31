package dev.bradburylabs.homedrive.model.s3;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

@JsonRootName("ListAllMyBucketsResult")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ListBucketsResponse(@JsonProperty("Bucket") @JacksonXmlElementWrapper(localName = "Buckets") List<Bucket> buckets, String continuationToken,
        String prefix) {
}
