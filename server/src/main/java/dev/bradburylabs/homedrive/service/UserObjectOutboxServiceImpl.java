package dev.bradburylabs.homedrive.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.support.WindowIterator;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.FileSystemUtils;
import dev.bradburylabs.homedrive.entity.UserObjectOutbox;
import dev.bradburylabs.homedrive.exception.OutboxHandlingException;
import dev.bradburylabs.homedrive.properties.HomeDriveProperties;
import dev.bradburylabs.homedrive.repository.UserObjectOutboxRepository;
import dev.bradburylabs.homedrive.util.IdUtils;
import dev.bradburylabs.homedrive.util.PathUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserObjectOutboxServiceImpl implements UserObjectOutboxService {
    private final UserObjectOutboxRepository userObjectOutboxRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final HomeDriveProperties homeDriveProperties;

    @TransactionalEventListener
    @Async
    @Transactional
    @Retryable
    public void onOutboxUpdatedEvent(OutboxUpdatedEvent event) {
        userObjectOutboxRepository.findById(event.id()).ifPresent(this::handleOutboxEntry);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    @Retryable
    public void onSchedule() {
        WindowIterator<UserObjectOutbox> iterator =
                WindowIterator.of(scrollPosition -> userObjectOutboxRepository.findAllBy((KeysetScrollPosition) scrollPosition, Limit.of(25)))
                        .startingAt(ScrollPosition.keyset());

        iterator.forEachRemaining(this::handleOutboxEntry);
    }

    @Override
    @Transactional
    public void createOutboxEntry(String objectName, String userId, String previousVersion, String currentVersion) {
        String id = IdUtils.generateId();

        eventPublisher.publishEvent(new OutboxUpdatedEvent(id));

        UserObjectOutbox outboxEntry = new UserObjectOutbox(id, objectName, userId, previousVersion, currentVersion);

        userObjectOutboxRepository.save(outboxEntry);
    }

    private void handleOutboxEntry(UserObjectOutbox outboxEntry) {
        Path storageDirectory = PathUtils.calculateStorageDirectory(outboxEntry.getObjectName());
        Path basePath = Path.of(homeDriveProperties.getDataLocation(), outboxEntry.getUserId()).resolve(storageDirectory);

        String previousVersion = outboxEntry.getPreviousVersion();
        String currentVersion = outboxEntry.getCurrentVersion();

        try {
            if (previousVersion == null && currentVersion != null) {
                removeDeleteMarker(basePath.resolve(currentVersion));
            } else if (previousVersion != null && currentVersion == null) {
                deleteDirectory(basePath.resolve(previousVersion));
            } else if (previousVersion != null) {
                removeDeleteMarker(basePath.resolve(currentVersion));
                deleteDirectory(basePath.resolve(previousVersion));
            }

            userObjectOutboxRepository.delete(outboxEntry);
        } catch (IOException e) {
            throw new OutboxHandlingException("Unable to update delete markers", e);
        }
    }

    private void removeDeleteMarker(Path directory) throws IOException {
        Path deleteMarker = directory.resolve(".delete");
        Files.deleteIfExists(deleteMarker);
    }

    private void deleteDirectory(Path directory) throws IOException {
        // Create delete marker just incase deleting entire directory fails
        Path deleteMarker = directory.resolve(".delete");
        Files.createFile(deleteMarker);

        FileSystemUtils.deleteRecursively(directory);
    }

    public record OutboxUpdatedEvent(String id) {
    }
}
