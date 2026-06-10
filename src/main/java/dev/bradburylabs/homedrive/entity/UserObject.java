package dev.bradburylabs.homedrive.entity;

import java.time.Instant;
import dev.bradburylabs.homedrive.util.ChecksumType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "user_object")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class UserObject {
    @Id
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "object_key")
    private String objectKey;

    @Column(name = "etag")
    private String etag;

    @Column(name = "content_encoding")
    private String contentEncoding;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "content_length")
    private long contentLength;

    @Column(name = "checksum_type")
    @Enumerated(EnumType.STRING)
    private ChecksumType checksumType;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "object_version")
    private String objectVersion;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    public UserObject(String id, String userId, String objectKey, Instant createdDate) {
        this.id = id;
        this.userId = userId;
        this.objectKey = objectKey;
        this.createdDate = createdDate;
    }

    public UserObject(String id, String userId, String objectKey, String etag, String contentEncoding, String contentType, long contentLength,
            ChecksumType checksumType, String checksum, String objectVersion, Instant createdDate, Instant lastUpdated) {
        this(id, userId, objectKey, createdDate);

        this.etag = etag;
        this.contentEncoding = contentEncoding;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.checksumType = checksumType;
        this.checksum = checksum;
        this.objectVersion = objectVersion;
        this.lastUpdated = lastUpdated;
    }

    public void updateObject(String etag, String contentEncoding, String contentType, long contentLength, ChecksumType checksumType, String checksum,
            String objectVersion, Instant lastUpdated) {
        this.etag = etag;
        this.contentEncoding = contentEncoding;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.checksumType = checksumType;
        this.checksum = checksum;
        this.objectVersion = objectVersion;
        this.lastUpdated = lastUpdated;
    }
}
