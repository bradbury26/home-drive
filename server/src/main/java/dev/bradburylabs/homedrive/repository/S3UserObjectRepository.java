package dev.bradburylabs.homedrive.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import dev.bradburylabs.homedrive.entity.S3UserObject;

public interface S3UserObjectRepository extends JpaRepository<S3UserObject, String>, JpaSpecificationExecutor<S3UserObject> {
    @Query("select uo.id from S3UserObject uo where uo.userId = :userId and uo.objectKey = :objectKey")
    Optional<String> findIdByUserIdAndObjectKey(@Param("userId") String userId, @Param("objectKey") String objectKey);

    @Query("select uo.objectName from S3UserObject uo where uo.userId = :userId and uo.objectKey = :objectKey")
    Optional<String> findObjectNameByUserIdAndObjectKey(@Param("userId") String userId, @Param("objectKey") String objectKey);

    Optional<S3UserObject> findByUserIdAndObjectKey(String userId, String objectKey);

    boolean existsByUserIdAndObjectKey(String userId, String objectKey);

    boolean existsByUserIdAndObjectKeyAndEtag(String userId, String objectKey, String etag);
}
