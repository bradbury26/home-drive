package dev.bradburylabs.homedrive.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import dev.bradburylabs.homedrive.entity.ObjectType;
import dev.bradburylabs.homedrive.entity.ObjectUpload;
import dev.bradburylabs.homedrive.entity.ObjectUploadPart;
import dev.bradburylabs.homedrive.entity.UploadStatus;
import dev.bradburylabs.homedrive.entity.UserObject;
import dev.bradburylabs.homedrive.exception.ObjectUploadNotFoundException;
import dev.bradburylabs.homedrive.exception.UserObjectNotFoundException;
import dev.bradburylabs.homedrive.model.object.CompleteObjectUploadModel;
import dev.bradburylabs.homedrive.model.object.CreateObjectUploadModel;
import dev.bradburylabs.homedrive.model.object.SaveObjectModel;
import dev.bradburylabs.homedrive.model.object.SaveUserObjectPartModel;
import dev.bradburylabs.homedrive.repository.ObjectUploadPartRepository;
import dev.bradburylabs.homedrive.repository.ObjectUploadRepository;
import dev.bradburylabs.homedrive.repository.UserObjectClosureRepository;
import dev.bradburylabs.homedrive.repository.UserObjectRepository;
import dev.bradburylabs.homedrive.util.IdUtils;
import dev.bradburylabs.homedrive.util.ObjectPath;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserObjectServiceImpl implements UserObjectService {
    private final UserObjectRepository userObjectRepository;
    private final UserObjectClosureRepository userObjectClosureRepository;
    private final UserObjectOutboxService userObjectOutboxService;
    private final ObjectUploadRepository objectUploadRepository;
    private final ObjectUploadPartRepository objectUploadPartRepository;

    @Override
    public UserObject getObject(String id, String userId) {
        return userObjectRepository.findByIdAndUserId(id, userId).orElseThrow(() -> new UserObjectNotFoundException("Object not found: " + id));
    }

    @Override
    @Transactional
    public UserObject saveByParentId(String parentId, SaveObjectModel saveObjectModel) {
        Optional<UserObject> userObjectOptional = findObject(parentId, saveObjectModel.userId(), saveObjectModel.objectName(), saveObjectModel.objectType());

        Instant now = Instant.now();

        UserObject userObject = userObjectOptional.orElseGet(
                () -> new UserObject(IdUtils.generateId(), saveObjectModel.userId(), saveObjectModel.objectType(), saveObjectModel.objectName(), now));

        String currentObjectVersion = userObject.getObjectVersion();

        userObject.updateObject(saveObjectModel.etag(), saveObjectModel.contentEncoding(), saveObjectModel.contentType(), saveObjectModel.contentLength(),
                saveObjectModel.checksumType(), saveObjectModel.checksum(), saveObjectModel.objectVersion(), now);

        UserObject updatedUserObject = userObjectRepository.save(userObject);

        userObjectOutboxService.createOutboxEntry(userObject.getObjectName(), userObject.getUserId(), currentObjectVersion, saveObjectModel.objectVersion());

        if (currentObjectVersion == null) {
            createUserObjectClosure(userObject.getId(), parentId);
        }

        return updatedUserObject;
    }

    @Override
    @Transactional
    public UserObject saveByPath(List<String> path, SaveObjectModel saveObjectModel) {
        if (path.isEmpty()) {
            return saveByParentId(null, saveObjectModel);
        } else {
            String parentId = null;

            for (String pathComponent : path) {
                Optional<UserObject> userObjectOptional;

                try {
                    userObjectOptional = findObject(parentId, saveObjectModel.userId(), pathComponent, ObjectType.DIRECTORY);
                } catch (UserObjectNotFoundException e) {
                    userObjectOptional = Optional.empty();
                }

                final String finalParentId = parentId;

                UserObject userObject = userObjectOptional.orElseGet(() -> {
                    SaveObjectModel parentDirectorySaveObjectModel =
                            new SaveObjectModel(IdUtils.generateId(), saveObjectModel.userId(), ObjectType.DIRECTORY, pathComponent, null, null, null, 0, null,
                                    null);
                    return saveByParentId(finalParentId, parentDirectorySaveObjectModel);
                });

                parentId = userObject.getId();
            }

            return saveByParentId(parentId, saveObjectModel);
        }
    }

    @Override
    @Transactional
    public void delete(String id, String userId) {
        UserObject userObject =
                userObjectRepository.findByIdAndUserId(id, userId).orElseThrow(() -> new UserObjectNotFoundException("Object not found for id: " + id));

        userObjectClosureRepository.deleteUserObjectClosures(id);
        userObjectOutboxService.createOutboxEntry(userObject.getObjectName(), userId, userObject.getObjectVersion(), null);
        userObjectRepository.delete(userObject);
    }

    @Override
    @Transactional
    public String createObjectUpload(CreateObjectUploadModel createObjectUploadModel) {
        Instant now = Instant.now();

        ObjectUpload objectUpload =
                objectUploadRepository.findByUserIdAndObjectKeyAndUploadStatus(createObjectUploadModel.userId(), createObjectUploadModel.objectKey(),
                                UploadStatus.STARTED)
                        .orElseGet(() -> new ObjectUpload(IdUtils.generateId(), createObjectUploadModel.userId(), createObjectUploadModel.objectKey(), now));

        if (UploadStatus.STARTED.equals(objectUpload.getUploadStatus())) {
            return objectUpload.getId();
        }

        objectUploadRepository.findAllIdsByCreatedDateBefore(now).forEach(previousUploadId -> {
            objectUploadPartRepository.deleteAllByObjectUploadId(previousUploadId);
            objectUploadRepository.deleteById(previousUploadId);
        });

        objectUpload.markAsStarted();
        objectUpload.update(createObjectUploadModel.contentEncoding(), createObjectUploadModel.contentType(), createObjectUploadModel.checksum().checksumType(),
                createObjectUploadModel.checksum().checksum(), now);

        objectUploadRepository.save(objectUpload);

        return objectUpload.getId();
    }

    @Override
    @Transactional
    public void saveUserObjectPart(SaveUserObjectPartModel saveUserObjectPartModel) {
        String uploadId = saveUserObjectPartModel.uploadId();
        ObjectUpload objectUpload = objectUploadRepository.findByIdAndUploadStatus(uploadId, UploadStatus.STARTED)
                .orElseThrow(() -> new ObjectUploadNotFoundException("Object upload not found for uploadId: " + uploadId));

        Instant now = Instant.now();
        int partNumber = saveUserObjectPartModel.partNumber();

        ObjectUploadPart userObjectPart = objectUploadPartRepository.findByObjectUploadIdAndPartNumber(objectUpload.getId(), partNumber)
                .orElseGet(() -> new ObjectUploadPart(IdUtils.generateId(), objectUpload.getId(), partNumber, now));

        userObjectPart.update(saveUserObjectPartModel.etag(), saveUserObjectPartModel.contentLength(), saveUserObjectPartModel.checksum(), now);

        objectUploadPartRepository.save(userObjectPart);
    }

    @Override
    @Transactional
    public void completeObjectUpload(CompleteObjectUploadModel completeObjectUploadModel) {
        String uploadId = completeObjectUploadModel.uploadId();
        ObjectUpload objectUpload = objectUploadRepository.findByIdAndUploadStatus(uploadId, UploadStatus.STARTED)
                .orElseThrow(() -> new ObjectUploadNotFoundException("Object upload not found for uploadId: " + uploadId));

        String userId = objectUpload.getUserId();
        String objectKey = objectUpload.getObjectKey();
        ObjectPath objectPath = ObjectPath.fromObjectKey(objectKey);

        SaveObjectModel saveObjectModel = new SaveObjectModel(uploadId, userId, ObjectType.FILE, objectPath.getObjectName(), completeObjectUploadModel.etag(),
                objectUpload.getContentEncoding(), objectUpload.getContentType(), completeObjectUploadModel.contentLength(), objectUpload.getChecksumType(),
                completeObjectUploadModel.checksum());

        saveByPath(objectPath.getPath(), saveObjectModel);

        objectUpload.markAsCompleted();
    }

    @Override
    @Transactional
    public void abortObjectUpload(String uploadId) {
        ObjectUpload objectUpload = objectUploadRepository.findByIdAndUploadStatus(uploadId, UploadStatus.STARTED)
                .orElseThrow(() -> new ObjectUploadNotFoundException("Object upload not found for uploadId: " + uploadId));

        objectUpload.markAsAborted();
    }

    private Optional<UserObject> findObject(String parentId, String userId, String objectName, ObjectType objectType) {
        if (parentId == null) {
            return userObjectRepository.findRootObjectByObjectName(userId, objectName, objectType);
        } else {
            validateParent(parentId, userId);

            return userObjectRepository.findByParentIdAndObjectName(userId, parentId, objectName, objectType);
        }
    }

    private void validateParent(String parentId, String userId) {
        UserObject parentUserObject = userObjectRepository.findById(parentId).orElse(null);

        if (parentUserObject == null || !parentUserObject.getUserId().equals(userId) || !ObjectType.DIRECTORY.equals(parentUserObject.getObjectType())) {
            throw new UserObjectNotFoundException("Parent directory not found");
        }
    }

    private void createUserObjectClosure(String id, String parentId) {
        userObjectClosureRepository.createUserObjectClosure(id);

        if (parentId != null) {
            userObjectClosureRepository.createUserObjectClosureSubTree(id, parentId);
        }
    }
}
