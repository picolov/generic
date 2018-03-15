package com.baswara.generic.repository;

import com.baswara.generic.domain.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findBySender(String senderId);
    List<Notification> findByTarget(String targetId);
}
