package com.example.cms.DAO;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AdminRepository extends MongoRepository<Admin, String> {
    Admin findByEmail(String email);
    boolean existsByEmail(String email);
}
