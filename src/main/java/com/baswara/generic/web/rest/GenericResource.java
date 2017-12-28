package com.baswara.generic.web.rest;

import com.baswara.generic.domain.Meta;
import com.baswara.generic.repository.MetaRepository;
import com.baswara.generic.service.GenericService;
import com.baswara.generic.web.rest.errors.MetaClassNotFoundException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Controller for view and managing Log Level at runtime.
 */
@RestController
@RequestMapping("/class")
public class GenericResource {

    private final GenericService genericService;
    private final MetaRepository metaRepository;// 2017-12-08T17:00:00.000Z
    private final SimpleDateFormat sdfDateDataType = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private Map<String, Object> ID_MAP = new HashMap<>();

    public GenericResource(GenericService genericService, MetaRepository metaRepository) {
        this.genericService = genericService;
        this.metaRepository = metaRepository;
        sdfDateDataType.setTimeZone(TimeZone.getTimeZone("UTC"));
        ID_MAP.put("name", "_id");
        ID_MAP.put("type", "string");
    }

    @GetMapping("/count/{_class}")
    public ResponseEntity<Object> count(@PathVariable String _class,
                                        @RequestParam(value = "criteria", required = false) String criteria) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        if (metaExist.isPresent()) {
            Meta meta = metaExist.get();
            meta.getColumns().put("_id", ID_MAP);
            long count = genericService.count(_class, criteria, meta);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } else {
            throw new MetaClassNotFoundException();
        }
    }

    @GetMapping("/exist/{_class}/{id}")
    public ResponseEntity<Object> existsById(@PathVariable String _class, @PathVariable String id) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        if (metaExist.isPresent()) {
            boolean result = genericService.existsById(_class, id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            throw new MetaClassNotFoundException();
        }
    }

    @DeleteMapping("/{_class}")
    public ResponseEntity<Object> deleteAll(@PathVariable String _class) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        if (metaExist.isPresent()) {
            DBObject result = new BasicDBObject();
            genericService.deleteAll(_class);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            throw new MetaClassNotFoundException();
        }
    }

    @DeleteMapping("/{_class}/{id}")
    public ResponseEntity<Object> deleteById(@PathVariable String _class, @PathVariable String id) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        if (metaExist.isPresent()) {
            DBObject result = new BasicDBObject();
            genericService.deleteById(_class, id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            throw new MetaClassNotFoundException();
        }
    }

    @GetMapping("/{_class}")
    public ResponseEntity<Object> findAll(@PathVariable String _class,
                                          @RequestParam(value = "level", required = false, defaultValue = "1") int level,
                                          @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                          @RequestParam(value = "size", required = false, defaultValue = "100") int size,
                                          @RequestParam(value = "sort", required = false) String sort,
                                          @RequestParam(value = "criteria", required = false) String criteria) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        if (metaExist.isPresent()) {
            Meta meta = metaExist.get();
            meta.getColumns().put("_id", ID_MAP);
            List<DBObject> resultList = new ArrayList<>();
            List<Sort.Order> orderList = new ArrayList<>();
            Pageable pageable;
            if (sort != null) {
                String[] sortTokenArr = sort.split(",");
                for (String sortToken : sortTokenArr) {
                    String[] token = sortToken.split(";");
                    orderList.add(new Sort.Order(token[1].equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, token[0]));
                }
                pageable = new PageRequest(page, size, new Sort(orderList));
            } else {
                pageable = new PageRequest(page, size);
            }
            List<DBObject> respList = genericService.findAllPaging(_class, criteria, pageable, meta);
            for (DBObject resp:respList) {
                DBObject result = new BasicDBObject();
                result.put("_id", resp.get("_id"));
                for (String key : meta.getColumns().keySet()) {
                    Map columnMap = meta.getColumns().get(key);
                    switch ((String) columnMap.get("type")) {
                        case "ref":
                            if (resp.get(key) != null && 1 <= level) {
                                String classRef = (String) columnMap.get("classRef");
                                result.put(key, getObject(classRef, (String) resp.get(key), 1, level));
                            } else {
                                result.put(key, resp.get(key));
                            }
                            break;
                        default:
                            result.put(key, resp.get(key));
                    }
                }
                resultList.add(result);
            }
            return new ResponseEntity<>(resultList, HttpStatus.OK);
        } else {
            throw new MetaClassNotFoundException();
        }
    }

    @GetMapping("/findAllWithTotal/{_class}")
    public ResponseEntity<Object> findAllWithTotal(@PathVariable String _class,
                                          @RequestParam(value = "level", required = false, defaultValue = "1") int level,
                                          @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                          @RequestParam(value = "size", required = false, defaultValue = "100") int size,
                                          @RequestParam(value = "sort", required = false) String sort,
                                          @RequestParam(value = "criteria", required = false) String criteria) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        if (metaExist.isPresent()) {
            Meta meta = metaExist.get();
            meta.getColumns().put("_id", ID_MAP);
            long count = genericService.count(_class, criteria, meta);
            List<DBObject> resultList = new ArrayList<>();
            List<Sort.Order> orderList = new ArrayList<>();
            Pageable pageable;
            if (sort != null) {
                String[] sortTokenArr = sort.split(",");
                for (String sortToken : sortTokenArr) {
                    String[] token = sortToken.split(";");
                    orderList.add(new Sort.Order(token[1].equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, token[0]));
                }
                pageable = new PageRequest(page, size, new Sort(orderList));
            } else {
                pageable = new PageRequest(page, size);
            }
            List<DBObject> respList = genericService.findAllPaging(_class, criteria, pageable, meta);
            for (DBObject resp:respList) {
                DBObject result = new BasicDBObject();
                result.put("_id", resp.get("_id"));
                for (String key : meta.getColumns().keySet()) {
                    Map columnMap = meta.getColumns().get(key);
                    switch ((String) columnMap.get("type")) {
                        case "ref":
                            if (resp.get(key) != null && 1 <= level) {
                                String classRef = (String) columnMap.get("classRef");
                                result.put(key, getObject(classRef, (String) resp.get(key), 1, level));
                            } else {
                                result.put(key, resp.get(key));
                            }
                            break;
                        default:
                            result.put(key, resp.get(key));
                    }
                }
                resultList.add(result);
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("totalRows", count);
            resultMap.put("list", resultList);
            return new ResponseEntity<>(resultMap, HttpStatus.OK);
        } else {
            throw new MetaClassNotFoundException();
        }
    }

    @GetMapping("/{_class}/{id}")
    public ResponseEntity<Object> findById(@PathVariable String _class, @PathVariable String id,
                                           @RequestParam(value = "level", required = false, defaultValue = "1") int level) {
        return new ResponseEntity<>(getObject(_class, id, 0, level), HttpStatus.OK);
    }

    private DBObject getObject(String _class, String id, int currLevel, int maxLevel) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        if (metaExist.isPresent()) {
            Meta meta = metaExist.get();
            meta.getColumns().put("_id", ID_MAP);
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
            BasicDBObject objToSave =  new BasicDBObject();
            Meta meta = metaExist.get();
            meta.getColumns().put("_id", ID_MAP);
            for (String key : meta.getColumns().keySet()) {
                Map columnMap = meta.getColumns().get(key);
                switch ((String) columnMap.get("type")) {
                    case "numeric":
                        objToSave.put(key, Long.parseLong(objParam.get(key) + ""));
                        break;
                    case "ref":
                        if (objParam.get(key) instanceof String) {
                            objToSave.put(key, objParam.get(key));
                        } else if (objParam.get(key) instanceof Map) {
                            Map value = (Map) objParam.get(key);
                            if (value.containsKey("_id")) objToSave.put(key, value.get("_id"));
                            else if (value.containsKey("id")) objToSave.put(key, value.get("id"));
                        }
                        break;
                    case "string":
                        objToSave.put(key, objParam.get(key));
                        break;
                    case "stringlist":
                        if (objParam.get(key) instanceof ArrayList) {
                            objToSave.put(key, objParam.get(key));
                        } else if (objParam.get(key) instanceof String) {
                            objToSave.put(key, ((String) objParam.get(key)).split(","));
                        }
                        break;
                    case "date":
                        if (objParam.get(key) instanceof String) {
                            try {
                                Date date = sdfDateDataType.parse((String) objParam.get(key));
                                objToSave.put(key, date.getTime());
                            } catch (ParseException e) {
                                try {
                                    objToSave.put(key, Long.parseLong((String) objParam.get(key)));
                                } catch (NumberFormatException e2) {
                                    // none will be saved if sdf parse failed and number string parse failed
                                }
                            }
                        }
                        break;
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

    @PutMapping("/{_class}")
    public ResponseEntity<Object> update(@PathVariable String _class, @RequestBody BasicDBObject objParam) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        if (metaExist.isPresent()) {
            BasicDBObject objToSave = (BasicDBObject) genericService.findById(_class, (String) objParam.get("_id"));
            Meta meta = metaExist.get();
            for (String key : meta.getColumns().keySet()) {
                if (objParam.containsField(key)) {
                    Map columnMap = meta.getColumns().get(key);
                    switch ((String) columnMap.get("type")) {
                        case "numeric":
                            objToSave.put(key, Long.parseLong(objParam.get(key) + ""));
                            break;
                        case "ref":
                            if (objParam.get(key) instanceof String) {
                                objToSave.put(key, objParam.get(key));
                            } else if (objParam.get(key) instanceof Map) {
                                Map value = (Map) objParam.get(key);
                                if (value.containsKey("_id")) objToSave.put(key, value.get("_id"));
                                else if (value.containsKey("id")) objToSave.put(key, value.get("id"));
                            }
                            break;
                        case "string":
                            objToSave.put(key, objParam.get(key));
                            break;
                        case "stringlist":
                            if (objParam.get(key) instanceof ArrayList) {
                                objToSave.put(key, objParam.get(key));
                            } else if (objParam.get(key) instanceof String) {
                                objToSave.put(key, ((String) objParam.get(key)).split(","));
                            }
                            break;
                        case "date":
                            if (objParam.get(key) instanceof String) {
                                try {
                                    Date date = sdfDateDataType.parse((String) objParam.get(key));
                                    objToSave.put(key, date.getTime());
                                } catch (ParseException e) {
                                    try {
                                        objToSave.put(key, Long.parseLong((String) objParam.get(key)));
                                    } catch (NumberFormatException e2) {
                                        // none will be saved if sdf parse failed and number string parse failed
                                    }
                                }
                            }
                            break;
                        default:
                            objToSave.put(key, objParam.get(key));
                            break;
                    }
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
                meta.getColumns().put("_id", ID_MAP);
                for (String key : meta.getColumns().keySet()) {
                    Map columnMap = meta.getColumns().get(key);
                    switch ((String) columnMap.get("type")) {
                        case "numeric":
                            objToSave.put(key, Long.parseLong(objParam.get(key) + ""));
                            break;
                        case "ref":
                            if (objParam.get(key) instanceof String) {
                                objToSave.put(key, objParam.get(key));
                            } else if (objParam.get(key) instanceof Map) {
                                Map value = (Map) objParam.get(key);
                                if (value.containsKey("_id")) objToSave.put(key, value.get("_id"));
                                else if (value.containsKey("id")) objToSave.put(key, value.get("id"));
                            }
                            break;
                        case "string":
                            objToSave.put(key, objParam.get(key));
                            break;
                        case "stringlist":
                            if (objParam.get(key) instanceof ArrayList) {
                                objToSave.put(key, objParam.get(key));
                            } else if (objParam.get(key) instanceof String) {
                                objToSave.put(key, ((String) objParam.get(key)).split(","));
                            }
                            break;
                        case "date":
                            if (objParam.get(key) instanceof String) {
                                try {
                                    Date date = sdfDateDataType.parse((String) objParam.get(key));
                                    objToSave.put(key, date.getTime());
                                } catch (ParseException e) {
                                    try {
                                        objToSave.put(key, Long.parseLong((String) objParam.get(key)));
                                    } catch (NumberFormatException e2) {
                                        // none will be saved if sdf parse failed and number string parse failed
                                    }
                                }
                            }
                            break;
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
