package com.baswara.generic.repository;

import com.baswara.generic.domain.Flow;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FlowRepository extends MongoRepository<Flow, String> {
    Optional<Flow> findOneById(String name);
    Optional<Flow> findOneByPath(String path);
    Optional<Flow> findOneByPathStartsWith(String path);
}
