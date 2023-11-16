package com.example.cms.service;

import com.example.cms.DAO.UserActivity;
import com.example.cms.DAO.UserFeed;
import com.example.cms.DAO.UserFeedRepository;
import com.example.cms.UserApplication.User;
import com.example.cms.DAO.UserRepository;
import com.example.cms.UserApplication.UserFeedDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFeedRepository userFeedRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MongoTemplate mongoTemplate;

    public User registerUser(String firstName, String lastName, Date dob, String email, String password) throws Exception {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use.");
        }

        User newUser = new User();
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setDob(dob);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        return userRepository.save(newUser);
    }

    public void addActivityToUserFeed(String email, UserActivity userActivity) {
        UserFeed userFeed = userFeedRepository.findByEmail(email);
        if (userFeed != null && userFeed.getUserFeed() !=null) {
            userFeed.getUserFeed().add(userActivity);
        } else {
            userFeed = new UserFeed();
            userFeed.setEmail(email);
            List<UserActivity> activities = new ArrayList<>();
            activities.add(userActivity);
            userFeed.setUserFeed(activities);
        }
        upsertUserFeed(userFeed);
    }

    public void upsertUserFeed(UserFeed userFeed) {
        Query query = new Query(Criteria.where("email").is(userFeed.getEmail()));
        UserFeed existingFeed = mongoTemplate.findOne(query, UserFeed.class);

        if (existingFeed == null) {
            mongoTemplate.insert(userFeed);
        } else {
            for (UserActivity activity : userFeed.getUserFeed()) {
                Update update = new Update().addToSet("userFeed", activity);
                mongoTemplate.updateFirst(query, update, UserFeed.class);
            }
        }
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<UserActivity> getUserActivityDetails(UserFeedDTO userFeedDTO) {
        String email = userFeedDTO.getEmail();
        UserFeed userFeed = userFeedRepository.findByEmail(email);

        List<UserActivity> userActivityList;

        if (userFeed != null) {
            userActivityList = userFeed.getUserFeed();
        } else {
            return null;
        }


        Collections.sort(userActivityList, new Comparator<UserActivity>() {
            @Override
            public int compare(UserActivity u1, UserActivity u2) {
                return u1.getUploadedDate().compareTo(u2.getUploadedDate());
            }
        });
        return userActivityList;
    }
}
