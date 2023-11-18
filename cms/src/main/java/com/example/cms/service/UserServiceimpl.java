package com.example.cms.service;

import com.example.cms.DAO.UserActivity;
import com.example.cms.DAO.UserFeed;
import com.example.cms.DAO.UserFeedRepository;
import com.example.cms.ResponseHandler.BaseResponse;
import com.example.cms.UserApplication.User;
import com.example.cms.DAO.UserRepository;
import com.example.cms.UserApplication.UserFeedDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceimpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFeedRepository userFeedRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private BaseResponse baseResponse;

    public UserServiceimpl() {
        baseResponse = new BaseResponse();
    }

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
        User user = userRepository.findByEmail(email);
        if (userFeed != null && userFeed.getUserFeed() !=null) {
            userFeed.getUserFeed().add(userActivity);
        } else {
            userFeed = new UserFeed();
            userFeed.setEmail(email);
            userFeed.setName(user.getFirstName());
            userFeed.setLastName(user.getLastName());
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

    public List<UserFeed> getUserActivityByCategory(UserFeedDTO userFeedDTO) {
        String type = userFeedDTO.getType();
        String filterText = userFeedDTO.getFilterText();
        Query query = new Query();

        if ("category".equalsIgnoreCase(type)) {
            query.addCriteria(Criteria.where("userFeed").elemMatch(Criteria.where("category").is(filterText)));
        }else if ("firstName".equalsIgnoreCase(type)){
            query.addCriteria(Criteria.where("name").is(filterText));
        } else if ("lastName".equalsIgnoreCase(type)) {
            query.addCriteria(Criteria.where("lastName").is(filterText));
        }

        return  mongoTemplate.find(query, UserFeed.class);
    }

    @Override
    public ResponseEntity<?> getUsersByType(List<UserFeed> userFeeds, String type, String filterText) {
        BaseResponse baseResponse = new BaseResponse();

        List<UserActivity> filteredActivities = new ArrayList<>();
        List<UserFeed> namedUsers = new ArrayList<>();

        switch (type.toLowerCase()) {
            case "category":
                for (UserFeed userFeed : userFeeds) {
                    String userNameFromFeed = userFeed.getName();
                    List<UserActivity> matchingActivities = userFeed.getUserFeed().stream()
                            .filter(activity -> activity.getCategory().equals(filterText))
                            .map(activity -> {
                                activity.setUserName(userNameFromFeed);
                                return activity;
                            })
                            .collect(Collectors.toList());
                    filteredActivities.addAll(matchingActivities);
                    baseResponse.setResponse(filteredActivities);
                }
                break;
            case "date":
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date filterDate = null;
                try {
                    if (filterText != null && !filterText.isEmpty()) {
                        filterDate = sdf.parse(filterText);
                    }
                } catch (ParseException e) {
                    // Handle exception
                }

                if (filterDate != null) {
                    for (UserFeed userFeed : userFeeds) {
                        Date finalFilterDate = filterDate;
                        String userNameFromFeed = userFeed.getName();
                        List<UserActivity> matchingActivities = userFeed.getUserFeed().stream()
                                .filter(activity -> {
                                    if (activity.getUploadedDate() == null) {
                                        return false;
                                    }
                                    return sdf.format(activity.getUploadedDate()).equals(sdf.format(finalFilterDate));
                                })
                                .map(activity -> {
                                    activity.setUserName(userNameFromFeed);
                                    return activity;
                                })
                                .collect(Collectors.toList());

                        filteredActivities.addAll(matchingActivities);
                    }
                    baseResponse.setResponse(filteredActivities);
                }
                break;
            case "name":
                userFeeds.stream()
                        .filter(feed -> feed.getName() != null && feed.getName().equalsIgnoreCase(filterText))
                        .findFirst()
                        .ifPresent(feed -> namedUsers.add(feed));
                baseResponse.setResponse(namedUsers);
                break;
            case "all":
                for (UserFeed userFeed : userFeeds) {
                    String userNameFromFeed = userFeed.getName();
                    List<UserActivity> activities = userFeed.getUserFeed().stream()
                            .map(activity -> {
                                activity.setUserName(userNameFromFeed);
                                return activity;
                            })
                            .collect(Collectors.toList());
                    filteredActivities.addAll(activities);
                }
                filteredActivities.sort((a1, a2) -> a2.getUploadedDate().compareTo(a1.getUploadedDate()));

                baseResponse.setResponse(filteredActivities);
                break;
        }

        if (baseResponse.getResponse() != null) {
            baseResponse.setStatusCode(HttpStatus.OK.value());
            baseResponse.setStatusMessage("User Activities");
            return new ResponseEntity<>(baseResponse, HttpStatus.OK);
        } else {
            baseResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            baseResponse.setStatusMessage("No Data Found");
            baseResponse.setError("No Data Found");
            return new ResponseEntity<>(baseResponse, HttpStatus.NOT_FOUND);
        }
    }

    private Date convertToDate(String dateString) {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return isoFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
