package com.mygaadi.repository;

import com.mygaadi.model.entity.AppNotification;
import com.mygaadi.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findByUserAndDeletedFalseOrderByCreatedAtDesc(User user);
    long countByUserAndReadFalseAndDeletedFalse(User user);
}
