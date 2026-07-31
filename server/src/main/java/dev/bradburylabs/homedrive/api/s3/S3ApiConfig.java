package dev.bradburylabs.homedrive.api.s3;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;
import dev.bradburylabs.homedrive.model.s3.Bucket;
import dev.bradburylabs.homedrive.model.s3.CommonPrefix;
import dev.bradburylabs.homedrive.model.s3.CompleteMultipartUpload;
import dev.bradburylabs.homedrive.model.s3.CompleteMultipartUploadResult;
import dev.bradburylabs.homedrive.model.s3.DeletedObject;
import dev.bradburylabs.homedrive.model.s3.ErrorResponse;
import dev.bradburylabs.homedrive.model.s3.InitiateMultipartUploadResult;
import dev.bradburylabs.homedrive.model.s3.ListBucketsRequest;
import dev.bradburylabs.homedrive.model.s3.ListBucketsResponse;
import dev.bradburylabs.homedrive.model.s3.ListObjectV2Request;
import dev.bradburylabs.homedrive.model.s3.ListObjectsRequest;
import dev.bradburylabs.homedrive.model.s3.ListObjectsResult;
import dev.bradburylabs.homedrive.model.s3.ListObjectsV2Result;
import dev.bradburylabs.homedrive.model.s3.ObjectContent;
import dev.bradburylabs.homedrive.model.s3.ObjectIdentifier;
import dev.bradburylabs.homedrive.model.s3.S3DeleteObjectsRequest;
import dev.bradburylabs.homedrive.model.s3.S3DeleteObjectsResponse;
import dev.bradburylabs.homedrive.model.s3.Trailers;

@Configuration
@RegisterReflectionForBinding(
        {Bucket.class, CommonPrefix.class, DeletedObject.class, Error.class, ErrorResponse.class, ListBucketsRequest.class, ListBucketsResponse.class,
                ListObjectsRequest.class, ListObjectsResult.class, ListObjectsV2Result.class, ListObjectV2Request.class, ObjectContent.class,
                ObjectIdentifier.class, S3DeleteObjectsRequest.class, S3DeleteObjectsResponse.class, Trailers.class, InitiateMultipartUploadResult.class,
                CompleteMultipartUpload.class, CompleteMultipartUpload.Part.class, CompleteMultipartUploadResult.class, InitiateMultipartUploadResult.class})
public class S3ApiConfig {
}
