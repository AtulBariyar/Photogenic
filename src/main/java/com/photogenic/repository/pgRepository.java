package com.photogenic.repository;

import com.photogenic.model.pgModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface pgRepository extends MongoRepository<pgModel,String> {
}
