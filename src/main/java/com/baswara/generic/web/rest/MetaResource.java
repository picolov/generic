package com.baswara.generic.web.rest;

import com.baswara.generic.domain.Meta;
import com.baswara.generic.repository.MetaRepository;
import com.baswara.generic.web.rest.errors.MetaClassNotFoundException;
import com.mongodb.DBObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @PostMapping("/link/{_class1}/{_class2}")
    public ResponseEntity<Object> link(@PathVariable String _class1, @PathVariable String _class2) {
        Optional<Meta> meta1Exist = metaRepository.findOneByName(_class1);
        Optional<Meta> meta2Exist = metaRepository.findOneByName(_class2);
        if (meta1Exist.isPresent() && meta2Exist.isPresent()) {
            Meta meta1 = meta1Exist.get();
            Meta meta2 = meta2Exist.get();
            Meta metaLink = new Meta();
            metaLink.setName(meta1.getName() + "-" + meta2.getName());
            Map<String, Map<String, Object>> columnMap = new HashMap<>();
            Map<String, Object> column = new HashMap<>();
            column.put("name", meta1.getName());
            column.put("type", "ref");
            columnMap.put(meta1.getName(), column);
            column = new HashMap<>();
            column.put("name", meta2.getName());
            column.put("type", "ref");
            columnMap.put(meta2.getName(), column);
            metaLink.setColumns(columnMap);
            // update meta 1, add link to columns
            Map<String, Map<String, Object>> columnMapMeta1 = meta1.getColumns();
            if (!columnMapMeta1.containsKey(meta2.getName())) {
                column = new HashMap<>();
                column.put("name", meta2.getName());
                column.put("type", "link");
                column.put("relModel", metaLink.getName());
                columnMapMeta1.put(meta2.getName(), column);
            }
            metaRepository.save(meta1);
            // update meta 2, add link to columns
            Map<String, Map<String, Object>> columnMapMeta2 = meta2.getColumns();
            if (!columnMapMeta2.containsKey(meta1.getName())) {
                column = new HashMap<>();
                column.put("name", meta1.getName());
                column.put("type", "link");
                column.put("relModel", metaLink.getName());
                columnMapMeta2.put(meta1.getName(), column);
            }
            metaRepository.save(meta2);
            Meta result = metaRepository.save(metaLink);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            throw new MetaClassNotFoundException();
        }
    }
}
