package com.baswara.generic.repository;

import com.baswara.generic.domain.UploadFiles;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UploadFilesRepository extends MongoRepository<UploadFiles, String> {
}
