package dev.bradburylabs.homedrive.processor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.support.WindowIterator;
import org.springframework.stereotype.Component;
import dev.bradburylabs.homedrive.entity.ObjectUploadPart;
import dev.bradburylabs.homedrive.exception.InvalidObjectUploadPartException;
import dev.bradburylabs.homedrive.exception.InvalidObjectUploadPartsOrderException;
import dev.bradburylabs.homedrive.model.object.ProcessObjectUploadPartResult;
import dev.bradburylabs.homedrive.model.s3.CompleteMultipartUpload;
import dev.bradburylabs.homedrive.repository.ObjectUploadPartRepository;
import dev.bradburylabs.homedrive.util.ChecksumType;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.checksums.SdkChecksum;
import tools.jackson.dataformat.xml.XmlMapper;

@Component
@RequiredArgsConstructor
public class ObjectUploadPartProcessorImpl implements ObjectUploadPartProcessor {
    private final XmlMapper xmlMapper;
    private final ObjectUploadPartRepository objectUploadPartRepository;

    @Override
    public ProcessObjectUploadPartResult processObjectUploadParts(String uploadId, InputStream inputStream, ChecksumType checksumType) {
        PartIterator<CompleteMultipartUpload.Part> requestPartIterator = createRequestPartIterator(inputStream);
        PartIterator<ObjectUploadPart> objectUploadPartPartIterator = createObjectUploadPartIterator(uploadId);

        CompleteMultipartUpload.Part requestPart = requestPartIterator.next();
        ObjectUploadPart objectUploadPart = objectUploadPartPartIterator.next();

        long contentLength = 0;
        byte[] etagBytes = new byte[0];
        byte[] checksumBytes = new byte[0];

        while (objectUploadPart != null) {
            if (requestPart == null || requestPart.partNumber() != objectUploadPart.getPartNumber()) {
                throw new InvalidObjectUploadPartsOrderException("Part number mismatch");
            }

            if (!requestPart.etag().equals(objectUploadPart.getEtag())) {
                throw new InvalidObjectUploadPartException("Etag mismatch");
            }

            contentLength += objectUploadPart.getContentLength();
            etagBytes = ArrayUtils.addAll(etagBytes, Base64.getDecoder().decode(objectUploadPart.getEtag()));

            if (checksumType != null && objectUploadPart.getChecksum() != null) {
                checksumBytes = ArrayUtils.addAll(checksumBytes, Base64.getDecoder().decode(objectUploadPart.getChecksum()));
            }

            requestPart = requestPartIterator.next();
            objectUploadPart = objectUploadPartPartIterator.next();
        }

        String etag = checksumString(ChecksumType.MD5, etagBytes);
        String checksum = checksumString(checksumType, checksumBytes);

        return new ProcessObjectUploadPartResult(etag, checksum, contentLength);
    }

    private PartIterator<CompleteMultipartUpload.Part> createRequestPartIterator(InputStream inputStream) {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        try {
            final XMLStreamReader streamReader = inputFactory.createXMLStreamReader(inputStream);

            return () -> {
                try {
                    while (streamReader.hasNext()) {
                        streamReader.next();

                        if (streamReader.getEventType() == XMLStreamConstants.START_ELEMENT && "Part".equals(streamReader.getLocalName())) {
                            return xmlMapper.readValue(streamReader, CompleteMultipartUpload.Part.class);
                        }
                    }

                    return null;
                } catch (XMLStreamException | IOException e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private PartIterator<ObjectUploadPart> createObjectUploadPartIterator(String uploadId) {
        final WindowIterator<ObjectUploadPart> iterator =
                WindowIterator.of(scrollPosition -> objectUploadPartRepository.findFirst10ByObjectUploadIdOrderByPartNumber(uploadId, scrollPosition))
                        .startingAt(ScrollPosition.keyset());

        return () -> {
            if (iterator.hasNext()) {
                return iterator.next();
            }

            return null;
        };
    }

    private String checksumString(ChecksumType checksumType, byte[] bytes) {
        if (checksumType == null) {
            return null;
        }

        SdkChecksum checksum = checksumType.getChecksum();
        checksum.update(bytes);

        return Base64.getEncoder().encodeToString(checksum.getChecksumBytes());
    }

    private interface PartIterator<T> {
        T next();
    }
}
