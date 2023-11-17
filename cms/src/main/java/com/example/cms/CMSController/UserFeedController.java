package com.example.cms.CMSController;
import com.example.cms.DAO.UserActivity;
import com.example.cms.ResponseHandler.BaseResponse;
import com.example.cms.UserApplication.LoginDTO;
import com.example.cms.UserApplication.User;
import com.example.cms.UserApplication.UserFeedDTO;
import com.example.cms.service.UserService;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/userfeed")
public class UserFeedController {

    @Autowired
    private UserService userService;

    @Autowired
    public BaseResponse baseResponse;
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFileAndText(
            @RequestParam("email") String email,
            @RequestParam("blogText") String blogText,
            @RequestParam("category") String category,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        baseResponse = new BaseResponse();
        try {
            if (file != null && !file.isEmpty()) {

                UserActivity userActivity = new UserActivity();
                userActivity.setBlogText(blogText);
                userActivity.setUploadedDate(new Date());
                userActivity.setFile(new Binary(file.getBytes()));
                if("video/mp4".equals(file.getContentType())){
                    userActivity.setVideo(true);
                }
                userActivity.setCategory(category);
                userService.addActivityToUserFeed(email, userActivity);

                baseResponse.setStatusCode(HttpStatus.OK.value());
                baseResponse.setStatusMessage("Content Successfully Posted!");
                baseResponse.setResponse("Content Successfully Posted!");
                return new ResponseEntity<>(baseResponse, HttpStatus.OK);
            }
        }catch (Exception e){
        }
        baseResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        baseResponse.setStatusMessage("");
        baseResponse.setError("Something went wrong while uploading Content!");
        return new ResponseEntity<>(baseResponse,HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/get-user-feed")
    public ResponseEntity<?> loginUser(@RequestBody UserFeedDTO userFeedDTO) {
        baseResponse = new BaseResponse();
        List<UserActivity> userActivityList = userService.getUserActivityDetails(userFeedDTO);

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

//        User user = userService.findByEmail(loginDTO.getEmail());
//        if (user == null)
//            return new ResponseEntity<>(baseResponse.failure("User does not exist please register!","Not Found",404), HttpStatus.NOT_FOUND);
//        if (user != null && passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
//            // Handle successful login, e.g., creating a session or generating a JWT
//            baseResponse.setStatusCode(HttpStatus.OK.value());
//            baseResponse.setStatusMessage("User Logged in successfully.");
//            baseResponse.setResponse(user);
//            return new ResponseEntity<>(baseResponse, HttpStatus.OK);
//        }
//        return new ResponseEntity<>(baseResponse.failure("Wrong Password, Please try again!","Un Authorized",401),HttpStatus.UNAUTHORIZED);
    }
}
