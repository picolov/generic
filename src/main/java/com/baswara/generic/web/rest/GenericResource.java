package com.baswara.generic.web.rest;

import com.baswara.generic.domain.Meta;
import com.baswara.generic.repository.MetaRepository;
import com.baswara.generic.service.GenericService;
import com.baswara.generic.web.rest.errors.MetaClassNotFoundException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller for view and managing Log Level at runtime.
 */
@RestController
@RequestMapping("/generic")
public class GenericResource {

    private final GenericService genericService;
    private final MetaRepository metaRepository;

    public GenericResource(GenericService genericService, MetaRepository metaRepository) {
        this.genericService = genericService;
        this.metaRepository = metaRepository;
    }

    @GetMapping("/count/{_class}")
    public ResponseEntity<Object> count(@PathVariable String _class) {
        long count = genericService.count(_class);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/exist/{_class}/{id}")
    public ResponseEntity<Object> existsById(@PathVariable String _class, @PathVariable String id) {
        boolean result = genericService.existsById(_class, id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{_class}")
    public ResponseEntity<Object> deleteAll(@PathVariable String _class) {
        DBObject result = new BasicDBObject();
        genericService.deleteAll(_class);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{_class}/{id}")
    public ResponseEntity<Object> deleteById(@PathVariable String _class, @PathVariable String id) {
        DBObject result = new BasicDBObject();
        genericService.deleteById(_class, id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{_class}")
    public ResponseEntity<Object> findAll(@PathVariable String _class) {
        List<DBObject> result = genericService.findAll(_class);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{_class}/{id}")
    public ResponseEntity<Object> findById(@PathVariable String _class, @PathVariable String id, @RequestParam("maxLevel") String maxLevelStr) {
        int maxLevel = 1;
        if (maxLevelStr != null) maxLevel = Integer.parseInt(maxLevelStr);
        return new ResponseEntity<>(getObject(_class, id, 0, maxLevel), HttpStatus.OK);
    }

    private DBObject getObject(String _class, String id, int currLevel, int maxLevel) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        if (metaExist.isPresent()) {
            Meta meta = metaExist.get();
            DBObject result = new BasicDBObject();
            DBObject resp = genericService.findById(_class, id);
            for (String key : meta.getColumns().keySet()) {
                Map columnMap = meta.getColumns().get(key);
                switch ((String) columnMap.get("type")) {
                    case "ref":
                        if (resp.get(key) != null && currLevel+1 <= maxLevel) {
                            String classRef = (String) columnMap.get("classRef");
                            result.put(key, getObject(classRef, (String) resp.get(key), currLevel + 1, maxLevel));
                        } else {
                            result.put(key, resp.get(key));
                        }
                        break;
                    default:
                        result.put(key, resp.get(key));
                }
            }
            return result;
        } else {
            throw new MetaClassNotFoundException();
        }
    }

    @PostMapping("/{_class}")
    public ResponseEntity<Object> save(@PathVariable String _class, @RequestBody BasicDBObject objParam) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        if (metaExist.isPresent()) {
            BasicDBObject objToSave = new BasicDBObject();
            Meta meta = metaExist.get();
            for (String key : meta.getColumns().keySet()) {
                Map columnMap = meta.getColumns().get(key);
                switch ((String) columnMap.get("type")) {
                    case "ref":
                    case "string":
                    case "numeric":
                    case "date":
                    default:
                        objToSave.put(key, objParam.get(key));
                        break;
                }
            }
            DBObject result = genericService.save(_class, objToSave);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            throw new MetaClassNotFoundException();
        }
    }

    @PostMapping("/bulk/{_class}")
    public ResponseEntity<Object> saveList(@PathVariable String _class, @RequestBody List<BasicDBObject> objParamList) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        if (metaExist.isPresent()) {
            List<DBObject> objList = new ArrayList<>();
            for (DBObject objParam:objParamList) {
                DBObject objToSave = new BasicDBObject();
                Meta meta = metaExist.get();
                for (String key : meta.getColumns().keySet()) {
                    Map columnMap = meta.getColumns().get(key);
                    switch ((String) columnMap.get("type")) {
                        case "ref":
                        case "string":
                        case "numeric":
                        case "date":
                        default:
                            objToSave.put(key, objParam.get(key));
                            break;
                    }
                }
                objList.add(objToSave);
            }
            List<DBObject> result = genericService.saveList(_class, objList);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            throw new MetaClassNotFoundException();
        }

    }
}
