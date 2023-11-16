package com.example.cms.DAO;

import com.example.cms.UserApplication.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserFeedRepository extends MongoRepository<UserFeed, String> {
    UserFeed findByEmail(String email);
}

