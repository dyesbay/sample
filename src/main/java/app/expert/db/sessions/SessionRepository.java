package app.expert.db.sessions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findFirstByManagerAndDisabledIsNull(Long manager);

    Session findByManager(Long manager);
}
