package com.baswara.generic.web.rest;

import com.baswara.generic.domain.Meta;
import com.baswara.generic.repository.MetaRepository;
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
@RequestMapping("/meta")
public class MetaResource {

    private final MetaRepository metaRepository;

    public MetaResource(MetaRepository metaRepository) {
        this.metaRepository = metaRepository;
    }

    @GetMapping("/count")
    public ResponseEntity<Object> count() {
        long count = metaRepository.count();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/exist/{id}")
    public ResponseEntity<Object> existsById(@PathVariable String id) {
        boolean result = metaRepository.exists(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("")
    public ResponseEntity<Object> deleteAll() {
        Map<String, Object> result = new HashMap<>();
        metaRepository.deleteAll();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteById(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        metaRepository.delete(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<Object> findAll() {
        List<Meta> result = metaRepository.findAll();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable String id) {
        Meta result = metaRepository.findOne(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Object> save(@RequestBody Meta objParam) {
        Meta result = metaRepository.save(objParam);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/bulk")
    public ResponseEntity<Object> saveList(@RequestBody List<Meta> objParamList) {
        List<Meta> result = metaRepository.save(objParamList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
