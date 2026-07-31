package dev.bradburylabs.homedrive.service;

import java.util.List;
import dev.bradburylabs.homedrive.entity.UserObject;
import dev.bradburylabs.homedrive.model.object.CompleteObjectUploadModel;
import dev.bradburylabs.homedrive.model.object.CreateObjectUploadModel;
import dev.bradburylabs.homedrive.model.object.SaveObjectModel;
import dev.bradburylabs.homedrive.model.object.SaveUserObjectPartModel;

public interface UserObjectService {
    UserObject getObject(String id, String userId);

    UserObject saveByParentId(String parentId, SaveObjectModel saveObjectModel);

    UserObject saveByPath(List<String> path, SaveObjectModel saveObjectModel);

    void delete(String id, String userId);

    String createObjectUpload(CreateObjectUploadModel createObjectUploadModel);

    void saveUserObjectPart(SaveUserObjectPartModel saveUserObjectPartModel);

    void completeObjectUpload(CompleteObjectUploadModel completeObjectUploadModel);

    void abortObjectUpload(String uploadId);
}
