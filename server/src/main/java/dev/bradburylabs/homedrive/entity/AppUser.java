package dev.bradburylabs.homedrive.entity;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import dev.bradburylabs.homedrive.model.user.UpdateUserModel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "app_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class AppUser {
    @Id
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "username")
    private String username;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "modified_date")
    private Instant modifiedDate;

    @Column(name = "admin_user")
    private boolean adminUser;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user")
    @MapKey(name = "accessKeyId")
    @Fetch(FetchMode.SUBSELECT)
    private final Map<String, S3ApiToken> s3ApiTokens = new HashMap<>();

    public AppUser(String id, String username, String name, String email, String password, Instant createdDate, Instant modifiedDate, boolean adminUser) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.adminUser = adminUser;
    }

    public void update(UpdateUserModel updateUserModel) {
        this.name = updateUserModel.name();
        this.email = updateUserModel.email();
        this.adminUser = updateUserModel.adminUser();
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public S3ApiToken getS3ApiToken(String accessKeyId) {
        return s3ApiTokens.get(accessKeyId);
    }

    public void addS3ApiToken(S3ApiToken s3ApiToken) {
        s3ApiToken.updateUser(this);

        this.s3ApiTokens.put(s3ApiToken.getAccessKeyId(), s3ApiToken);
    }

    public boolean removeS3ApiToken(String accessKeyId) {
        return this.s3ApiTokens.remove(accessKeyId) != null;
    }
}
