package dev.bradburylabs.homedrive.service.s3;

import java.io.InputStream;
import dev.bradburylabs.homedrive.model.object.CreateObjectUploadModel;
import dev.bradburylabs.homedrive.model.object.RetrieveObjectResponse;
import dev.bradburylabs.homedrive.model.object.StoreObjectResponse;
import dev.bradburylabs.homedrive.model.s3.ListObjectV2Request;
import dev.bradburylabs.homedrive.model.s3.ListObjectsRequest;
import dev.bradburylabs.homedrive.model.s3.ListObjectsResult;
import dev.bradburylabs.homedrive.model.s3.ListObjectsV2Result;
import dev.bradburylabs.homedrive.model.s3.S3DeleteObjectRequest;
import dev.bradburylabs.homedrive.model.s3.S3RetrieveObjectRequest;
import dev.bradburylabs.homedrive.model.s3.S3StoreObjectRequest;

public interface S3ObjectService {
    RetrieveObjectResponse retrieveObject(S3RetrieveObjectRequest retrieveObjectRequest, boolean retrieveInputStream);

    ListObjectsResult listObjects(String userId, ListObjectsRequest request);

    ListObjectsV2Result listObjectsV2(String userId, ListObjectV2Request request);

    StoreObjectResponse uploadSinglePartObject(S3StoreObjectRequest storeObjectRequest, InputStream inputStream);

    StoreObjectResponse uploadChunkedSinglePartObject(S3StoreObjectRequest storeObjectRequest, InputStream inputStream, boolean trailers);

    void deleteObject(S3DeleteObjectRequest deleteObjectRequest);

    String createObjectUpload(CreateObjectUploadModel createObjectUploadModel);

    StoreObjectResponse uploadObjectPart(String uploadId, int partNumber, S3StoreObjectRequest storeObjectRequest, InputStream inputStream);

    StoreObjectResponse completeObjectUpload(String uploadId, InputStream inputStream);

    void abortObjectUpload(String uploadId);
}
