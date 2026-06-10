package dev.bradburylabs.homedrive.model.s3;

import static dev.bradburylabs.homedrive.util.DateUtils.API_DATE_TIME_FORMAT_VALUE;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Bucket(@JsonProperty("Name") String name,
        @JsonProperty("CreationDate") @JsonFormat(pattern = API_DATE_TIME_FORMAT_VALUE, timezone = "UTC") Instant creationDate) {
}
