package dev.bradburylabs.homedrive.repository.specs;

import org.springframework.data.jpa.domain.PredicateSpecification;
import dev.bradburylabs.homedrive.entity.S3UserObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class S3UserObjectSpecs {
    public static PredicateSpecification<S3UserObject> forUserId(String userId) {
        return (root, builder) -> builder.equal(root.get("userId"), userId);
    }

    public static PredicateSpecification<S3UserObject> keyPrefix(String prefix) {
        return (root, builder) -> builder.like(root.get("objectKey"), prefix + "%");
    }

    public static PredicateSpecification<S3UserObject> startAfter(String startAfter) {
        return (root, builder) -> builder.greaterThan(root.get("objectKey"), startAfter);
    }
}
