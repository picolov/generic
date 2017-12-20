package com.baswara.generic.repository;

import com.baswara.generic.domain.Meta;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MetaRepository extends MongoRepository<Meta, String> {
    Optional<Meta> findOneByName(String name);
}
