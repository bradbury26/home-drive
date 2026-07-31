package dev.bradburylabs.homedrive.api.internal.admin;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import dev.bradburylabs.homedrive.api.annotation.IsAdmin;
import dev.bradburylabs.homedrive.model.user.CreateS3ApiTokenModel;
import dev.bradburylabs.homedrive.model.user.CreateUserModel;
import dev.bradburylabs.homedrive.model.user.S3ApiTokenModel;
import dev.bradburylabs.homedrive.model.user.UpdateUserModel;
import dev.bradburylabs.homedrive.model.user.UserModel;
import dev.bradburylabs.homedrive.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/user")
@IsAdmin
@RequiredArgsConstructor
public class UserAdminController {
    private final UserService userService;

    @GetMapping
    public PagedModel<UserModel> readUsers(Pageable pageable) {
        return userService.readUsers(pageable);
    }

    @GetMapping("/{id}")
    public UserModel readUser(@PathVariable String id) {
        return userService.readUser(id);
    }

    @PostMapping
    public void createUser(@RequestBody CreateUserModel createUserModel) {
        try {
            userService.createUser(createUserModel);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }
    }

    @PutMapping("/{id}")
    public void updateUser(@PathVariable String id, @RequestBody UpdateUserModel updateUserModel) {
        userService.updateUser(id, updateUserModel);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
    }

    @PostMapping("/{id}/s3/token")
    public S3ApiTokenModel createS3ApiToken(@PathVariable String id, @RequestBody CreateS3ApiTokenModel createS3ApiTokenModel) {
        return userService.createS3ApiToken(id, createS3ApiTokenModel);
    }

    @DeleteMapping("/{id}/s3/token/{accessKeyId}")
    public void deleteS3ApiToken(@PathVariable String id, @PathVariable String accessKeyId) {
        userService.deleteS3ApiToken(id, accessKeyId);
    }
}
