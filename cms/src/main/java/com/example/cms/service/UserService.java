package com.example.cms.service;

import com.example.cms.DAO.UserActivity;
import com.example.cms.DAO.UserFeed;
import com.example.cms.DAO.User;
import com.example.cms.UserApplication.RegistrationDTO;
import com.example.cms.UserApplication.UserFeedDTO;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

public interface UserService {

    User register(String firstName, String lastName, Date dob, String email, String password,boolean isAdmin) throws Exception;

    void addActivityToUserFeed(String email, UserActivity userActivity);

    void upsertUserFeed(UserFeed userFeed);

    User findByEmail(String email);

    List<UserActivity> getUserActivityDetails(UserFeedDTO userFeedDTO);

    List<UserFeed> getUserActivityByCategory(UserFeedDTO userFeedDTO);

    ResponseEntity<?> getUsersByType(List<UserFeed> userFeeds, String type, String filterText);

    ResponseEntity<?> registration(RegistrationDTO registrationDTO, boolean isBoolen);

    ResponseEntity<?> getUserAdmins(boolean admin);

    ResponseEntity<?> deactivateUser(String email, boolean admin);

    ResponseEntity<?> addComments(UserFeedDTO userFeedDTO);

}
