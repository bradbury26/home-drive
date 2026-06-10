package dev.bradburylabs.homedrive.repository;

import java.util.Optional;
import org.hibernate.jpa.SpecHints;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import dev.bradburylabs.homedrive.entity.UserObjectOutbox;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface UserObjectOutboxRepository extends JpaRepository<UserObjectOutbox, String> {
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = SpecHints.HINT_SPEC_QUERY_TIMEOUT, value = "-2"))
    Optional<UserObjectOutbox> findById(String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = SpecHints.HINT_SPEC_QUERY_TIMEOUT, value = "-2"))
    Window<UserObjectOutbox> findAllBy(KeysetScrollPosition position, Limit limit);
}
