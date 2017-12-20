package com.baswara.generic.repository;

import com.baswara.generic.domain.Layout;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LayoutRepository extends MongoRepository<Layout, String> {
    Optional<Layout> findOneByName(String name);
}
