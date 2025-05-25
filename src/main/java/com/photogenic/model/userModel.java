package com.photogenic.model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pgUsers")
@Data
public class userModel {
    @Id
    private String id;
    private String email;
    private String username;
    private String password;
}


