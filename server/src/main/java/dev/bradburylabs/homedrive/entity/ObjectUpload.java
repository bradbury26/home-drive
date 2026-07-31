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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "object_upload")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class ObjectUpload {
    @Id
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "upload_status")
    @Enumerated(EnumType.STRING)
    private UploadStatus uploadStatus;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "object_key")
    private String objectKey;

    @Column(name = "content_encoding")
    private String contentEncoding;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "checksum_type")
    @Enumerated(EnumType.STRING)
    private ChecksumType checksumType;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    public ObjectUpload(String id, String userId, String objectKey, Instant createdDate) {
        this.id = id;
        this.userId = userId;
        this.objectKey = objectKey;
        this.createdDate = createdDate;
    }

    public void update(String contentEncoding, String contentType, ChecksumType checksumType, String checksum, Instant lastUpdated) {
        this.contentEncoding = contentEncoding;
        this.contentType = contentType;
        this.checksumType = checksumType;
        this.checksum = checksum;
        this.lastUpdated = lastUpdated;
    }

    public void markAsStarted() {
        this.uploadStatus = UploadStatus.STARTED;
    }

    public void markAsCompleted() {
        this.uploadStatus = UploadStatus.COMPLETED;
    }

    public void markAsAborted() {
        this.uploadStatus = UploadStatus.ABORTED;
    }
}
