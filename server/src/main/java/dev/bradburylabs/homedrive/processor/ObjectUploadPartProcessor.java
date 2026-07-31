package dev.bradburylabs.homedrive.processor;

import java.io.InputStream;
import dev.bradburylabs.homedrive.model.object.ProcessObjectUploadPartResult;
import dev.bradburylabs.homedrive.util.ChecksumType;

public interface ObjectUploadPartProcessor {
    ProcessObjectUploadPartResult processObjectUploadParts(String uploadId, InputStream inputStream, ChecksumType checksumType);
}
