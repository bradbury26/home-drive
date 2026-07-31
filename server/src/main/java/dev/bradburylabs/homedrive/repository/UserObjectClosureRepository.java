package dev.bradburylabs.homedrive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import dev.bradburylabs.homedrive.entity.UserObjectClosure;

public interface UserObjectClosureRepository extends JpaRepository<UserObjectClosure, UserObjectClosure.UserObjectClosurePK> {
    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO user_object_closure (ancestor_id, descendant_id, depth) VALUES(:id, :id, 0)")
    void createUserObjectClosure(@Param("id") String id);

    @Modifying
    @Query(nativeQuery = true,
            value = "INSERT INTO user_object_closure (ancestor_id, descendant_id, depth) SELECT supertree.ancestor_id, subtree.descendant_id, supertree.depth + subtree.depth + 1 FROM user_object_closure AS supertree CROSS JOIN user_object_closure AS subtree WHERE supertree.descendant_id = :parentId AND subtree.ancestor_id = :id")
    void createUserObjectClosureSubTree(@Param("id") String id, @Param("parentId") String parentId);

    @Modifying
    @Query(nativeQuery = true,
            value = "DELETE FROM user_object_closure WHERE descendant_id IN (SELECT descendant_id FROM user_object_closure WHERE ancestor_id = :id) AND ancestor_id IN (SELECT ancestor_id FROM user_object_closure WHERE descendant_id = :id AND ancestor_id != descendant_id)")
    void deleteUserObjectClosureSubTree(@Param("id") String id);

    @Modifying
    @Query(nativeQuery = true,
            value = "DELETE FROM user_object_closure WHERE descendant_id IN (SELECT descendant_id FROM user_object_closure WHERE ancestor_id = :id OR descendant_id = :id)")
    void deleteUserObjectClosures(@Param("id") String id);
}
