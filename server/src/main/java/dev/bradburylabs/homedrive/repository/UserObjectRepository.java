package dev.bradburylabs.homedrive.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import dev.bradburylabs.homedrive.entity.ObjectType;
import dev.bradburylabs.homedrive.entity.UserObject;

public interface UserObjectRepository extends JpaRepository<UserObject, String>, JpaSpecificationExecutor<UserObject> {
    Optional<UserObject> findByIdAndUserId(String id, String userId);

    @Query("select uo from UserObject uo join uo.ancestors a where uo.userId = :userId and uo.objectName = :objectName and uo.objectType = :objectType and size(a) = 1")
    Optional<UserObject> findRootObjectByObjectName(@Param("userId") String userId, @Param("objectName") String objectName,
            @Param("objectType") ObjectType objectType);

    @Query("select uo from UserObject uo join uo.ancestors a where uo.userId = :userId and uo.objectName = :objectName and uo.objectType = :objectType and a.id.ancestorId = :parentId and a.depth = 1")
    Optional<UserObject> findByParentIdAndObjectName(@Param("userId") String userId, @Param("parentId") String parentId, @Param("objectName") String objectName,
            @Param("objectType") ObjectType objectType);
}
