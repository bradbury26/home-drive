package dev.bradburylabs.homedrive.s3;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.checksums.internal.DigestAlgorithm;
import software.amazon.awssdk.checksums.internal.DigestAlgorithmChecksum;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ChecksumAlgorithm;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;

public class S3Test {
    private static final Logger log = LoggerFactory.getLogger(S3Test.class);

    private static final String accessKeyId = "5lUck1uVoe3IaDuOI3Jq";
    private static final String secretAccessKey = "fCiHUYloWgy7stbSE4dw";


    @Test
    public void testGetObject() {
        StaticCredentialsProvider staticCredentialsProvider =
                StaticCredentialsProvider.create(AwsBasicCredentials.create("003629e92c3ca050000000003", "K003d4qmYM7uXugMlFvYJj12xgsNwYM"));

        try (S3Client client = S3Client.builder().endpointOverride(URI.create("https://s3.eu-central-003.backblazeb2.com")).forcePathStyle(true)
                .credentialsProvider(staticCredentialsProvider).region(Region.of("test")).build()) {
            GetObjectResponse response = client.getObject(GetObjectRequest.builder().bucket("bradburylabs-immich").key("config").build(),
                    ResponseTransformer.toFile(Path.of("config")));

            log.info("Response: {}", response);
        }
    }

    @Test
    public void testPutObject() throws IOException, DecoderException {
        StaticCredentialsProvider staticCredentialsProvider =
                StaticCredentialsProvider.create(AwsBasicCredentials.create("S9PAEITqsL4D4sMfzY1u", "R1Zx2NJhusDDQtRwZQog"));

        try (S3Client client = S3Client.builder().endpointOverride(URI.create("http://localhost:8080")).forcePathStyle(true).region(Region.of("bradburylabs"))
                .credentialsProvider(staticCredentialsProvider).serviceConfiguration(builder -> builder.chunkedEncodingEnabled(true)).build()) {

            byte[] bytes = S3Test.class.getResourceAsStream("/image.png").readAllBytes();
            String s = DigestUtils.sha256Hex(bytes);
            String base64 = Base64.getEncoder().encodeToString(Hex.decodeHex(s));

            client.putObject(PutObjectRequest.builder().bucket("admin").key("uploaded.png").contentType(MediaType.IMAGE_PNG_VALUE)
                    .checksumAlgorithm(ChecksumAlgorithm.SHA256).checksumSHA256(base64).build(), RequestBody.fromBytes(bytes));
        }
    }

    @Test
    public void testMutlipartUpload() throws IOException {
        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));

        try (S3Client client = S3Client.builder().endpointOverride(URI.create("http://localhost:8080")).forcePathStyle(true).region(Region.of("bradburylabs"))
                .credentialsProvider(staticCredentialsProvider).serviceConfiguration(builder -> builder.chunkedEncodingEnabled(true)).build()) {

            CreateMultipartUploadResponse multipart =
                    client.createMultipartUpload(CreateMultipartUploadRequest.builder().bucket("admin").key("Pokemon Y.cci").build());

            InputStream resourceAsStream = S3Test.class.getResourceAsStream("/Pokemon Y.cci");

            int i = 1;
            byte[] buffer = resourceAsStream.readNBytes(5242880);
            List<CompletedPart> parts = new ArrayList<>();
            while (buffer.length > 0) {
                client.uploadPart(UploadPartRequest.builder().bucket("admin").key("Pokemon Y.cci").uploadId(multipart.uploadId()).partNumber(i).build(),
                        RequestBody.fromBytes(buffer));

                String etag = Base64.getEncoder().encodeToString(DigestUtils.md5(buffer));

                parts.add(CompletedPart.builder().partNumber(i).eTag(etag).build());

                i++;
                buffer = resourceAsStream.readNBytes(5242880);
            }

            client.completeMultipartUpload(CompleteMultipartUploadRequest.builder().bucket("admin").key("Pokemon Y.cci").uploadId(multipart.uploadId())
                    .multipartUpload(CompletedMultipartUpload.builder().parts(parts).build()).build());
        }
    }

    @Test
    public void testMd5() throws IOException {
        byte[] bytes = S3Test.class.getResourceAsStream("/image.png").readAllBytes();
        DigestAlgorithmChecksum checksum = new DigestAlgorithmChecksum(DigestAlgorithm.MD5);

        checksum.update(bytes);

        String base64 = Base64.getEncoder().encodeToString(checksum.getChecksumBytes());
        String base642 = Base64.getEncoder().encodeToString(Hex.encodeHexString(checksum.getChecksumBytes()).getBytes(StandardCharsets.UTF_8));

        System.out.println(base64);
        System.out.println(base642);
    }
}
