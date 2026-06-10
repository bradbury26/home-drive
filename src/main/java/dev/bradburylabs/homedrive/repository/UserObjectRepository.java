package dev.bradburylabs.homedrive.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import dev.bradburylabs.homedrive.entity.UserObject;

public interface UserObjectRepository extends JpaRepository<UserObject, String>, JpaSpecificationExecutor<UserObject> {
    Optional<UserObject> findByUserIdAndObjectKey(String userId, String objectKey);
}
