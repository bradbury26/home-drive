package dev.bradburylabs.homedrive.service;

import java.io.InputStream;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.stereotype.Service;
import dev.bradburylabs.homedrive.entity.UserObject;
import dev.bradburylabs.homedrive.mapper.ObjectModelMapper;
import dev.bradburylabs.homedrive.model.object.GetObjectResponse;
import dev.bradburylabs.homedrive.model.object.ListObjectsResponse;
import dev.bradburylabs.homedrive.model.object.ObjectList;
import dev.bradburylabs.homedrive.model.object.ObjectModel;
import dev.bradburylabs.homedrive.repository.specs.UserObjectSpecs;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ObjectServiceImpl implements ObjectService {
    private final ObjectStorageService objectStorageService;
    private final UserObjectService userObjectService;
    private final ListObjectsService<UserObject> listObjectsService;
    private final ObjectModelMapper objectModelMapper;

    @Override
    public GetObjectResponse getObject(String id, String userId, boolean retrieveInputStream) {
        UserObject userObject = userObjectService.getObject(id, userId);
        ObjectModel objectModel = objectModelMapper.map(userObject);

        InputStream inputStream = null;

        if (retrieveInputStream) {
            inputStream = objectStorageService.retrieveObject(userObject.getObjectName(), userObject.getObjectVersion(), userId);
        }

        return new GetObjectResponse(objectModel, inputStream);
    }

    @Override
    public ListObjectsResponse listObjects(String userId, String parentId, String continuationToken, Pageable pageable) {
        PredicateSpecification<UserObject> spec = PredicateSpecification.allOf(UserObjectSpecs.forUserId(userId), UserObjectSpecs.forParentId(parentId));
        ObjectList<UserObject> objectList = listObjectsService.listObjects(spec, continuationToken, pageable);

        List<ObjectModel> objects = objectList.objects().stream().map(objectModelMapper::map).toList();

        return new ListObjectsResponse(objects, objectList.continuationToken());
    }
}
