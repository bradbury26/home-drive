package dev.bradburylabs.homedrive.api.s3;

import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_CONTENT_SHA256_HEADER;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import dev.bradburylabs.homedrive.api.s3.exception.BucketNotFoundException;
import dev.bradburylabs.homedrive.exception.UserNotFoundException;
import dev.bradburylabs.homedrive.model.s3.ListBucketsRequest;
import dev.bradburylabs.homedrive.model.s3.ListBucketsResponse;
import dev.bradburylabs.homedrive.service.UserService;
import dev.bradburylabs.homedrive.service.s3.S3BucketService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(headers = X_AMZ_CONTENT_SHA256_HEADER)
@RequiredArgsConstructor
public class BucketController {
    private final S3BucketService s3BucketService;
    private final UserService userService;

    @GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
    public ListBucketsResponse listBuckets(@RequestParam(value = "continuation-token", required = false) String continuationToken,
            @RequestParam(value = "max-buckets", required = false) Integer maxBuckets, @RequestParam(value = "prefix", required = false) String prefix) {
        return s3BucketService.listBuckets(userId(), new ListBucketsRequest(maxBuckets, continuationToken, prefix));
    }

    private String userId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalArgumentException("Authentication is null");
        }

        String username = authentication.getName();

        try {
            return userService.readUserByUsername(username).id();
        } catch (UserNotFoundException e) {
            throw new BucketNotFoundException();
        }
    }
}
