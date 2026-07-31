package dev.bradburylabs.homedrive.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.PredicateSpecification;
import dev.bradburylabs.homedrive.entity.AbstractUserObject;
import dev.bradburylabs.homedrive.model.object.ObjectList;

public interface ListObjectsService<T extends AbstractUserObject> {
    ObjectList<T> listObjects(PredicateSpecification<T> specification, String continuationToken, Pageable pageable);
}
