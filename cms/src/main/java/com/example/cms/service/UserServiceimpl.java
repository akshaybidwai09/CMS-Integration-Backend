package com.example.cms.service;

import com.example.cms.DAO.*;
import com.example.cms.ResponseHandler.BaseResponse;
import com.example.cms.UserApplication.Comment;
import com.example.cms.UserApplication.RegistrationDTO;
import com.example.cms.UserApplication.UserFeedDTO;
import com.mongodb.client.result.UpdateResult;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceimpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

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
    public User register(String firstName, String lastName, Date dob, String email, String password, boolean isAdmin) throws Exception {
        validateEmail(email, isAdmin);

        if (isAdmin) {
            return registerAdmin(firstName, lastName, dob, email, password);
        } else {
            return registerUser(firstName, lastName, dob, email, password);
        }
    }

    private void validateEmail(String email, boolean isAdmin) {
        if (isAdmin) {
            if (adminRepository.existsByEmail(email)) {
                throw new RuntimeException("Email already in use.");
            }
        } else if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use.");
        }
    }

    private User registerUser(String firstName, String lastName, Date dob, String email, String password) {
        User newUser = createUser(firstName, lastName, dob, email, password);
        return userRepository.save(newUser);
    }

    private Admin registerAdmin(String firstName, String lastName, Date dob, String email, String password) {
        Admin newAdmin = createAdmin(firstName, lastName, dob, email, password);
        return adminRepository.save(newAdmin);
    }

    private User createUser(String firstName, String lastName, Date dob, String email, String password) {
        User user = new User();
        setUserDetails(user, firstName, lastName, dob, email, password);
        return user;
    }

    private Admin createAdmin(String firstName, String lastName, Date dob, String email, String password) {
        Admin admin = new Admin();
        setUserDetails(admin, firstName, lastName, dob, email, password);
        admin.setAdmin(true);
        return admin;
    }

    private void setUserDetails(User user, String firstName, String lastName, Date dob, String email, String password) {
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setDob(dob);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(true);
    }
    public void addActivityToUserFeed(String email, UserActivity userActivity) {
        UserFeed userFeed = userFeedRepository.findByEmail(email);
        User user = userRepository.findByEmail(email);
        if (userFeed != null && userFeed.getUserFeed() !=null) {
            userFeed.getUserFeed().add(userActivity);
            userFeed.setActive(user.isActive());
        } else {
            userFeed = new UserFeed();
            userFeed.setActive(true);
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
                    String userEmail = userFeed.getEmail();
                    List<UserActivity> matchingActivities = userFeed.getUserFeed().stream()
                            .filter(activity -> activity.getCategory().equals(filterText))
                            .map(activity -> {
                                activity.setUserName(userNameFromFeed);
                                activity.setEmail(userEmail);
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
                                    activity.setEmail(userFeed.getEmail());
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
                    String email = userFeed.getEmail();
                    List<UserActivity> activities = userFeed.getUserFeed().stream()
                            .map(activity -> {
                                activity.setUserName(userNameFromFeed);
                                activity.setEmail(email);
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

    @Override
    public ResponseEntity<?> registration(RegistrationDTO registrationDTO,boolean isAdmin) {
        baseResponse = new BaseResponse();

        try {
            if(!validteEmail(registrationDTO.getEmail())){
                baseResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                baseResponse.setError("Current email is not valid, Make sure to use organization email!");
                return new ResponseEntity<>(baseResponse,HttpStatus.BAD_REQUEST);
            }
            User user = register(
                    registrationDTO.getFirstName(),
                    registrationDTO.getLastName(),
                    registrationDTO.getDob(),
                    registrationDTO.getEmail(),
                    registrationDTO.getPassword(),
                    isAdmin
            );
            baseResponse.setStatusCode(HttpStatus.OK.value());
            baseResponse.setStatusMessage("User registered successfully.");
            baseResponse.setResponse(user);
            return new ResponseEntity<>(baseResponse, HttpStatus.OK);
        }catch (Exception e) {
            baseResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
            baseResponse.setStatusMessage("");
            baseResponse.setError("User Already Exist Please Login!");
            return new ResponseEntity<>(baseResponse,HttpStatus.BAD_REQUEST);
        }
    }

    public enum ValidEmailDomain {
        ECHOSPHERE_COM;

        @Override
        public String toString() {
            return name().replace("_", ".").toLowerCase();
        }
    }

    private boolean validteEmail(String email) throws Exception {
        String VALID_DOMAIN = ValidEmailDomain.ECHOSPHERE_COM.toString();
        if (email == null || !email.contains("@")) {
            return false;
        }

        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        if (domain.equals(VALID_DOMAIN)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ResponseEntity<?> getUserAdmins(boolean admin) {
        BaseResponse baseResponse = new BaseResponse();
        try {
            List<User> users = (List<User>) getAllMembers(admin);
            baseResponse.setResponse(users);
            baseResponse.setStatusCode(200);
            baseResponse.setStatusMessage("Success");
            return new ResponseEntity<>(baseResponse, HttpStatus.OK);
        }catch (Exception e){
            baseResponse.setStatusCode(HttpStatus.NO_CONTENT.value());
            baseResponse.setStatusCode(204);
            baseResponse.setError("No Users");
            return new ResponseEntity<>(baseResponse,HttpStatus.NO_CONTENT);
        }
    }

    @Override
    public ResponseEntity<?> deactivateUser(String email, boolean isAdmin) {
        BaseResponse baseResponse = new BaseResponse();
        Query query = new Query(Criteria.where("email").is(email));
        Update update = new Update().set("isActive", false);
        try {
            UpdateResult updateResult = null;
            if (isAdmin) {
                updateResult = mongoTemplate.upsert(query, update, Admin.class);
            } else {
                updateResult = mongoTemplate.upsert(query, update, User.class);
                mongoTemplate.upsert(query, update, UserFeed.class);
            }
            if (updateResult.wasAcknowledged() && updateResult.getModifiedCount()>0) {
                baseResponse.setStatusCode(200);
                baseResponse.setResponse("Success");
                return new ResponseEntity<>(baseResponse, HttpStatus.OK);
            }
        }catch (Exception e){

        }
        baseResponse.setStatusCode(417);
        baseResponse.setResponse("Failure to Deactivate. Either Member Does not Exist or Something Went Wrong ");
        return new ResponseEntity<>(baseResponse,HttpStatus.EXPECTATION_FAILED);
    }

    @Override
    public ResponseEntity<?> addComments(UserFeedDTO userFeedDTO) {
        BaseResponse baseResponse = new BaseResponse();
        if(!CollectionUtils.isEmpty(userFeedDTO.getComment())) {
            userFeedDTO.getComment().forEach((key, value) -> {
                String userCommentEmail = key;
                User user = userRepository.findByEmail(userCommentEmail);
                Query query = new Query(Criteria.where("email").is(userFeedDTO.getEmail())
                        .and("userFeed.id").is(userFeedDTO.getPostId()));

                Comment comment = new Comment();
                comment.setUser(user);
                comment.setComment(value);
                comment.setCommentDate(new Date());
                Update update = new Update().push("userFeed.$.feedbacks", comment);
                UpdateResult updateResult = mongoTemplate.updateFirst(query, update, UserFeed.class);
                if (updateResult.wasAcknowledged() && updateResult.getModifiedCount() > 0) {
                    baseResponse.setStatusCode(200);
                    baseResponse.setResponse(comment);
                }
            });
        }
        return new ResponseEntity<>(baseResponse,HttpStatus.OK);
    }

//    @Override
//    public ResponseEntity<?> addLikes(UserFeedDTO userFeedDTO) {
//        UserActivity activity = userActivityRepository.findById(userFeedDTO.getPostId())
//                .orElseThrow(() -> new ResourceNotFoundException("UserActivity not found for this id :: " + userFeedDTO.getPostId()));
//
//
//        for (String userEmail : userFeedDTO.getLike()) {
//            User user = /* Retrieve User object based on userEmail */;
//            activity.getLikes().add(user);
//        }
//
//        userActivityRepository.save(activity);
//
//        return ResponseEntity.ok().body("Likes added successfully");
//    }

    @NotNull
    private List<?> getAllMembers(boolean admin) {
        Query query = new Query();
        if (!admin)
            return mongoTemplate.find(query,User.class);
        return mongoTemplate.find(query,Admin.class);

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
