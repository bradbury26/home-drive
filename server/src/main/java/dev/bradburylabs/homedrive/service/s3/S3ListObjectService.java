package dev.bradburylabs.homedrive.service.s3;

import static dev.bradburylabs.homedrive.util.ContinuationTokenUtils.createContinuationToken;
import static dev.bradburylabs.homedrive.util.ContinuationTokenUtils.createScrollPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.stereotype.Service;
import dev.bradburylabs.homedrive.entity.S3UserObject;
import dev.bradburylabs.homedrive.model.object.ObjectList;
import dev.bradburylabs.homedrive.repository.S3UserObjectRepository;
import dev.bradburylabs.homedrive.service.ListObjectsService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3ListObjectService implements ListObjectsService<S3UserObject> {
    private final S3UserObjectRepository s3UserObjectRepository;

    @Override
    public ObjectList<S3UserObject> listObjects(PredicateSpecification<S3UserObject> specification, String continuationToken, Pageable pageable) {
        if (continuationToken == null) {
            Page<S3UserObject> page = s3UserObjectRepository.findBy(specification, q -> q.page(pageable));
            String nextMarker = null;

            if (page.hasNext()) {
                nextMarker = page.toList().getLast().getObjectKey();
            }

            return new ObjectList<>(page.getContent(), nextMarker);

        } else {
            ScrollPosition scrollPosition = createScrollPosition(continuationToken);

            Window<S3UserObject> window = s3UserObjectRepository.findBy(specification,
                    q -> q.sortBy(pageable.getSortOr(Sort.by("objectKey"))).limit(pageable.getPageSize()).scroll(scrollPosition));
            String nextContinuationToken = createContinuationToken(window);

            return new ObjectList<>(window.getContent(), nextContinuationToken);
        }
    }
}
