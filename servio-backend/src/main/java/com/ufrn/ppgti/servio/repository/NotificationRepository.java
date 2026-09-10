package com.ufrn.ppgti.servio.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ufrn.ppgti.servio.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    long countByRecipientIdAndReadFalse(Long recipientId);

    @Modifying(clearAutomatically = true)
    @Query("""
                UPDATE Notification n
                SET n.read = true, n.readAt = :now
                WHERE n.recipient.id = :recipientId AND n.read = false
            """)
    int markAllAsRead(@Param("recipientId") Long recipientId, @Param("now") LocalDateTime now);
}
