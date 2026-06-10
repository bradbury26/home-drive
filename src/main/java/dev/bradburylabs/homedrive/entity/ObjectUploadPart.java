package dev.bradburylabs.homedrive.entity;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "object_upload_part")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class ObjectUploadPart {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "object_upload_id")
    private String objectUploadId;

    @Column(name = "part_number")
    private int partNumber;

    @Column(name = "etag")
    private String etag;

    @Column(name = "content_length")
    private long contentLength;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    public ObjectUploadPart(String id, String objectUploadId, int partNumber, Instant createdDate) {
        this.id = id;
        this.objectUploadId = objectUploadId;
        this.partNumber = partNumber;
        this.createdDate = createdDate;
    }

    public void update(String etag, long contentLength, String checksum, Instant lastUpdated) {
        this.etag = etag;
        this.contentLength = contentLength;
        this.checksum = checksum;
        this.lastUpdated = lastUpdated;
    }
}
