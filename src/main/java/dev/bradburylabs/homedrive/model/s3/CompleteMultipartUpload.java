package dev.bradburylabs.homedrive.model.s3;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

public record CompleteMultipartUpload(@JsonProperty("Part") @JacksonXmlElementWrapper(useWrapping = false) List<Part> parts) {
    public record Part(@JsonProperty("ChecksumCRC32") String checksumCrc32, @JsonProperty("ChecksumMD5") String checksumMd5,
            @JsonProperty("ChecksumSHA1") String checksumSha1, @JsonProperty("ChecksumSHA256") String checksumSha256,
            @JsonProperty("ChecksumSHA512") String checksumSha512, @JsonProperty("ETag") String etag, @JsonProperty("PartNumber") int partNumber) {
    }
}
