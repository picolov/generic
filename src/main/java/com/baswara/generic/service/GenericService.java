package com.baswara.generic.service;

import com.baswara.generic.domain.Meta;
import com.baswara.generic.repository.MetaRepository;
import com.baswara.generic.web.rest.errors.MetaClassNotFoundException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class GenericService {

    private final Logger log = LoggerFactory.getLogger(GenericService.class);

    private final MetaRepository metaRepository;
    private final SimpleDateFormat sdfDateDataType = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private Map<String, Object> ID_MAP = new HashMap<>();

    private final MongoTemplate mongoTemplate;

    public GenericService(MongoTemplate mongoTemplate, MetaRepository metaRepository) {
        this.mongoTemplate = mongoTemplate;
        this.metaRepository = metaRepository;
        sdfDateDataType.setTimeZone(TimeZone.getTimeZone("UTC"));
        ID_MAP.put("name", "_id");
        ID_MAP.put("type", "string");
    }

    public long count(String _class, String criteria, Object... param) {
        Meta meta = null;
        if (param.length > 0) {
            if (!(param[0] instanceof Meta)) {
                throw new IllegalArgumentException("wrong param");
            }
            meta = (Meta)param[0];
        }
        long count;
        if (meta == null) {
            Optional<Meta> metaExist = metaRepository.findOneByName(_class);
            if (metaExist.isPresent()) {
                meta = metaExist.get();
                meta.getColumns().put("_id", ID_MAP);
            }
        }
        if (meta != null) {
            Query query = new Query();
            if (criteria != null) {
                String[] criteriaTokenArr = criteria.split(",");
                for (String criteriaToken:criteriaTokenArr) {
                    String[] token = criteriaToken.split(";");
                    String attr = token[0];
                    String[] attrToken = attr.split("\\.");
                    attrToken = attrToken[0].split("_");
                    if (!meta.getColumns().containsKey(attrToken[0])) continue;
                    Map<String, Object> attrMap = meta.getColumns().get(attrToken[0]);
                    String attrType = (String) attrMap.get("type");
                    if (attrType.equals("link")) {
                        // get all id in relation link table which have the partner is as requested
                        Query linkQuery = new Query();
                        List<String> linkIdList = Arrays.asList(token[1].split(","));
                        linkQuery.addCriteria(Criteria.where((String) attrMap.get("name")).in(linkIdList));
                        List<DBObject> idList = mongoTemplate.find(linkQuery, DBObject.class, (String) attrMap.get("relModel"));
                        query.addCriteria(Criteria.where(attr).in(idList));
                    } else {
                        switch (token[1]) {
                            case "filter":
                                switch (attrType) {
                                    case "string":
                                    case "ref":
                                        query.addCriteria(Criteria.where(attr).regex(".*" + token[2] + ".*"));
                                        break;
                                    case "numeric":
                                        query.addCriteria(Criteria.where(attr).is(Integer.parseInt(token[2])));
                                        break;
                                    case "boolean":
                                        query.addCriteria(Criteria.where(attr).is(Boolean.parseBoolean(token[2])));
                                        break;
                                }
                                break;
                            case "like":
                                query.addCriteria(Criteria.where(attr).regex(".*" + token[2] + ".*"));
                                break;
                            case "is":
                                switch (attrType) {
                                    case "string":
                                    case "ref":
                                        query.addCriteria(Criteria.where(attr).is(token[2]));
                                        break;
                                    case "numeric":
                                        query.addCriteria(Criteria.where(attr).is(Integer.parseInt(token[2])));
                                        break;
                                    case "boolean":
                                        query.addCriteria(Criteria.where(attr).is(Boolean.parseBoolean(token[2])));
                                        break;
                                }
                                break;
                            case "isNot":
                                switch (attrType) {
                                    case "string":
                                    case "ref":
                                        query.addCriteria(Criteria.where(attr).ne(token[2]));
                                        break;
                                    case "numeric":
                                        query.addCriteria(Criteria.where(attr).ne(Integer.parseInt(token[2])));
                                        break;
                                    case "boolean":
                                        query.addCriteria(Criteria.where(attr).ne(Boolean.parseBoolean(token[2])));
                                        break;
                                }
                                break;
                            case "gt":
                                switch (attrType) {
                                    case "numeric":
                                        query.addCriteria(Criteria.where(attr).gt(Integer.parseInt(token[2])));
                                        break;
                                }
                                break;
                            case "gte":
                                switch (attrType) {
                                    case "numeric":
                                        query.addCriteria(Criteria.where(attr).gte(Integer.parseInt(token[2])));
                                        break;
                                }
                                break;
                            case "lt":
                                switch (attrType) {
                                    case "numeric":
                                        query.addCriteria(Criteria.where(attr).lt(Integer.parseInt(token[2])));
                                        break;
                                }
                                break;
                            case "lte":
                                switch (attrType) {
                                    case "numeric":
                                        query.addCriteria(Criteria.where(attr).lte(Integer.parseInt(token[2])));
                                        break;
                                }
                                break;
                            case "btw":
                                switch (attrType) {
                                    case "numeric":
                                        query.addCriteria(Criteria.where(attr).gte(Integer.parseInt(token[2])).lte(Integer.parseInt(token[3])));
                                        break;
                                }
                                break;
                            case "btwx":
                                switch (attrType) {
                                    case "numeric":
                                        query.addCriteria(Criteria.where(attr).gt(Integer.parseInt(token[2])).lt(Integer.parseInt(token[3])));
                                        break;
                                }
                                break;
                        }
                    }
                }
            }
            count = mongoTemplate.count(query, _class);
        } else {
            throw new MetaClassNotFoundException();
        }

        return count;
    }

    public boolean existsById(String _class, String id) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        if (metaExist.isPresent()) {
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(id));
            return mongoTemplate.exists(query, _class);
        } else {
            throw new MetaClassNotFoundException();
        }
    }

    public void deleteAll(String _class) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        if (metaExist.isPresent()) {
            mongoTemplate.dropCollection( _class);
        } else {
            throw new MetaClassNotFoundException();
        }
    }

    public void deleteById(String _class, String id) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        if (metaExist.isPresent()) {
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(id));
            mongoTemplate.remove(query, _class);
        } else {
            throw new MetaClassNotFoundException();
        }
    }

    public List<DBObject> findAll(String _class) {
        return mongoTemplate.findAll(DBObject.class, _class);
    }

    public DBObject findOne(String _class, String criteria, int level) {
        List<DBObject> resultList = findAllPaging(_class, criteria, level, 0, 1, null);
        if (resultList != null && resultList.size() > 0) {
            return resultList.get(0);
        } else {
            return null;
        }
    }

    public List<DBObject> findAllPaging(String _class, String criteria, int level, int page, int size, String sort) {
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
            Query query = new Query();
            // name;like;pi,age;is;6
            if (criteria != null) {
                String[] criteriaTokenArr = criteria.split(",");
                for (String criteriaToken : criteriaTokenArr) {
                    String[] token = criteriaToken.split(";");
                    String attr = token[0];
                    String[] attrToken = attr.split("\\.");
                    attrToken = attrToken[0].split("_");
                    if (!meta.getColumns().containsKey(attrToken[0])) continue;
                    Map<String, Object> attrMap = meta.getColumns().get(attrToken[0]);
                    String attrType = (String) attrMap.get("type");
                    if (attrType.equals("link")) {
                        // get all id in relation link table which have the partner is as requested
                        Query linkQuery = new Query();
                        List<String> linkIdList = Arrays.asList(token[1].split(","));
                        linkQuery.addCriteria(Criteria.where((String) attrMap.get("name")).in(linkIdList));
                        List<DBObject> relList = mongoTemplate.find(linkQuery, DBObject.class, (String) attrMap.get("relModel"));
                        List<String> idList = new ArrayList<>();
                        for (DBObject rel : relList) {
                            idList.add((String) rel.get(meta.getName()));
                        }
                        query.addCriteria(Criteria.where("_id").in(idList));
                    } else {
                        switch (token[1]) {
                            case "filter":
                                switch (attrType) {
                                    case "string":
                                    case "ref":
                                        query.addCriteria(Criteria.where(attr).regex(".*" + token[2] + ".*"));
                                        break;
                                    case "numeric":
                                        query.addCriteria(Criteria.where(attr).is(Integer.parseInt(token[2])));
                                        break;
                                    case "boolean":
                                        query.addCriteria(Criteria.where(attr).is(Boolean.parseBoolean(token[2])));
                                        break;
                                }
                                break;
                            case "like":
                                query.addCriteria(Criteria.where(attr).regex(".*" + token[2] + ".*"));
                                break;
                            case "is":
                                switch (attrType) {
                                    case "string":
                                    case "ref":
                                        query.addCriteria(Criteria.where(attr).is(token[2]));
                                        break;
                                    case "numeric":
                                        query.addCriteria(Criteria.where(attr).is(Integer.parseInt(token[2])));
                                        break;
                                    case "boolean":
                                        query.addCriteria(Criteria.where(attr).is(Boolean.parseBoolean(token[2])));
                                        break;
                                }
                                break;
                            case "isNot":
                                switch (attrType) {
                                    case "string":
                                    case "ref":
                                        query.addCriteria(Criteria.where(attr).ne(token[2]));
                                        break;
                                    case "numeric":
                                        query.addCriteria(Criteria.where(attr).ne(Integer.parseInt(token[2])));
                                        break;
                                    case "boolean":
                                        query.addCriteria(Criteria.where(attr).ne(Boolean.parseBoolean(token[2])));
                                        break;
                                }
                                break;
                            case "gt":
                                switch (attrType) {
                                    case "numeric":
                                        query.addCriteria(Criteria.where(attr).gt(Integer.parseInt(token[2])));
                                        break;
                                }
                                break;
                            case "gte":
                                switch (attrType) {
                                    case "numeric":
                                        query.addCriteria(Criteria.where(attr).gte(Integer.parseInt(token[2])));
                                        break;
                                }
                                break;
                            case "lt":
                                switch (attrType) {
                                    case "numeric":
                                        query.addCriteria(Criteria.where(attr).lt(Integer.parseInt(token[2])));
                                        break;
                                }
                                break;
                            case "lte":
                                switch (attrType) {
                                    case "numeric":
                                        query.addCriteria(Criteria.where(attr).lte(Integer.parseInt(token[2])));
                                        break;
                                }
                                break;
                            case "btw":
                                switch (attrType) {
                                    case "numeric":
                                        query.addCriteria(Criteria.where(attr).gte(Integer.parseInt(token[2])).lte(Integer.parseInt(token[3])));
                                        break;
                                }
                                break;
                            case "btwx":
                                switch (attrType) {
                                    case "numeric":
                                        query.addCriteria(Criteria.where(attr).gt(Integer.parseInt(token[2])).lt(Integer.parseInt(token[3])));
                                        break;
                                }
                                break;
                        }
                    }
                }
            }
            System.out.println(query);
            query.with(pageable);
            List<DBObject> respList = mongoTemplate.find(query, DBObject.class, _class);
            for (DBObject resp:respList) {
                DBObject result = new BasicDBObject();
                result.put("_id", resp.get("_id"));
                for (String key : meta.getColumns().keySet()) {
                    Map columnMap = meta.getColumns().get(key);
                    if (columnMap.containsKey("isArray") && (Boolean) columnMap.get("isArray")) {
                        for (String paramKey : resp.keySet()) {
                            if (paramKey.startsWith(key + "_")) {
                                switch ((String) columnMap.get("type")) {
                                    case "ref":
                                        if (resp.get(paramKey) != null && 1 <= level) {
                                            String classRef = (String) columnMap.get("classRef");
                                            result.put(paramKey, getObject(classRef, (String) resp.get(paramKey), 1, level));
                                        } else {
                                            result.put(paramKey, resp.get(paramKey));
                                        }
                                        break;
                                    case "link":

                                        break;
                                    default:
                                        result.put(paramKey, resp.get(paramKey));
                                }
                            }
                        }
                    } else if (resp.containsField(key)) {
                        switch ((String) columnMap.get("type")) {
                            case "ref":
                                if (resp.get(key) != null && 1 <= level) {
                                    String classRef = (String) columnMap.get("classRef");
                                    result.put(key, getObject(classRef, (String) resp.get(key), 1, level));
                                } else {
                                    result.put(key, resp.get(key));
                                }
                                break;
                            case "link":

                                break;
                            default:
                                result.put(key, resp.get(key));
                        }
                    }
                }
                resultList.add(result);
            }
            return resultList;
        } else {
            throw new MetaClassNotFoundException();
        }
    }

    public DBObject findById(String _class, String id) {
        return mongoTemplate.findById(id, DBObject.class, _class);
    }

    public DBObject saveModel(String _class, BasicDBObject objParam) {
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
                Map columnMap = meta.getColumns().get(key);
                if (columnMap.containsKey("isArray") && (Boolean) columnMap.get("isArray")) {
                    for (String paramKey : objParam.keySet()) {
                        if (paramKey.startsWith(key + "_")) {
                            fillObjectBasedOnColumn(columnMap, paramKey, objParam, objToSave, linkToSaveMap);
                        }
                    }
                } else if (objParam.containsField(key)) {
                    fillObjectBasedOnColumn(columnMap, key, objParam, objToSave, linkToSaveMap);
                }
            }
            DBObject result = save(_class, objToSave);
            for (Map.Entry<String, BasicDBObject> linkEntry : linkToSaveMap.entrySet()) {
                BasicDBObject link = linkEntry.getValue();
                link.put(meta.getName(), result.get("_id"));
                save(linkEntry.getKey(), link);
            }
            return result;
        } else {
            throw new MetaClassNotFoundException();
        }
    }

    private void fillObjectBasedOnColumn(Map columnMap, String key, BasicDBObject objParam, BasicDBObject objToSave, Map<String, BasicDBObject> linkToSaveMap) {
        switch ((String) columnMap.get("type")) {
            case "boolean":
                objToSave.put(key, Boolean.parseBoolean(objParam.get(key) + ""));
                break;
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

    public DBObject updateModel(String _class, BasicDBObject objParam) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        Map<String, BasicDBObject> linkToSaveMap = new HashMap<>();
        if (metaExist.isPresent()) {
            BasicDBObject objToSave = (BasicDBObject) findById(_class, (String) objParam.get("_id"));
            Meta meta = metaExist.get();
            for (String key : meta.getColumns().keySet()) {
                Map columnMap = meta.getColumns().get(key);
                if (columnMap.containsKey("isArray") && (Boolean) columnMap.get("isArray")) {
                    for (String paramKey : objParam.keySet()) {
                        if (paramKey.startsWith(key + "_")) {
                            fillObjectBasedOnColumn(columnMap, paramKey, objParam, objToSave, linkToSaveMap);
                        }
                    }
                } else if (objParam.containsField(key)) {
                    fillObjectBasedOnColumn(columnMap, key, objParam, objToSave, linkToSaveMap);
                }
            }
            DBObject result = save(_class, objToSave);
            for (Map.Entry<String, BasicDBObject> linkEntry : linkToSaveMap.entrySet()) {
                BasicDBObject link = linkEntry.getValue();
                link.put(meta.getName(), result.get("_id"));
                save(linkEntry.getKey(), link);
            }
            return result;
        } else {
            throw new MetaClassNotFoundException();
        }
    }

    public List<DBObject> saveModelBulk(String _class, List<BasicDBObject> objParamList) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        Map<String, BasicDBObject> linkToSaveMap = new HashMap<>();
        if (metaExist.isPresent()) {
            List<DBObject> objList = new ArrayList<>();
            for (BasicDBObject objParam:objParamList) {
                BasicDBObject objToSave = new BasicDBObject();
                Meta meta = metaExist.get();
                meta.getColumns().put("_id", ID_MAP);
                if (!objParam.containsField("_id")) {
                    objParam.put("_id", UUID.randomUUID().toString());
                }
                for (String key : meta.getColumns().keySet()) {
                    Map columnMap = meta.getColumns().get(key);
                    if (columnMap.containsKey("isArray") && (Boolean) columnMap.get("isArray")) {
                        for (String paramKey : objParam.keySet()) {
                            if (paramKey.startsWith(key + "_")) {
                                fillObjectBasedOnColumn(columnMap, paramKey, objParam, objToSave, linkToSaveMap);
                            }
                        }
                    } else if (objParam.containsField(key)) {
                        fillObjectBasedOnColumn(columnMap, key, objParam, objToSave, linkToSaveMap);
                    }
                }
                objList.add(objToSave);
            }
            List<DBObject> result = saveList(_class, objList);
            return result;
        } else {
            throw new MetaClassNotFoundException();
        }
    }

    public DBObject save(String _class, DBObject obj) {
        mongoTemplate.save(obj, _class);
        return obj;
    }

    public List<DBObject> saveList(String _class, List<DBObject> objParamList) {
        mongoTemplate.save(objParamList, _class);
        return objParamList;
    }

    public DBObject getObject(String _class, String id, int currLevel, int maxLevel) {
        Optional<Meta> metaExist = metaRepository.findOneByName(_class);
        if (metaExist.isPresent()) {
            Meta meta = metaExist.get();
            meta.getColumns().put("_id", ID_MAP);
            DBObject result = new BasicDBObject();
            DBObject resp = findById(_class, id);
            for (String key : meta.getColumns().keySet()) {
                Map columnMap = meta.getColumns().get(key);
                if (columnMap.containsKey("isArray") && (Boolean) columnMap.get("isArray")) {
                    for (String paramKey : resp.keySet()) {
                        if (paramKey.startsWith(key + "_")) {
                            switch ((String) columnMap.get("type")) {
                                case "ref":
                                    if (resp.get(paramKey) != null && currLevel+1 <= maxLevel) {
                                        String classRef = (String) columnMap.get("classRef");
                                        result.put(paramKey, getObject(classRef, (String) resp.get(paramKey), currLevel + 1, maxLevel));
                                    } else {
                                        result.put(paramKey, resp.get(paramKey));
                                    }
                                    break;
                                default:
                                    result.put(paramKey, resp.get(paramKey));
                            }
                        }
                    }
                } else if (resp.containsField(key)) {
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
            }
            return result;
        } else {
            throw new MetaClassNotFoundException();
        }
    }

}
