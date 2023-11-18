package com.example.cms.DAO;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
public class UserFeed {

    private String name;

    private String lastName;

    private String email;

    private List<UserActivity> userFeed;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

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
