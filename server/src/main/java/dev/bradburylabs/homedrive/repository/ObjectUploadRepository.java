package dev.bradburylabs.homedrive.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import dev.bradburylabs.homedrive.entity.ObjectUpload;
import dev.bradburylabs.homedrive.entity.UploadStatus;

public interface ObjectUploadRepository extends JpaRepository<ObjectUpload, String> {
    Optional<ObjectUpload> findByIdAndUploadStatus(String id, UploadStatus uploadStatus);

    Optional<ObjectUpload> findByUserIdAndObjectKeyAndUploadStatus(String userId, String objectKey, UploadStatus uploadStatus);

    @Query("select ou.id from ObjectUpload ou where ou.createdDate < :createdDate")
    List<String> findAllIdsByCreatedDateBefore(@Param("createdDate") Instant createdDate);
}
