package dev.bradburylabs.homedrive.entity;

import java.time.Instant;
import java.util.List;
import dev.bradburylabs.homedrive.util.ChecksumType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public abstract class AbstractUserObject {
    @Id
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "object_type")
    @Enumerated(EnumType.STRING)
    private ObjectType objectType;

    @Column(name = "object_name")
    private String objectName;

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

    @OneToMany
    @JoinColumn(name = "descendant_id", referencedColumnName = "id", updatable = false, insertable = false)
    @OrderColumn(name = "depth")
    private List<UserObjectClosure> ancestors;

    @OneToMany
    @JoinColumn(name = "ancestor_id", referencedColumnName = "id", updatable = false, insertable = false)
    @OrderColumn(name = "depth")
    private List<UserObjectClosure> descendants;

    protected AbstractUserObject(String id, String userId, ObjectType objectType, String objectName, Instant createdDate) {
        this.id = id;
        this.userId = userId;
        this.objectType = objectType;
        this.objectName = objectName;
        this.createdDate = createdDate;
    }

    protected AbstractUserObject(String id, String userId, ObjectType objectType, String objectName, String etag, String contentEncoding, String contentType,
            long contentLength, ChecksumType checksumType, String checksum, String objectVersion, Instant createdDate, Instant lastUpdated) {
        this(id, userId, objectType, objectName, createdDate);

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
