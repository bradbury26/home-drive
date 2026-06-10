package dev.bradburylabs.homedrive.service.s3;

import static dev.bradburylabs.homedrive.util.ContinuationTokenUtils.createContinuationToken;
import static dev.bradburylabs.homedrive.util.ContinuationTokenUtils.createScrollPosition;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.stereotype.Service;
import dev.bradburylabs.homedrive.entity.UserObject;
import dev.bradburylabs.homedrive.mapper.ObjectContentMapper;
import dev.bradburylabs.homedrive.model.s3.CommonPrefix;
import dev.bradburylabs.homedrive.model.s3.ListObjectV2Request;
import dev.bradburylabs.homedrive.model.s3.ListObjectsRequest;
import dev.bradburylabs.homedrive.model.s3.ListObjectsResult;
import dev.bradburylabs.homedrive.model.s3.ListObjectsV2Result;
import dev.bradburylabs.homedrive.model.s3.ObjectContent;
import dev.bradburylabs.homedrive.repository.UserObjectRepository;
import dev.bradburylabs.homedrive.repository.specs.UserObjectSpecs;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class S3ListObjectsServiceImpl implements S3ListObjectsService {
    private final UserObjectRepository userObjectRepository;
    private final ObjectContentMapper objectContentMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ListObjectsResult listObjects(String userId, ListObjectsRequest request) {
        PredicateSpecification<UserObject> spec = UserObjectSpecs.forUserId(userId);

        if (request.prefix() != null) {
            spec = PredicateSpecification.allOf(spec, UserObjectSpecs.keyPrefix(request.prefix()));
        }

        if (request.marker() != null) {
            spec = PredicateSpecification.allOf(spec, UserObjectSpecs.startAfter(request.marker()));
        }

        int maxKeys = Optional.ofNullable(request.maxKeys()).orElse(1000);

        Page<UserObject> page = userObjectRepository.findBy(spec, q -> q.page(PageRequest.of(0, maxKeys, Sort.by("objectKey"))));

        List<ObjectContent> objectContents = page.stream().map(item -> objectContentMapper.map(item, request.encodingType())).toList();
        DelimiterResults delimiterResults = handleDelimiter(objectContents, request.prefix(), request.delimiter());

        String nextMarker = null;

        if (page.hasNext()) {
            nextMarker = page.toList().getLast().getObjectKey();
        }

        String encodingType = request.encodingType() != null ? request.encodingType().getName() : null;

        return new ListObjectsResult(request.bucketName(), request.prefix(), request.delimiter(), maxKeys, encodingType, request.marker(), nextMarker,
                nextMarker != null, delimiterResults.objectContents(), delimiterResults.commonPrefixes());
    }

    @Override
    public ListObjectsV2Result listObjectsV2(String userId, ListObjectV2Request request) {
        PredicateSpecification<UserObject> spec = UserObjectSpecs.forUserId(userId);

        if (request.prefix() != null) {
            spec = PredicateSpecification.allOf(spec, UserObjectSpecs.keyPrefix(request.prefix()));
        }

        if (request.startAfter() != null) {
            spec = PredicateSpecification.allOf(spec, UserObjectSpecs.startAfter(request.startAfter()));
        }

        int maxKeys = Optional.ofNullable(request.maxKeys()).orElse(1000);
        ScrollPosition scrollPosition = createScrollPosition(request.continuationToken());

        Window<UserObject> window = userObjectRepository.findBy(spec, q -> q.sortBy(Sort.by("objectKey")).limit(maxKeys).scroll(scrollPosition));

        List<ObjectContent> objectContents = window.stream().map(item -> objectContentMapper.map(item, request.encodingType())).toList();
        DelimiterResults delimiterResults = handleDelimiter(objectContents, request.prefix(), request.delimiter());

        String nextContinuationToken = createContinuationToken(window, objectContents.size() - 1);
        String encodingType = request.encodingType() != null ? request.encodingType().getName() : null;

        return new ListObjectsV2Result(request.bucketName(), request.prefix(), request.delimiter(), maxKeys, encodingType, delimiterResults.keyCount(),
                request.continuationToken(), nextContinuationToken, request.startAfter(), nextContinuationToken != null, delimiterResults.objectContents(),
                delimiterResults.commonPrefixes());
    }

    private DelimiterResults handleDelimiter(List<ObjectContent> objectContents, String prefix, String delimiter) {
        if (delimiter == null) {
            return new DelimiterResults(objectContents, null);
        }

        List<ObjectContent> updatedObjectContents = new ArrayList<>();
        Set<String> prefixes = new LinkedHashSet<>();

        for (ObjectContent objectContent : objectContents) {
            String key = objectContent.key();
            String prefixedKey = prefix != null ? key.substring(prefix.length()) : key;

            if (prefixedKey.contains(delimiter)) {
                int delimiterIndex = prefixedKey.indexOf(delimiter);

                prefixes.add((prefix != null ? prefix : "") + prefixedKey.substring(0, delimiterIndex + 1));
            } else {
                updatedObjectContents.add(objectContent);
            }
        }

        List<CommonPrefix> commonPrefixes = !prefixes.isEmpty() ? prefixes.stream().map(CommonPrefix::new).toList() : null;

        return new DelimiterResults(updatedObjectContents, commonPrefixes);
    }


    private record DelimiterResults(List<ObjectContent> objectContents, List<CommonPrefix> commonPrefixes) {
        public int keyCount() {
            return objectContents.size() + (commonPrefixes != null ? commonPrefixes.size() : 0);
        }
    }
}
