package dev.bradburylabs.homedrive.service.s3;

import dev.bradburylabs.homedrive.model.s3.ListObjectV2Request;
import dev.bradburylabs.homedrive.model.s3.ListObjectsRequest;
import dev.bradburylabs.homedrive.model.s3.ListObjectsResult;
import dev.bradburylabs.homedrive.model.s3.ListObjectsV2Result;

public interface S3ListObjectsService {
    ListObjectsResult listObjects(String userId, ListObjectsRequest request);

    ListObjectsV2Result listObjectsV2(String userId, ListObjectV2Request request);
}
