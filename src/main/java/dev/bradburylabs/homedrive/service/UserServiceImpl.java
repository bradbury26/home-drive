package dev.bradburylabs.homedrive.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.passay.data.EnglishCharacterData;
import org.passay.generate.PasswordGenerator;
import org.passay.rule.CharacterRule;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import dev.bradburylabs.homedrive.entity.AppUser;
import dev.bradburylabs.homedrive.entity.S3ApiToken;
import dev.bradburylabs.homedrive.exception.S3ApiTokenNotFoundException;
import dev.bradburylabs.homedrive.exception.UserNotFoundException;
import dev.bradburylabs.homedrive.mapper.UserMapper;
import dev.bradburylabs.homedrive.model.user.CreateS3ApiTokenModel;
import dev.bradburylabs.homedrive.model.user.CreateUserModel;
import dev.bradburylabs.homedrive.model.user.S3ApiTokenModel;
import dev.bradburylabs.homedrive.model.user.UpdateUserModel;
import dev.bradburylabs.homedrive.model.user.UserModel;
import dev.bradburylabs.homedrive.properties.HomeDriveProperties;
import dev.bradburylabs.homedrive.repository.UserRepository;
import dev.bradburylabs.homedrive.util.EncryptionUtils;
import dev.bradburylabs.homedrive.util.IdUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final List<CharacterRule> apiTokenRules =
            List.of(new CharacterRule(EnglishCharacterData.UpperCase, 1), new CharacterRule(EnglishCharacterData.LowerCase, 1),
                    new CharacterRule(EnglishCharacterData.Digit, 1));

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final HomeDriveProperties homeDriveProperties;
    private final UserMapper userMapper;

    @EventListener(ApplicationStartedEvent.class)
    @Transactional
    public void createDefaultAdminUser() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            String password = passwordEncoder.encode("test1234");
            Instant now = Instant.now();
            AppUser adminUser = new AppUser("1", "admin", "Administrator", null, password, now, now, true);

            userRepository.save(adminUser);
        }
    }

    @Override
    public PagedModel<UserModel> readUsers(Pageable pageable) {
        Page<AppUser> appUsers = userRepository.findAll(pageable);

        return new PagedModel<>(appUsers.map(userMapper::map));
    }

    @Override
    public UserModel readUser(String id) {
        return userRepository.findById(id).map(userMapper::map).orElseThrow(() -> new UserNotFoundException("Unable to find user with id: " + id));
    }

    @Override
    public UserModel readUserByUsername(String username) {
        return userRepository.findByUsername(username).map(userMapper::map)
                .orElseThrow(() -> new UserNotFoundException("Unable to find user with username: " + username));
    }

    @Override
    @Transactional
    public void createUser(CreateUserModel createUserModel) {
        Instant now = Instant.now();
        String hashedPassword = createUserModel.password() != null ? passwordEncoder.encode(createUserModel.password()) : null;

        AppUser appUser =
                new AppUser(IdUtils.generateId(), createUserModel.username(), createUserModel.name(), createUserModel.email(), hashedPassword, now, now,
                        createUserModel.adminUser());

        userRepository.save(appUser);
    }

    @Override
    @Transactional
    public void updateUser(String id, UpdateUserModel updateUserModel) {
        AppUser appUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Unable to find user with id: " + id));

        appUser.update(updateUserModel);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public S3ApiTokenModel createS3ApiToken(String id, CreateS3ApiTokenModel createS3ApiTokenModel) {
        AppUser appUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Unable to find user with id: " + id));
        Pair<String, String> token = generateS3ApiToken();

        String accessKeyId = token.getFirst();
        String secretAccessKey = token.getSecond();
        String encryptedSecretAccessKey = EncryptionUtils.encrypt(homeDriveProperties.getSecurity().getTokenKey(), secretAccessKey);

        S3ApiToken s3ApiToken = new S3ApiToken(IdUtils.generateId(), accessKeyId, encryptedSecretAccessKey, createS3ApiTokenModel.readPermission(),
                createS3ApiTokenModel.writePermission());

        appUser.addS3ApiToken(s3ApiToken);

        return new S3ApiTokenModel(accessKeyId, secretAccessKey);
    }

    @Override
    @Transactional
    public void deleteS3ApiToken(String id, String accessKeyId) {
        AppUser appUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Unable to find user with id: " + id));

        if (!appUser.removeS3ApiToken(accessKeyId)) {
            throw new S3ApiTokenNotFoundException("No token found for accessKeyId: " + accessKeyId);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user =
                userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Unable to find user with username: " + username));

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        if (user.isAdminUser()) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return new User(user.getUsername(), user.getPassword(), grantedAuthorities);
    }

    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails user, @Nullable String newPassword) {
        AppUser appUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Unable to find user with username:" + user.getUsername()));

        appUser.updatePassword(newPassword);

        return User.withUserDetails(user).password(newPassword).build();
    }

    private Pair<String, String> generateS3ApiToken() {
        PasswordGenerator passwordGenerator = new PasswordGenerator(20, apiTokenRules);

        String accessKeyId = passwordGenerator.generate().toString();
        String secretAccessKey = passwordGenerator.generate().toString();

        return Pair.of(accessKeyId, secretAccessKey);
    }
}
