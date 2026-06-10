package dev.bradburylabs.homedrive.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import dev.bradburylabs.homedrive.entity.ObjectUploadPart;

public interface ObjectUploadPartRepository extends JpaRepository<ObjectUploadPart, String> {
    Optional<ObjectUploadPart> findByObjectUploadIdAndPartNumber(String objectUploadId, int partNumber);

    List<ObjectUploadPart> findAllByObjectUploadIdOrderByPartNumber(String objectUploadId);

    void deleteAllByObjectUploadId(String objectUploadId);
}
