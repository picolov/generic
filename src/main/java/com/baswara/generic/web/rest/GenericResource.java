package com.baswara.generic.web.rest;

import com.baswara.generic.domain.Meta;
import com.baswara.generic.repository.MetaRepository;
import com.baswara.generic.service.GenericService;
import com.baswara.generic.web.rest.errors.MetaClassNotFoundException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
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
    private final MetaRepository metaRepository;
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
    public ResponseEntity<Object> findAll(@PathVariable String _class,
                                          @RequestParam(value = "level", required = false, defaultValue = "1") int level,
                                          @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                          @RequestParam(value = "size", required = false, defaultValue = "100") int size,
                                          @RequestParam(value = "sort", required = false) String sort,
                                          @RequestParam(value = "criteria", required = false) String criteria) {
        List<DBObject> resultList = genericService.findAllPaging(_class, criteria, level, page, size, sort);
        return new ResponseEntity<>(resultList, HttpStatus.OK);
    }

    @GetMapping("/findAllWithTotal/{_class}")
    public ResponseEntity<Object> findAllWithTotal(@PathVariable String _class,
                                          @RequestParam(value = "level", required = false, defaultValue = "1") int level,
                                          @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                          @RequestParam(value = "size", required = false, defaultValue = "100") int size,
                                          @RequestParam(value = "sort", required = false) String sort,
                                          @RequestParam(value = "criteria", required = false) String criteria) {
        long count = genericService.count(_class, criteria);
        List<DBObject> resultList = genericService.findAllPaging(_class, criteria, level, page, size, sort);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalRows", count);
        resultMap.put("list", resultList);
        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    @GetMapping("/{_class}/{id}")
    public ResponseEntity<Object> findById(@PathVariable String _class, @PathVariable String id,
                                           @RequestParam(value = "level", required = false, defaultValue = "1") int level) {
        return new ResponseEntity<>(genericService.getObject(_class, id, 0, level), HttpStatus.OK);
    }



    @PostMapping("/{_class}")
    public ResponseEntity<Object> save(@PathVariable String _class, @RequestBody BasicDBObject objParam) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        Map<String, BasicDBObject> linkToSaveMap = new HashMap<>();
        if (metaExist.isPresent()) {
            BasicDBObject objToSave =  new BasicDBObject();
            Meta meta = metaExist.get();
            meta.getColumns().put("_id", ID_MAP);
            if (!objParam.containsField("_id")) {
                objParam.put("_id", UUID.randomUUID().toString());
            }
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
                        case "link":
                            String relModel = (String) columnMap.get("relModel");
                            if (objParam.get(key) instanceof ArrayList) {
                                List valueList = (ArrayList) objParam.get(key);
                                for (int i = 0; i < valueList.size(); i++) {
                                    BasicDBObject linkToSave = new BasicDBObject();
                                    linkToSave.put(key, valueList.get(i));
                                    linkToSaveMap.put(relModel, linkToSave);
                                }
                            } else if (objParam.get(key) instanceof String) {
                                BasicDBObject linkToSave = new BasicDBObject();
                                linkToSave.put(key, objParam.get(key));
                                linkToSaveMap.put(relModel, linkToSave);
                            }
                            break;
                        default:
                            objToSave.put(key, objParam.get(key));
                            break;
                    }
                }
            }
            DBObject result = genericService.save(_class, objToSave);
            for (Map.Entry<String, BasicDBObject> linkEntry : linkToSaveMap.entrySet()) {
                BasicDBObject link = linkEntry.getValue();
                link.put(meta.getName(), result.get("_id"));
                genericService.save(linkEntry.getKey(), link);
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            throw new MetaClassNotFoundException();
        }
    }

    @PutMapping("/{_class}")
    public ResponseEntity<Object> update(@PathVariable String _class, @RequestBody BasicDBObject objParam) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        Map<String, BasicDBObject> linkToSaveMap = new HashMap<>();
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
                        case "link":
                            String relModel = (String) columnMap.get("relModel");
                            if (objParam.get(key) instanceof ArrayList) {
                                List valueList = (ArrayList) objParam.get(key);
                                for (int i = 0; i < valueList.size(); i++) {
                                    BasicDBObject linkToSave = new BasicDBObject();
                                    linkToSave.put(key, valueList.get(i));
                                    linkToSaveMap.put(relModel, linkToSave);
                                }
                            } else if (objParam.get(key) instanceof String) {
                                BasicDBObject linkToSave =  new BasicDBObject();
                                linkToSave.put(key, objParam.get(key));
                                linkToSaveMap.put(relModel, linkToSave);
                            }
                            break;
                        default:
                            objToSave.put(key, objParam.get(key));
                            break;
                    }
                }
            }
            DBObject result = genericService.save(_class, objToSave);
            for (Map.Entry<String, BasicDBObject> linkEntry : linkToSaveMap.entrySet()) {
                BasicDBObject link = linkEntry.getValue();
                link.put(meta.getName(), result.get("_id"));
                genericService.save(linkEntry.getKey(), link);
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            throw new MetaClassNotFoundException();
        }
    }

    @PostMapping("/bulk/{_class}")
    public ResponseEntity<Object> saveList(@PathVariable String _class, @RequestBody List<BasicDBObject> objParamList) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        Map<String, BasicDBObject> linkToSaveMap = new HashMap<>();
        if (metaExist.isPresent()) {
            List<DBObject> objList = new ArrayList<>();
            for (DBObject objParam:objParamList) {
                DBObject objToSave = new BasicDBObject();
                Meta meta = metaExist.get();
                meta.getColumns().put("_id", ID_MAP);
                if (!objParam.containsField("_id")) {
                    objParam.put("_id", UUID.randomUUID().toString());
                }
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
                            case "link":
                                String relModel = (String) columnMap.get("relModel");
                                if (objParam.get(key) instanceof ArrayList) {
                                    List valueList = (ArrayList) objParam.get(key);
                                    for (int i = 0; i < valueList.size(); i++) {
                                        BasicDBObject linkToSave = new BasicDBObject();
                                        linkToSave.put(key, valueList.get(i));
                                        linkToSaveMap.put(relModel, linkToSave);
                                    }
                                } else if (objParam.get(key) instanceof String) {
                                    BasicDBObject linkToSave =  new BasicDBObject();
                                    linkToSave.put(key, objParam.get(key));
                                    linkToSaveMap.put(relModel, linkToSave);
                                }
                                break;
                            default:
                                objToSave.put(key, objParam.get(key));
                                break;
                        }
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
