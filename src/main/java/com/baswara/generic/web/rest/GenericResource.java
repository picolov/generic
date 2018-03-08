package com.baswara.generic.web.rest;

import com.baswara.generic.service.GenericService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller for view and managing Log Level at runtime.
 */
@RestController
@RequestMapping("/class")
public class GenericResource {

    private final GenericService genericService;

    public GenericResource(GenericService genericService) {
        this.genericService = genericService;
    }

    @GetMapping("/count/{_class}")
    public ResponseEntity<Object> count(@PathVariable String _class,
                                        @RequestParam(value = "criteria", required = false) String criteria) {
        long count = genericService.count(_class, criteria);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/exist/{_class}/{id}")
    public ResponseEntity<Object> existsById(@PathVariable String _class, @PathVariable String id) {
        boolean result = genericService.existsById(_class, id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{_class}")
    public ResponseEntity<Object> deleteAll(@PathVariable String _class) {
        genericService.deleteAll(_class);
        return new ResponseEntity<>(new HashMap<String, Map>(), HttpStatus.OK);
    }

    @DeleteMapping("/{_class}/{id}")
    public ResponseEntity<Object> deleteById(@PathVariable String _class, @PathVariable String id) {
        genericService.deleteById(_class, id);
        return new ResponseEntity<>(new HashMap<String, Map>(), HttpStatus.OK);
    }

    @GetMapping("/{_class}")
    public ResponseEntity<Object> findAll(
        @PathVariable String _class,
        @RequestParam(value = "level", required = false, defaultValue = "1") int level,
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        @RequestParam(value = "size", required = false, defaultValue = "100") int size,
        @RequestParam(value = "sort", required = false) String sort,
        @RequestParam(value = "criteria", required = false) String criteria,
        @RequestParam(value = "fields", required = false) String fields) {
        List<DBObject> resultList = genericService.findAllPaging(_class, criteria, level, page, size, sort, fields);
        return new ResponseEntity<>(resultList, HttpStatus.OK);
    }

    @GetMapping("/findAllWithTotal/{_class}")
    public ResponseEntity<Object> findAllWithTotal(
        @PathVariable String _class,
        @RequestParam(value = "level", required = false, defaultValue = "1") int level,
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        @RequestParam(value = "size", required = false, defaultValue = "100") int size,
        @RequestParam(value = "sort", required = false) String sort,
        @RequestParam(value = "criteria", required = false) String criteria,
        @RequestParam(value = "fields", required = false) String fields) {
        long count = genericService.count(_class, criteria);
        List<DBObject> resultList = genericService.findAllPaging(_class, criteria, level, page, size, sort, fields);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalRows", count);
        resultMap.put("list", resultList);
        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    @GetMapping("/{_class}/{id}")
    public ResponseEntity<Object> findById(
        @PathVariable String _class, @PathVariable String id,
        @RequestParam(value = "level", required = false, defaultValue = "1") int level,
        @RequestParam(value = "fields", required = false) String fields) {
        List<String> fieldList = null;
        if (fields != null) {
            String[] fieldToken = fields.split(",");
            fieldList = Arrays.asList(fieldToken);
        }
        DBObject resp = genericService.getObject(_class, id, 0, level);
        DBObject result = new BasicDBObject();
        result.put("_id", resp.get("_id"));
        for (String key : resp.keySet()) {
            String[] keyToken = key.split("_");
            if (fieldList != null && !fieldList.contains(keyToken[0])) continue;
            result.put(key, resp.get(key));
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }



    @PostMapping("/{_class}")
    public ResponseEntity<Object> save(@PathVariable String _class, @RequestBody BasicDBObject objParam) {
        DBObject result = genericService.saveModel(_class, objParam);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/{_class}")
    public ResponseEntity<Object> update(@PathVariable String _class, @RequestBody BasicDBObject objParam) {
        DBObject result = genericService.updateModel(_class, objParam);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/bulk/{_class}")
    public ResponseEntity<Object> saveList(@PathVariable String _class, @RequestBody List<BasicDBObject> objParamList) {
        List<DBObject> result = genericService.saveModelBulk(_class, objParamList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
