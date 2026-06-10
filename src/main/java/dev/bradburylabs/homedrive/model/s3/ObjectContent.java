package dev.bradburylabs.homedrive.model.s3;

import static dev.bradburylabs.homedrive.util.DateUtils.API_DATE_TIME_FORMAT_VALUE;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.bradburylabs.homedrive.util.ChecksumType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ObjectContent(@JsonProperty("ChecksumAlgorithm") ChecksumType checksumAlgorithm, @JsonProperty("ETag") String etag,
        @JsonProperty("Key") String key, @JsonProperty("LastModified") @JsonFormat(pattern = API_DATE_TIME_FORMAT_VALUE, timezone = "UTC") Instant lastModified,
        @JsonProperty("Size") long size) {
}
