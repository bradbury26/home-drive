package dev.bradburylabs.homedrive.service.s3;

import dev.bradburylabs.homedrive.model.s3.ListBucketsRequest;
import dev.bradburylabs.homedrive.model.s3.ListBucketsResponse;

public interface S3BucketService {
    ListBucketsResponse listBuckets(String userId, ListBucketsRequest request);
}
