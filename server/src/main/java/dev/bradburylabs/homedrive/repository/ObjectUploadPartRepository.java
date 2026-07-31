package dev.bradburylabs.homedrive.repository;

import java.util.Optional;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import dev.bradburylabs.homedrive.entity.ObjectUploadPart;

public interface ObjectUploadPartRepository extends JpaRepository<ObjectUploadPart, String> {
    Optional<ObjectUploadPart> findByObjectUploadIdAndPartNumber(String objectUploadId, int partNumber);

    Window<ObjectUploadPart> findFirst10ByObjectUploadIdOrderByPartNumber(String objectUploadId, ScrollPosition position);

    void deleteAllByObjectUploadId(String objectUploadId);
}
