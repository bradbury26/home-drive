package dev.bradburylabs.homedrive.entity;

import java.time.Instant;
import dev.bradburylabs.homedrive.util.ChecksumType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "user_object")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = true)
public class UserObject extends AbstractUserObject {
    public UserObject(String id, String userId, ObjectType objectType, String objectName, Instant createdDate) {
        super(id, userId, objectType, objectName, createdDate);
    }

    public UserObject(String id, String userId, ObjectType objectType, String objectName, String etag, String contentEncoding, String contentType,
            long contentLength, ChecksumType checksumType, String checksum, String objectVersion, Instant createdDate, Instant lastUpdated) {
        super(id, userId, objectType, objectName, etag, contentEncoding, contentType, contentLength, checksumType, checksum, objectVersion, createdDate,
                lastUpdated);
    }
}
