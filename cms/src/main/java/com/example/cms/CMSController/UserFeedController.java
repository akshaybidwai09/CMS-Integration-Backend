package com.example.cms.CMSController;
import com.example.cms.DAO.UserActivity;
import com.example.cms.DAO.UserFeed;
import com.example.cms.ResponseHandler.BaseResponse;
import com.example.cms.UserApplication.UserFeedDTO;
import com.example.cms.service.UserServiceimpl;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/userfeed")
public class UserFeedController {

    @Autowired
    private UserServiceimpl userServiceimpl;

    @Autowired
    public BaseResponse baseResponse;
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFileAndText(
            @RequestParam("email") String email,
            @RequestParam("blogText") String blogText,
            @RequestParam("category") String category,
            @RequestParam(value = "file", required = true) MultipartFile file) throws IOException {
        baseResponse = new BaseResponse();
        try {
            if (file != null && !file.isEmpty()) {

                UserActivity userActivity = new UserActivity();
                userActivity.setBlogText(blogText);
                userActivity.setId(UUID.randomUUID().toString());
                userActivity.setUploadedDate(getCurrentDateUTC());
                userActivity.setFile(new Binary(file.getBytes()));
                if("video/mp4".equals(file.getContentType())){
                    userActivity.setVideo(true);
                }
                userActivity.setCategory(Character.toUpperCase(category.charAt(0)) + category.substring(1).toLowerCase());
                userServiceimpl.addActivityToUserFeed(email, userActivity);

                baseResponse.setStatusCode(HttpStatus.OK.value());
                baseResponse.setStatusMessage("Content Successfully Posted!");
                baseResponse.setResponse("Content Successfully Posted!");
                return new ResponseEntity<>(baseResponse, HttpStatus.OK);
            }
        }catch (Exception e){
            e.getMessage();
        }
        baseResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        baseResponse.setStatusMessage("");
        baseResponse.setError("Something went wrong while uploading Content!");
        return new ResponseEntity<>(baseResponse,HttpStatus.BAD_REQUEST);
    }

    public Date getCurrentDateUTC() {
        LocalDate todayLocal = LocalDate.now(ZoneId.systemDefault());
        ZonedDateTime startOfDayUTC = todayLocal.atStartOfDay(ZoneId.of("UTC"));
        return Date.from(startOfDayUTC.toInstant());
    }

    @PostMapping("/get-user-feed")
    public ResponseEntity<?> getUserFeed(@RequestBody UserFeedDTO userFeedDTO) {
        baseResponse = new BaseResponse();
        List<UserActivity> userActivityList = userServiceimpl.getUserActivityDetails(userFeedDTO);

        if(userActivityList != null){
            baseResponse.setStatusCode(HttpStatus.OK.value());
            baseResponse.setStatusMessage("User Feed");
            baseResponse.setResponse(userActivityList);
            return new ResponseEntity<>(baseResponse, HttpStatus.OK);
        }
        baseResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
        baseResponse.setStatusMessage("No Feed Against User");
        baseResponse.setError("No Feed Against User");
        return new ResponseEntity<>(baseResponse,HttpStatus.NOT_FOUND);
    }


    @PostMapping("/get-users")
    public ResponseEntity<?> getUsers(@RequestBody UserFeedDTO userFeedDTO) {
        List<UserFeed> userFeeds = userServiceimpl.getUserActivityByCategory(userFeedDTO);
        List<UserFeed> activeUserFeeds = userFeeds.stream()
                .filter(UserFeed::isActive)
                .collect(Collectors.toList());
        return userServiceimpl.getUsersByType(activeUserFeeds,userFeedDTO.getType(),userFeedDTO.getFilterText().trim());
    }

    @PostMapping("/add-comment")
    public ResponseEntity<?> addComments(@RequestBody UserFeedDTO userFeedDTO) {
        return userServiceimpl.addComments(userFeedDTO);
    }

//    @PostMapping("/add-like")
//    public ResponseEntity<?> addLikes(@RequestBody UserFeedDTO userFeedDTO) {
//
//        return userServiceimpl.addLikes();
//    }


}
