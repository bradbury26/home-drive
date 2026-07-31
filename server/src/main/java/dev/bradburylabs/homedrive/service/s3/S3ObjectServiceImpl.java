package dev.bradburylabs.homedrive.service.s3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import dev.bradburylabs.homedrive.api.s3.exception.InvalidChunkException;
import dev.bradburylabs.homedrive.api.s3.exception.InvalidTrailersException;
import dev.bradburylabs.homedrive.api.s3.exception.ObjectNotModifiedException;
import dev.bradburylabs.homedrive.api.s3.exception.ObjectPreconditionFailedException;
import dev.bradburylabs.homedrive.api.s3.security.S3AuthenticationDetails;
import dev.bradburylabs.homedrive.api.s3.signature.ChunkSignatureValidator;
import dev.bradburylabs.homedrive.api.s3.signature.TrailerSignatureValidator;
import dev.bradburylabs.homedrive.entity.ObjectType;
import dev.bradburylabs.homedrive.entity.ObjectUpload;
import dev.bradburylabs.homedrive.entity.S3UserObject;
import dev.bradburylabs.homedrive.entity.UploadStatus;
import dev.bradburylabs.homedrive.exception.BadDigestException;
import dev.bradburylabs.homedrive.exception.ObjectUploadNotFoundException;
import dev.bradburylabs.homedrive.exception.UserObjectNotFoundException;
import dev.bradburylabs.homedrive.mapper.ObjectContentMapper;
import dev.bradburylabs.homedrive.mapper.ObjectRetrievalResponseMapper;
import dev.bradburylabs.homedrive.model.object.Checksum;
import dev.bradburylabs.homedrive.model.object.CompleteObjectUploadModel;
import dev.bradburylabs.homedrive.model.object.CreateObjectUploadModel;
import dev.bradburylabs.homedrive.model.object.ObjectList;
import dev.bradburylabs.homedrive.model.object.ProcessObjectUploadPartResult;
import dev.bradburylabs.homedrive.model.object.RetrieveObjectResponse;
import dev.bradburylabs.homedrive.model.object.SaveObjectModel;
import dev.bradburylabs.homedrive.model.object.SaveUserObjectPartModel;
import dev.bradburylabs.homedrive.model.object.StoreObjectResponse;
import dev.bradburylabs.homedrive.model.s3.ChunkedMetadata;
import dev.bradburylabs.homedrive.model.s3.CommonPrefix;
import dev.bradburylabs.homedrive.model.s3.ListObjectV2Request;
import dev.bradburylabs.homedrive.model.s3.ListObjectsRequest;
import dev.bradburylabs.homedrive.model.s3.ListObjectsResult;
import dev.bradburylabs.homedrive.model.s3.ListObjectsV2Result;
import dev.bradburylabs.homedrive.model.s3.ObjectContent;
import dev.bradburylabs.homedrive.model.s3.S3DeleteObjectRequest;
import dev.bradburylabs.homedrive.model.s3.S3RetrieveObjectRequest;
import dev.bradburylabs.homedrive.model.s3.S3StoreObjectRequest;
import dev.bradburylabs.homedrive.model.s3.Trailers;
import dev.bradburylabs.homedrive.processor.ObjectUploadPartProcessor;
import dev.bradburylabs.homedrive.repository.ObjectUploadRepository;
import dev.bradburylabs.homedrive.repository.S3UserObjectRepository;
import dev.bradburylabs.homedrive.repository.specs.S3UserObjectSpecs;
import dev.bradburylabs.homedrive.service.ListObjectsService;
import dev.bradburylabs.homedrive.service.ObjectStorageService;
import dev.bradburylabs.homedrive.service.UserObjectService;
import dev.bradburylabs.homedrive.util.ObjectPath;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3ObjectServiceImpl implements S3ObjectService {
    private final ObjectStorageService objectStorageService;
    private final UserObjectService userObjectService;
    private final ListObjectsService<S3UserObject> listObjectsService;
    private final S3UserObjectRepository s3UserObjectRepository;
    private final ObjectUploadRepository objectUploadRepository;
    private final ObjectUploadPartProcessor objectUploadPartProcessor;
    private final ObjectRetrievalResponseMapper objectRetrievalResponseMapper;
    private final ObjectContentMapper objectContentMapper;

    @Override
    public RetrieveObjectResponse retrieveObject(S3RetrieveObjectRequest retrieveObjectRequest, boolean retrieveInputStream) {
        String userId = retrieveObjectRequest.getUserId();
        String key = retrieveObjectRequest.getKey();

        S3UserObject userObject = s3UserObjectRepository.findByUserIdAndObjectKey(userId, key)
                .orElseThrow(() -> new UserObjectNotFoundException("Object not found for key: " + key));

        preValidateUserObjectRetrieval(retrieveObjectRequest, userObject);

        InputStream inputStream = null;

        if (retrieveInputStream) {
            inputStream =
                    objectStorageService.retrieveObject(userObject.getObjectName(), userObject.getObjectVersion(), userId, retrieveObjectRequest.getRange(),
                            userObject.getContentLength());
        }

        return objectRetrievalResponseMapper.map(userObject, inputStream);
    }

    @Override
    public ListObjectsResult listObjects(String userId, ListObjectsRequest request) {
        PredicateSpecification<S3UserObject> spec = S3UserObjectSpecs.forUserId(userId);

        if (request.prefix() != null) {
            spec = PredicateSpecification.allOf(spec, S3UserObjectSpecs.keyPrefix(request.prefix()));
        }

        if (request.marker() != null) {
            spec = PredicateSpecification.allOf(spec, S3UserObjectSpecs.startAfter(request.marker()));
        }

        int maxKeys = Optional.ofNullable(request.maxKeys()).orElse(1000);

        ObjectList<S3UserObject> objectList = listObjectsService.listObjects(spec, null, PageRequest.of(0, maxKeys, Sort.by("objectKey")));

        DelimiterResults delimiterResults = handleDelimiter(objectList.objects(), request.prefix(), request.delimiter());
        List<ObjectContent> objectContents =
                delimiterResults.userObjects().stream().map(item -> objectContentMapper.map(item, request.encodingType())).toList();
        List<CommonPrefix> commonPrefixes = delimiterResults.commonPrefixes().stream().map(CommonPrefix::new).toList();

        String encodingType = request.encodingType() != null ? request.encodingType().getName() : null;

        return new ListObjectsResult(request.bucketName(), request.prefix(), request.delimiter(), maxKeys, encodingType, request.marker(),
                objectList.continuationToken(), objectList.continuationToken() != null, objectContents, commonPrefixes);
    }

    @Override
    public ListObjectsV2Result listObjectsV2(String userId, ListObjectV2Request request) {
        PredicateSpecification<S3UserObject> spec = S3UserObjectSpecs.forUserId(userId);

        if (request.prefix() != null) {
            spec = PredicateSpecification.allOf(spec, S3UserObjectSpecs.keyPrefix(request.prefix()));
        }

        if (request.startAfter() != null) {
            spec = PredicateSpecification.allOf(spec, S3UserObjectSpecs.startAfter(request.startAfter()));
        }

        int maxKeys = Optional.ofNullable(request.maxKeys()).orElse(1000);

        ObjectList<S3UserObject> objectList =
                listObjectsService.listObjects(spec, request.continuationToken(), PageRequest.of(0, maxKeys, Sort.by("objectKey")));

        DelimiterResults delimiterResults = handleDelimiter(objectList.objects(), request.prefix(), request.delimiter());
        List<ObjectContent> objectContents =
                delimiterResults.userObjects().stream().map(item -> objectContentMapper.map(item, request.encodingType())).toList();
        List<CommonPrefix> commonPrefixes = delimiterResults.commonPrefixes().stream().map(CommonPrefix::new).toList();

        String encodingType = request.encodingType() != null ? request.encodingType().getName() : null;

        return new ListObjectsV2Result(request.bucketName(), request.prefix(), request.delimiter(), maxKeys, encodingType, delimiterResults.keyCount(),
                request.continuationToken(), objectList.continuationToken(), request.startAfter(), objectList.continuationToken() != null, objectContents,
                commonPrefixes);
    }

    @Override
    public StoreObjectResponse uploadSinglePartObject(S3StoreObjectRequest storeObjectRequest, InputStream inputStream) {
        preValidateUserObjectStorage(storeObjectRequest);

        ObjectPath s3Path = ObjectPath.fromObjectKey(storeObjectRequest.getKey());

        StoreObjectResponse storeObjectResponse =
                objectStorageService.storeObject(s3Path.getObjectName(), storeObjectRequest.getUserId(), storeObjectRequest.getChecksum(),
                        defaultOutputStreamConsumer(inputStream));

        SaveObjectModel saveObjectModel = createSaveObjectModel(storeObjectRequest, storeObjectResponse, s3Path.getObjectName());

        userObjectService.saveByPath(s3Path.getPath(), saveObjectModel);

        return storeObjectResponse;
    }

    @Override
    public StoreObjectResponse uploadChunkedSinglePartObject(S3StoreObjectRequest storeObjectRequest, InputStream inputStream, boolean trailers) {
        preValidateUserObjectStorage(storeObjectRequest);

        ObjectPath s3Path = ObjectPath.fromObjectKey(storeObjectRequest.getKey());

        S3AuthenticationDetails s3AuthenticationDetails = s3AuthenticationDetails();
        ChunkSignatureValidator chunkSignatureValidator = new ChunkSignatureValidator(s3AuthenticationDetails, secretAccessKey());
        ChunkedMetadata chunkedMetadata = new ChunkedMetadata(s3AuthenticationDetails.signature());

        StoreObjectResponse storeObjectResponse =
                objectStorageService.storeObject(s3Path.getObjectName(), storeObjectRequest.getUserId(), storeObjectRequest.getChecksum(),
                        objectStreamConsumer(chunkedMetadata, chunkSignatureValidator, inputStream));

        if (trailers) {
            handleTrailers(chunkedMetadata, storeObjectResponse.checksum().checksum(), storeObjectRequest.getTrailerHeader(), inputStream, true);
        }

        SaveObjectModel saveObjectModel = createSaveObjectModel(storeObjectRequest, storeObjectResponse, s3Path.getObjectName());

        userObjectService.saveByPath(s3Path.getPath(), saveObjectModel);

        return storeObjectResponse;
    }

    @Override
    public void deleteObject(S3DeleteObjectRequest deleteObjectRequest) {
        String id = s3UserObjectRepository.findIdByUserIdAndObjectKey(deleteObjectRequest.getUserId(), deleteObjectRequest.getKey())
                .orElseThrow(() -> new UserObjectNotFoundException("Object not found for key: " + deleteObjectRequest.getKey()));

        userObjectService.delete(id, deleteObjectRequest.getUserId());
    }

    @Override
    public String createObjectUpload(CreateObjectUploadModel createObjectUploadModel) {
        return userObjectService.createObjectUpload(createObjectUploadModel);
    }

    @Override
    public StoreObjectResponse uploadObjectPart(String uploadId, int partNumber, S3StoreObjectRequest storeObjectRequest, InputStream inputStream) {
        String objectName = s3UserObjectRepository.findObjectNameByUserIdAndObjectKey(storeObjectRequest.getUserId(), storeObjectRequest.getKey())
                .orElseThrow(() -> new UserObjectNotFoundException("Object not found for key: " + storeObjectRequest.getKey()));

        S3AuthenticationDetails s3AuthenticationDetails = s3AuthenticationDetails();
        ChunkSignatureValidator chunkSignatureValidator = new ChunkSignatureValidator(s3AuthenticationDetails, secretAccessKey());
        ChunkedMetadata chunkedMetadata = new ChunkedMetadata(s3AuthenticationDetails.signature());

        StoreObjectResponse storeObjectResponse =
                objectStorageService.storeObjectUploadPart(uploadId, partNumber, objectName, storeObjectRequest.getUserId(), storeObjectRequest.getChecksum(),
                        objectStreamConsumer(chunkedMetadata, chunkSignatureValidator, inputStream));

        handleTrailers(chunkedMetadata, storeObjectResponse.checksum().checksum(), storeObjectRequest.getTrailerHeader(), inputStream, false);

        SaveUserObjectPartModel saveUserObjectPartModel =
                new SaveUserObjectPartModel(uploadId, partNumber, storeObjectResponse.etag(), storeObjectResponse.contentLength(),
                        storeObjectResponse.checksum().checksum());

        userObjectService.saveUserObjectPart(saveUserObjectPartModel);

        return storeObjectResponse;
    }

    @Override
    public StoreObjectResponse completeObjectUpload(String uploadId, InputStream inputStream) {
        ObjectUpload objectUpload = objectUploadRepository.findByIdAndUploadStatus(uploadId, UploadStatus.STARTED)
                .orElseThrow(() -> new ObjectUploadNotFoundException("Object upload not found for uploadId: " + uploadId));

        ProcessObjectUploadPartResult result = objectUploadPartProcessor.processObjectUploadParts(uploadId, inputStream, objectUpload.getChecksumType());

        CompleteObjectUploadModel completeObjectUploadModel = new CompleteObjectUploadModel(uploadId, result.etag(), result.contentLength(), result.checksum());
        userObjectService.completeObjectUpload(completeObjectUploadModel);

        return new StoreObjectResponse(uploadId, result.etag(), new Checksum(objectUpload.getChecksumType(), result.checksum()), result.contentLength());
    }

    @Override
    public void abortObjectUpload(String uploadId) {
        userObjectService.abortObjectUpload(uploadId);
    }

    private void handleTrailers(ChunkedMetadata chunkedMetadata, String checksum, String trailerHeader, InputStream inputStream,
            boolean failOnMissingTrailers) {
        try {
            String trailer = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();

            String[] trailerLines = trailer.split("\n");

            if (trailerLines.length < 2 || !trailerLines[1].contains(":")) {
                if (failOnMissingTrailers) {
                    throw new InvalidTrailersException("Trailers not found");
                }
                return;
            }

            String expectedSignature = trailerLines[1].split(":")[1].trim();

            S3AuthenticationDetails s3AuthenticationDetails = s3AuthenticationDetails();
            Trailers trailers = new Trailers(chunkedMetadata.getCurrentChunkSignature(), trailerHeader, expectedSignature);

            TrailerSignatureValidator trailerSignatureValidator = new TrailerSignatureValidator(s3AuthenticationDetails, secretAccessKey());
            trailerSignatureValidator.validateSignature(trailers, checksum);
        } catch (IOException e) {
            throw new InvalidTrailersException("Error reading object trailers", e);
        }
    }

    private SaveObjectModel createSaveObjectModel(S3StoreObjectRequest storeObjectRequest, StoreObjectResponse storeObjectResponse, String objectName) {
        ObjectType objectType = storeObjectRequest.getKey().endsWith("/") ? ObjectType.DIRECTORY : ObjectType.FILE;

        return new SaveObjectModel(storeObjectResponse.objectVersion(), storeObjectRequest.getUserId(), objectType, objectName, storeObjectResponse.etag(),
                storeObjectRequest.getContentEncoding(), storeObjectRequest.getContentType(), storeObjectResponse.contentLength(),
                storeObjectResponse.checksum().checksumType(), storeObjectResponse.checksum().checksum());
    }

    private void preValidateUserObjectStorage(S3StoreObjectRequest objectStorageRequest) {
        if (objectStorageRequest.isIfNoneMatch() && s3UserObjectRepository.existsByUserIdAndObjectKey(objectStorageRequest.getUserId(),
                objectStorageRequest.getKey())) {
            throw new ObjectPreconditionFailedException();
        }

        String ifMatch = objectStorageRequest.getIfMatch();

        if (ifMatch != null && !s3UserObjectRepository.existsByUserIdAndObjectKeyAndEtag(objectStorageRequest.getUserId(), objectStorageRequest.getKey(),
                ifMatch)) {
            throw new ObjectPreconditionFailedException();
        }
    }

    private void preValidateUserObjectRetrieval(S3RetrieveObjectRequest objectRetrievalRequest, S3UserObject userObject) {
        String ifMatch = objectRetrievalRequest.getIfMatch();

        if (ifMatch != null && !ifMatch.equals(userObject.getEtag())) {
            throw new ObjectPreconditionFailedException();
        }

        String ifNoneMatch = objectRetrievalRequest.getIfNoneMatch();

        if (ifNoneMatch != null && ifNoneMatch.equals(userObject.getEtag())) {
            throw new ObjectPreconditionFailedException();
        }

        Instant ifModifiedSince = objectRetrievalRequest.getIfModifiedSince();

        if (ifModifiedSince != null && !userObject.getLastUpdated().isAfter(objectRetrievalRequest.getIfModifiedSince())) {
            throw new ObjectNotModifiedException();
        }

        Instant ifUnmodifiedSince = objectRetrievalRequest.getIfUnmodifiedSince();

        if (ifUnmodifiedSince != null && userObject.getLastUpdated().isAfter(objectRetrievalRequest.getIfUnmodifiedSince())) {
            throw new ObjectNotModifiedException();
        }
    }

    private String secretAccessKey() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication()).map(Authentication::getCredentials).map(String.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Invalid authentication found"));
    }

    private S3AuthenticationDetails s3AuthenticationDetails() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication()).filter(UsernamePasswordAuthenticationToken.class::isInstance)
                .map(UsernamePasswordAuthenticationToken.class::cast).map(UsernamePasswordAuthenticationToken::getDetails)
                .filter(S3AuthenticationDetails.class::isInstance).map(S3AuthenticationDetails.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Invalid authentication found"));
    }

    private Consumer<OutputStream> defaultOutputStreamConsumer(InputStream inputStream) {
        return outputStream -> {
            try {
                inputStream.transferTo(outputStream);
            } catch (IOException e) {
                throw new InvalidChunkException("Error reading object stream", e);
            }
        };
    }

    private Consumer<OutputStream> objectStreamConsumer(ChunkedMetadata chunkedMetadata, ChunkSignatureValidator chunkSignatureValidator,
            InputStream inputStream) {
        return outputStream -> {
            try {
                byte[] bytes = new byte[1];
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                while ((inputStream.read(bytes)) != -1) {
                    byteArrayOutputStream.writeBytes(bytes);

                    int value = bytes[0] & 0xff;

                    if (value == '\n') {
                        String metadata = byteArrayOutputStream.toString(StandardCharsets.UTF_8).trim();
                        chunkedMetadata.addMetadata(metadata);

                        int chunkSize = chunkedMetadata.getCurrentChunkSize();
                        byte[] content = inputStream.readNBytes(chunkSize);

                        if (!chunkSignatureValidator.validateSignature(chunkedMetadata, DigestUtils.sha256Hex(content))) {
                            throw new BadDigestException();
                        }

                        if (chunkSize == 0) {
                            break;
                        }

                        outputStream.write(content);

                        inputStream.skipNBytes(2);
                        byteArrayOutputStream.reset();
                    }
                }
            } catch (IOException e) {
                throw new InvalidChunkException("Error reading object stream", e);
            }
        };
    }

    private DelimiterResults handleDelimiter(List<S3UserObject> userObjects, String prefix, String delimiter) {
        if (delimiter == null) {
            return new DelimiterResults(userObjects, new ArrayList<>());
        }

        List<S3UserObject> updatedUserObjects = new ArrayList<>();
        Set<String> prefixes = new LinkedHashSet<>();

        for (S3UserObject userObject : userObjects) {
            String key = userObject.getObjectKey();
            String prefixedKey = prefix != null ? key.substring(prefix.length()) : key;

            if (prefixedKey.contains(delimiter)) {
                int delimiterIndex = prefixedKey.indexOf(delimiter);

                prefixes.add((prefix != null ? prefix : "") + prefixedKey.substring(0, delimiterIndex + 1));
            } else {
                updatedUserObjects.add(userObject);
            }
        }

        return new DelimiterResults(updatedUserObjects, new ArrayList<>(prefixes));
    }


    private record DelimiterResults(List<S3UserObject> userObjects, List<String> commonPrefixes) {
        public int keyCount() {
            return userObjects.size() + commonPrefixes.size();
        }
    }
}
