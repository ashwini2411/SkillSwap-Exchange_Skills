package com.skillswap.platform.repository;

import com.skillswap.platform.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderByCreatedAtDesc();
    
    void deleteByCreatedAtBefore(LocalDateTime expiryTime);
}
