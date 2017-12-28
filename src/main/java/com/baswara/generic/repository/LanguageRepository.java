package com.baswara.generic.repository;

import com.baswara.generic.domain.Language;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LanguageRepository extends MongoRepository<Language, String> {
    Optional<Language> findOneByName(String name);
}
