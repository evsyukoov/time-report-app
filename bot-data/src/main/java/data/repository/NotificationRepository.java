package data.repository;

import hibernate.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> getAllByNextFireTimeBefore(LocalDateTime time);

    Notification getAllByUid(long uid);
}
