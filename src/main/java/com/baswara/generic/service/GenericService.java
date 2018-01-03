package com.baswara.generic.service;

import com.baswara.generic.domain.Meta;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class GenericService {

    private final Logger log = LoggerFactory.getLogger(GenericService.class);

    private final MongoTemplate mongoTemplate;

    public GenericService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public long count(String _class, String criteria, Meta meta) {
        Query query = new Query();
        if (criteria != null) {
            String[] criteriaTokenArr = criteria.split(",");
            for (String criteriaToken:criteriaTokenArr) {
                String[] token = criteriaToken.split(";");
                String attr = token[0];
                if (!meta.getColumns().containsKey(attr)) continue;
                Map<String, Object> attrMap = meta.getColumns().get(attr);
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
                                    query.addCriteria(Criteria.where(attr).regex(".*" + token[2] + ".*"));
                                    break;
                                case "numeric":
                                    query.addCriteria(Criteria.where(attr).is(Integer.parseInt(token[2])));
                                    break;
                            }
                            break;
                        case "like":
                            query.addCriteria(Criteria.where(attr).regex(".*" + token[2] + ".*"));
                            break;
                        case "is":
                            switch (attrType) {
                                case "string":
                                    query.addCriteria(Criteria.where(attr).is(token[2]));
                                    break;
                                case "numeric":
                                    query.addCriteria(Criteria.where(attr).is(Integer.parseInt(token[2])));
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
        long count = mongoTemplate.count(query, _class);
        return count;
    }

    public boolean existsById(String _class, String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        return mongoTemplate.exists(query, _class);
    }

    public void deleteAll(String _class) {
        mongoTemplate.dropCollection( _class);
    }

    public void deleteById(String _class, String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, _class);
    }

    public List<DBObject> findAll(String _class) {
        return mongoTemplate.findAll(DBObject.class, _class);
    }

    public List<DBObject> findAllPaging(String _class, String criteria, Pageable pageable, Meta meta) {
        Query query = new Query();
        // name;like;pi,age;is;6
        if (criteria != null) {
            String[] criteriaTokenArr = criteria.split(",");
            for (String criteriaToken:criteriaTokenArr) {
                String[] token = criteriaToken.split(";");
                String attr = token[0];
                if (!meta.getColumns().containsKey(attr)) continue;
                Map<String, Object> attrMap = meta.getColumns().get(attr);
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
                                    query.addCriteria(Criteria.where(attr).regex(".*" + token[2] + ".*"));
                                    break;
                                case "numeric":
                                    query.addCriteria(Criteria.where(attr).is(Integer.parseInt(token[2])));
                                    break;
                            }
                            break;
                        case "like":
                            query.addCriteria(Criteria.where(attr).regex(".*" + token[2] + ".*"));
                            break;
                        case "is":
                            switch (attrType) {
                                case "string":
                                    query.addCriteria(Criteria.where(attr).is(token[2]));
                                    break;
                                case "numeric":
                                    query.addCriteria(Criteria.where(attr).is(Integer.parseInt(token[2])));
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
        query.with(pageable);
        return mongoTemplate.find(query, DBObject.class, _class);
    }

    public DBObject findById(String _class, String id) {
        return mongoTemplate.findById(id, DBObject.class, _class);
    }

    public DBObject save(String _class, DBObject obj) {
        mongoTemplate.save(obj, _class);
        return obj;
    }

    public List<DBObject> saveList(String _class, List<DBObject> objParamList) {
        mongoTemplate.save(objParamList, _class);
        return objParamList;
    }

}
