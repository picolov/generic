package com.baswara.generic.service;

import com.baswara.generic.client.AccountFeignClient;
import com.baswara.generic.domain.Notification;
import com.baswara.generic.repository.NotificationRepository;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class NotificationService {

    private final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final AccountFeignClient accountFeignClient;
    private final GenericService genericService;

    public NotificationService(NotificationRepository notificationRepository, AccountFeignClient accountFeignClient, GenericService genericService) {
        this.notificationRepository = notificationRepository;
        this.accountFeignClient = accountFeignClient;
        this.genericService = genericService;
    }

    public List<Map<String, Object>> getForMe() {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> account = accountFeignClient.getAccount();
        DBObject myProfile = genericService.findOne("userProfile", "userId;filter;" + account.get("login"), 0, null);
        List<Notification> notificationList = notificationRepository.findByTarget((String) account.get("login"));
        for (Notification notification:notificationList) {
            DBObject userProfile = genericService.findOne("userProfile", "userId;filter;" + notification.getSender(), 0, null);
            Map<String, Object> notifMap = new HashMap<>();
            notifMap.put("_id", notification.getId());
            notifMap.put("subject", notification.getSubject());
            notifMap.put("route", notification.getRoute());
            notifMap.put("date", notification.getDate());
            notifMap.put("sender", userProfile);
            notifMap.put("target", myProfile);
            notifMap.put("isRead", notification.getIsRead());
            result.add(notifMap);
        }
        return result;
    }

    public List<Map<String, Object>> getByMe() {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> account = accountFeignClient.getAccount();
        DBObject myProfile = genericService.findOne("userProfile", "userId;filter;" + account.get("login"), 0, null);
        List<Notification> notificationList = notificationRepository.findBySender((String) account.get("login"));
        for (Notification notification:notificationList) {
            DBObject userProfile = genericService.findOne("userProfile", "userId;filter;" + notification.getTarget(), 0, null);
            Map<String, Object> notifMap = new HashMap<>();
            notifMap.put("_id", notification.getId());
            notifMap.put("subject", notification.getSubject());
            notifMap.put("route", notification.getRoute());
            notifMap.put("date", notification.getDate());
            notifMap.put("sender", myProfile);
            notifMap.put("target", userProfile);
            notifMap.put("isRead", notification.getIsRead());
            result.add(notifMap);
        }
        return result;
    }

}
