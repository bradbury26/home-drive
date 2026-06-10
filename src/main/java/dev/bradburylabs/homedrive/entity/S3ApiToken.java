package dev.bradburylabs.homedrive.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "s3_api_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class S3ApiToken {
    @Id
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "access_key_id")
    private String accessKeyId;

    @Column(name = "secret_access_key")
    private String secretAccessKey;

    @Column(name = "read_permission")
    private boolean readPermission;

    @Column(name = "write_permission")
    private boolean writePermission;

    public S3ApiToken(String id, String accessKeyId, String secretAccessKey, boolean readPermission, boolean writePermission) {
        this.id = id;
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.readPermission = readPermission;
        this.writePermission = writePermission;
    }

    public void updateUser(AppUser user) {
        this.user = user;
    }
}
