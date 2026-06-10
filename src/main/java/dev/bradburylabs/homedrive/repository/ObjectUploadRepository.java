package dev.bradburylabs.homedrive.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import dev.bradburylabs.homedrive.entity.ObjectUpload;
import dev.bradburylabs.homedrive.entity.UploadStatus;

public interface ObjectUploadRepository extends JpaRepository<ObjectUpload, String> {
    Optional<ObjectUpload> findByUserIdAndObjectKeyAndUploadStatus(String userId, String objectKey, UploadStatus uploadStatus);
}
