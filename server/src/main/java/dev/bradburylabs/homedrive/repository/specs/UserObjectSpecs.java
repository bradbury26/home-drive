package dev.bradburylabs.homedrive.repository.specs;

import org.springframework.data.jpa.domain.PredicateSpecification;
import dev.bradburylabs.homedrive.entity.UserObject;
import dev.bradburylabs.homedrive.entity.UserObjectClosure;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserObjectSpecs {
    public static PredicateSpecification<UserObject> forUserId(String userId) {
        return (root, builder) -> builder.equal(root.get("userId"), userId);
    }

    public static PredicateSpecification<UserObject> forParentId(String parentId) {
        return (root, builder) -> {

            if (parentId != null) {
                Join<UserObject, UserObjectClosure> ancestors = root.join("ancestors", JoinType.INNER);

                return builder.and(builder.equal(ancestors.get("id").get("ancestorId"), parentId), builder.equal(ancestors.get("depth"), 1));
            } else {
                return builder.size(root.get("ancestors")).equalTo(1);
            }
        };
    }
}
