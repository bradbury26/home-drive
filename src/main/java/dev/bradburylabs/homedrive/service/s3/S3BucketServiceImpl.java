package dev.bradburylabs.homedrive.service.s3;

import static dev.bradburylabs.homedrive.util.ContinuationTokenUtils.createContinuationToken;
import static dev.bradburylabs.homedrive.util.ContinuationTokenUtils.createScrollPosition;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import dev.bradburylabs.homedrive.entity.AppUser;
import dev.bradburylabs.homedrive.model.s3.Bucket;
import dev.bradburylabs.homedrive.model.s3.ListBucketsRequest;
import dev.bradburylabs.homedrive.model.s3.ListBucketsResponse;
import dev.bradburylabs.homedrive.repository.UserRepository;
import dev.bradburylabs.homedrive.repository.specs.AppUserSpecs;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3BucketServiceImpl implements S3BucketService {
    private final UserRepository userRepository;

    @Override
    public ListBucketsResponse listBuckets(String userId, ListBucketsRequest request) {
        PredicateSpecification<AppUser> spec = PredicateSpecification.unrestricted();

        if (!isAdminUser()) {
            spec = PredicateSpecification.allOf(spec, AppUserSpecs.forId(userId));
        }

        if (request.prefix() != null) {
            spec = PredicateSpecification.allOf(spec, AppUserSpecs.usernamePrefix(request.prefix()));
        }

        int maxBuckets = Optional.ofNullable(request.maxBuckets()).orElse(10_000);
        ScrollPosition scrollPosition = createScrollPosition(request.continuationToken());

        Window<AppUser> window = userRepository.findBy(spec, q -> q.sortBy(Sort.by("username")).limit(maxBuckets).scroll(scrollPosition));

        List<Bucket> buckets = window.stream().map(appUser -> new Bucket(appUser.getUsername(), appUser.getCreatedDate())).toList();

        String nextContinuationToken = createContinuationToken(window, buckets.size() - 1);

        return new ListBucketsResponse(buckets, nextContinuationToken, request.prefix());
    }

    private boolean isAdminUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalArgumentException("Authentication is null");
        }

        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(authority -> Objects.equals(authority, "ROLE_ADMIN"));
    }
}
