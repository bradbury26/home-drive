package dev.bradburylabs.homedrive.repository.specs;

import org.springframework.data.jpa.domain.PredicateSpecification;
import dev.bradburylabs.homedrive.entity.AppUser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppUserSpecs {
    public static PredicateSpecification<AppUser> forId(String id) {
        return (root, builder) -> builder.equal(root.get("id"), id);
    }

    public static PredicateSpecification<AppUser> usernamePrefix(String prefix) {
        return (root, builder) -> builder.like(root.get("username"), prefix + "%");
    }
}
