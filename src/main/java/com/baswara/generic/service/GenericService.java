package com.baswara.generic.service;

import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class GenericService {

    private final Logger log = LoggerFactory.getLogger(GenericService.class);

    private final MongoTemplate mongoTemplate;

    public GenericService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public long count(String _class) {
        long count = mongoTemplate.count(new Query(), _class);
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

    public DBObject findById(String _class, String id) {
        return mongoTemplate.findById(id, DBObject.class, _class);
    }

    public DBObject save(String _class, DBObject obj) {
        mongoTemplate.insert(obj, _class);
        return obj;
    }

    public List<DBObject> saveList(String _class, List<DBObject> objParamList) {
        mongoTemplate.insert(objParamList, _class);
        return objParamList;
    }

}
