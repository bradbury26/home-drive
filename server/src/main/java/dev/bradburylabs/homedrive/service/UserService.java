package dev.bradburylabs.homedrive.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import dev.bradburylabs.homedrive.model.user.CreateS3ApiTokenModel;
import dev.bradburylabs.homedrive.model.user.CreateUserModel;
import dev.bradburylabs.homedrive.model.user.S3ApiTokenModel;
import dev.bradburylabs.homedrive.model.user.UpdateUserModel;
import dev.bradburylabs.homedrive.model.user.UserModel;

public interface UserService extends UserDetailsService, UserDetailsPasswordService {
    PagedModel<UserModel> readUsers(Pageable pageable);

    UserModel readUser(String id);

    UserModel readUserByUsername(String username);

    void createUser(CreateUserModel createUserModel);

    void updateUser(String id, UpdateUserModel updateUserModel);

    void deleteUser(String id);

    S3ApiTokenModel createS3ApiToken(String id, CreateS3ApiTokenModel createS3ApiTokenModel);

    void deleteS3ApiToken(String id, String accessKeyId);
}
