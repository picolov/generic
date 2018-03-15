package com.baswara.generic.web.rest;

import com.baswara.generic.domain.Notification;
import com.baswara.generic.repository.NotificationRepository;
import com.baswara.generic.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notification")
public class NotificationResource {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public NotificationResource(NotificationRepository notificationRepository, NotificationService notificationService) {
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    @GetMapping("/count")
    public ResponseEntity<Object> count() {
        long count = notificationRepository.count();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/exist/{id}")
    public ResponseEntity<Object> existsById(@PathVariable String id) {
        boolean result = notificationRepository.exists(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/getForMe")
    public ResponseEntity<Object> getForMe() {
        List<Map<String, Object>> result = notificationService.getForMe();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/getByMe")
    public ResponseEntity<Object> getByMe() {
        List<Map<String, Object>> result = notificationService.getByMe();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("")
    public ResponseEntity<Object> deleteAll() {
        Map<String, Object> result = new HashMap<>();
        notificationRepository.deleteAll();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteById(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        notificationRepository.delete(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<Object> findAll() {
        List<Notification> result = notificationRepository.findAll();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable String id) {
        Notification result = notificationRepository.findOne(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Object> save(@RequestBody Notification objParam) {
        Notification result = notificationRepository.save(objParam);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("")
    public ResponseEntity<Object> update(@RequestBody Notification objParam) {
        Notification notification = notificationRepository.findOne(objParam.getId());
        notification.setRoute(objParam.getRoute());
        notification.setSubject(objParam.getSubject());
        notification.setDate(objParam.getDate());
        notification.setSender(objParam.getSender());
        notification.setTarget(objParam.getTarget());
        notification.setIsRead(objParam.getIsRead());
        notificationRepository.save(notification);
        return new ResponseEntity<>(notification, HttpStatus.OK);
    }

    @PostMapping("/bulk")
    public ResponseEntity<Object> saveList(@RequestBody List<Notification> objParamList) {
        List<Notification> result = notificationRepository.save(objParamList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
