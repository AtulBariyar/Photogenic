package com.photogenic.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.photogenic.model.userModel;

public interface userRepository extends MongoRepository<userModel,String> {
    userModel findByUsername(String username);
}
