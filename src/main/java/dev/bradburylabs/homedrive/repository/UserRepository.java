package dev.bradburylabs.homedrive.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import dev.bradburylabs.homedrive.entity.AppUser;
import dev.bradburylabs.homedrive.entity.S3ApiToken;

public interface UserRepository extends JpaRepository<AppUser, String>, JpaSpecificationExecutor<AppUser> {
    Optional<AppUser> findByUsername(String username);

    @Query("select s3 from AppUser au join au.s3ApiTokens s3 where s3.accessKeyId = :accessKeyId")
    Optional<S3ApiToken> findS3ApiTokenByAccessKeyId(@Param("accessKeyId") String accessKeyId);
}
