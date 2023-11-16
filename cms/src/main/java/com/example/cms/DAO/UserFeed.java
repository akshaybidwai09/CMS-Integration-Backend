package com.example.cms.DAO;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
public class UserFeed {

    private String email;

    List<UserActivity> userFeed;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<UserActivity> getUserFeed() {
        return userFeed;
    }

    public void setUserFeed(List<UserActivity> userFeed) {
        this.userFeed = userFeed;
    }
}
