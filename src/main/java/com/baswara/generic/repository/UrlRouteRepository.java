package com.baswara.generic.repository;

import com.baswara.generic.domain.UrlRoute;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UrlRouteRepository extends MongoRepository<UrlRoute, String> {
}
