package com.baswara.generic.web.rest;

import com.baswara.generic.domain.Layout;
import com.baswara.generic.repository.LayoutRepository;
import com.mongodb.DBObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for view and managing Log Level at runtime.
 */
@RestController
@RequestMapping("/layout")
public class LayoutResource {

    private final LayoutRepository layoutRepository;

    public LayoutResource(LayoutRepository layoutRepository) {
        this.layoutRepository = layoutRepository;
    }

    @GetMapping("/count")
    public ResponseEntity<Object> count() {
        long count = layoutRepository.count();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/exist/{id}")
    public ResponseEntity<Object> existsById(@PathVariable String id) {
        boolean result = layoutRepository.exists(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("")
    public ResponseEntity<Object> deleteAll() {
        Map<String, Object> result = new HashMap<>();
        layoutRepository.deleteAll();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteById(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        layoutRepository.delete(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<Object> findAll() {
        List<Layout> result = layoutRepository.findAll();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable String id) {
        Layout result = layoutRepository.findOne(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Object> save(@RequestBody Layout objParam) {
        Layout result = layoutRepository.save(objParam);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/bulk")
    public ResponseEntity<Object> saveList(@RequestBody List<Layout> objParamList) {
        List<Layout> result = layoutRepository.save(objParamList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
