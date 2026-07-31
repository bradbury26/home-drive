package dev.bradburylabs.homedrive.service;

import static dev.bradburylabs.homedrive.util.ContinuationTokenUtils.createContinuationToken;
import static dev.bradburylabs.homedrive.util.ContinuationTokenUtils.createScrollPosition;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.stereotype.Service;
import dev.bradburylabs.homedrive.entity.UserObject;
import dev.bradburylabs.homedrive.mapper.ObjectModelMapper;
import dev.bradburylabs.homedrive.model.object.ObjectList;
import dev.bradburylabs.homedrive.model.object.ObjectModel;
import dev.bradburylabs.homedrive.repository.UserObjectRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListObjectsServiceImpl implements ListObjectsService<UserObject> {
    private final UserObjectRepository userObjectRepository;
    private final ObjectModelMapper objectModelMapper;

    @Override
    public ObjectList<UserObject> listObjects(PredicateSpecification<UserObject> specification, String continuationToken, Pageable pageable) {
        ScrollPosition scrollPosition = createScrollPosition(continuationToken);

        List<Sort.Order> orders = new ArrayList<>();
        orders.add(Sort.Order.asc("objectType"));
        orders.addAll(pageable.getSortOr(Sort.by("objectName")).toList());

        Window<UserObject> window =
                userObjectRepository.findBy(specification, q -> q.sortBy(Sort.by(orders)).limit(pageable.getPageSize()).scroll(scrollPosition));

        List<ObjectModel> objects = window.stream().map(objectModelMapper::map).toList();
        String nextContinuationToken = createContinuationToken(window);

        return new ObjectList<>(window.getContent(), nextContinuationToken);
    }
}
