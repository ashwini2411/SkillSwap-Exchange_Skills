package com.skillswap.platform.service;

import com.skillswap.platform.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationCleanupService {

    @Autowired
    private NotificationRepository notificationRepository;

    // Run every hour (3600000 ms) to clean up old notifications
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        notificationRepository.deleteByCreatedAtBefore(twentyFourHoursAgo);
        System.out.println("Scheduler: Cleaned up notifications older than " + twentyFourHoursAgo);
    }
}
