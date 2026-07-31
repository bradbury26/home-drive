package dev.bradburylabs.homedrive.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "user_object_closure")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class UserObjectClosure {
    @EmbeddedId
    @EqualsAndHashCode.Include
    private UserObjectClosurePK id;

    @Column(name = "depth")
    @Getter
    private int depth;

    public String getAncestorId() {
        return id.ancestorId;
    }

    public String getDescendantId() {
        return id.descendantId;
    }

    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @EqualsAndHashCode
    @ToString
    public static class UserObjectClosurePK {
        @Column(name = "ancestor_id")
        private String ancestorId;

        @Column(name = "descendant_id")
        private String descendantId;
    }
}
