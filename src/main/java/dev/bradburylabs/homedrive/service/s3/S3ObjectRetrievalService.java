package dev.bradburylabs.homedrive.service.s3;

import java.time.Instant;
import org.springframework.stereotype.Service;
import dev.bradburylabs.homedrive.api.s3.exception.ObjectNotModifiedException;
import dev.bradburylabs.homedrive.api.s3.exception.ObjectPreconditionFailedException;
import dev.bradburylabs.homedrive.entity.UserObject;
import dev.bradburylabs.homedrive.mapper.ObjectRetrievalResponseMapper;
import dev.bradburylabs.homedrive.model.s3.S3RetrieveObjectRequest;
import dev.bradburylabs.homedrive.properties.HomeDriveProperties;
import dev.bradburylabs.homedrive.repository.UserObjectRepository;
import dev.bradburylabs.homedrive.service.AbstractObjectRetrievalService;

@Service
public class S3ObjectRetrievalService extends AbstractObjectRetrievalService<S3RetrieveObjectRequest> {
    public S3ObjectRetrievalService(UserObjectRepository userObjectRepository, HomeDriveProperties homeDriveProperties,
            ObjectRetrievalResponseMapper objectRetrievalResponseMapper) {
        super(userObjectRepository, homeDriveProperties, objectRetrievalResponseMapper);
    }

    @Override
    protected void preValidateUserObject(S3RetrieveObjectRequest objectRetrievalRequest, UserObject userObject) {
        String ifMatch = objectRetrievalRequest.getIfMatch();

        if (ifMatch != null && !ifMatch.equals(userObject.getEtag())) {
            throw new ObjectPreconditionFailedException();
        }

        String ifNoneMatch = objectRetrievalRequest.getIfNoneMatch();

        if (ifNoneMatch != null && ifNoneMatch.equals(userObject.getEtag())) {
            throw new ObjectPreconditionFailedException();
        }

        Instant ifModifiedSince = objectRetrievalRequest.getIfModifiedSince();

        if (ifModifiedSince != null && !userObject.getLastUpdated().isAfter(objectRetrievalRequest.getIfModifiedSince())) {
            throw new ObjectNotModifiedException();
        }

        Instant ifUnmodifiedSince = objectRetrievalRequest.getIfUnmodifiedSince();

        if (ifUnmodifiedSince != null && userObject.getLastUpdated().isAfter(objectRetrievalRequest.getIfUnmodifiedSince())) {
            throw new ObjectNotModifiedException();
        }
    }
}
