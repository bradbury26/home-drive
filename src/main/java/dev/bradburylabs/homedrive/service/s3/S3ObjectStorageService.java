package dev.bradburylabs.homedrive.service.s3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import dev.bradburylabs.homedrive.api.s3.exception.InvalidChunkException;
import dev.bradburylabs.homedrive.api.s3.exception.InvalidTrailersException;
import dev.bradburylabs.homedrive.api.s3.exception.ObjectPreconditionFailedException;
import dev.bradburylabs.homedrive.api.s3.security.S3AuthenticationDetails;
import dev.bradburylabs.homedrive.api.s3.signature.ChunkSignatureValidator;
import dev.bradburylabs.homedrive.api.s3.signature.TrailerSignatureValidator;
import dev.bradburylabs.homedrive.entity.UserObject;
import dev.bradburylabs.homedrive.exception.BadDigestException;
import dev.bradburylabs.homedrive.model.object.StoreObjectResponse;
import dev.bradburylabs.homedrive.model.s3.ChunkedMetadata;
import dev.bradburylabs.homedrive.model.s3.S3DeleteObjectRequest;
import dev.bradburylabs.homedrive.model.s3.S3StoreObjectRequest;
import dev.bradburylabs.homedrive.model.s3.Trailers;
import dev.bradburylabs.homedrive.properties.HomeDriveProperties;
import dev.bradburylabs.homedrive.repository.UserObjectRepository;
import dev.bradburylabs.homedrive.service.AbstractObjectStorageService;
import dev.bradburylabs.homedrive.service.UserObjectOutboxService;

@Service
public class S3ObjectStorageService extends AbstractObjectStorageService<S3StoreObjectRequest, S3DeleteObjectRequest> {
    public S3ObjectStorageService(UserObjectRepository userObjectRepository, UserObjectOutboxService userObjectOutboxService,
            HomeDriveProperties homeDriveProperties, TransactionTemplate transactionTemplate) {
        super(userObjectRepository, userObjectOutboxService, homeDriveProperties, transactionTemplate);
    }

    @Override
    public StoreObjectResponse storeObjectStream(S3StoreObjectRequest objectStorageRequest, InputStream inputStream, boolean trailers) {
        S3AuthenticationDetails s3AuthenticationDetails = s3AuthenticationDetails();
        ChunkSignatureValidator chunkSignatureValidator = new ChunkSignatureValidator(s3AuthenticationDetails, secretAccessKey());
        ChunkedMetadata chunkedMetadata = new ChunkedMetadata(s3AuthenticationDetails.signature());

        StoreObjectResponse result = storeObject(objectStorageRequest, outputStream -> {
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
        });

        if (trailers) {
            handleTrailers(chunkedMetadata, result.checksum().checksum(), objectStorageRequest, inputStream);
        }

        return result;
    }

    @Override
    protected void preValidateUserObject(S3StoreObjectRequest objectStorageRequest, UserObject userObject) {
        if (objectStorageRequest.isIfNoneMatch() && userObject.getObjectVersion() != null) {
            throw new ObjectPreconditionFailedException();
        }

        String ifMatch = objectStorageRequest.getIfMatch();

        if (ifMatch != null && !ifMatch.equals(userObject.getEtag())) {
            throw new ObjectPreconditionFailedException();
        }
    }

    @Override
    protected void validateDelete(S3DeleteObjectRequest objectDeleteRequest, UserObject userObject) {
        String ifMatch = objectDeleteRequest.getIfMatch();

        if (ifMatch != null && !ifMatch.equals(userObject.getEtag())) {
            throw new ObjectPreconditionFailedException();
        }
    }

    private void handleTrailers(ChunkedMetadata chunkedMetadata, String checksum, S3StoreObjectRequest objectStorageRequest, InputStream inputStream) {
        try {
            String trailer = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();

            String[] trailerLines = trailer.split("\n");

            if (trailerLines.length < 2 || !trailerLines[1].contains(":")) {
                throw new InvalidTrailersException("Trailers not found");
            }

            String expectedSignature = trailerLines[1].split(":")[1].trim();

            S3AuthenticationDetails s3AuthenticationDetails = s3AuthenticationDetails();
            Trailers trailers = new Trailers(chunkedMetadata.getCurrentChunkSignature(), objectStorageRequest.getTrailerHeader(), expectedSignature);

            TrailerSignatureValidator trailerSignatureValidator = new TrailerSignatureValidator(s3AuthenticationDetails, secretAccessKey());
            trailerSignatureValidator.validateSignature(trailers, checksum);
        } catch (IOException e) {
            throw new InvalidTrailersException("Error reading object trailers", e);
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
}
