package com.example.cms.CMSController;

import com.example.cms.DAO.UserActivity;
import com.example.cms.ResponseHandler.BaseResponse;
import com.example.cms.UserApplication.LoginDTO;
import com.example.cms.UserApplication.RegistrationDTO;
import com.example.cms.UserApplication.User;
import com.example.cms.service.UserService;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public BaseResponse baseResponse;

    public RegistrationController(BaseResponse baseResponse) {
        this.baseResponse = new BaseResponse();
    }


    @PostMapping("/upload")
    public ResponseEntity<?> uploadFileAndText(
            @RequestParam("email") String email,
            @RequestParam("blogText") String blogText,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        BaseResponse baseResponse = new BaseResponse();
        try {
            if (file != null && !file.isEmpty()) {
                // Create a new UserActivity
                UserActivity userActivity = new UserActivity();
                userActivity.setBlogText(blogText);
                userActivity.setUploadedDate(new Date());
                userActivity.setFile(new Binary(file.getBytes()));

                // Add the UserActivity to the user's feed
                userService.addActivityToUserFeed(email, userActivity);

                baseResponse.setStatusCode(HttpStatus.OK.value());
                baseResponse.setStatusMessage("Content Successfully Posted!");
                baseResponse.setResponse("Content Successfully Posted!");
                return new ResponseEntity<>(baseResponse, HttpStatus.OK);
            } else {
                // Handle the case where file is null or empty
                baseResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                baseResponse.setStatusMessage("File is required.");
                baseResponse.setError("No file was uploaded.");
                return new ResponseEntity<>(baseResponse, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            // Exception handling
            baseResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponse.setStatusMessage("Internal Server Error");
            baseResponse.setError("An error occurred: " + e.getMessage());
            return new ResponseEntity<>(baseResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDTO loginDTO) {
        User user = userService.findByEmail(loginDTO.getEmail());
        if (user == null)
            return new ResponseEntity<>(baseResponse.failure("User does not exist please register!","Not Found",404),HttpStatus.NOT_FOUND);
        if (user != null && passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            // Handle successful login, e.g., creating a session or generating a JWT
            baseResponse.setStatusCode(HttpStatus.OK.value());
            baseResponse.setStatusMessage("User Logged in successfully.");
            baseResponse.setResponse(user);
            return new ResponseEntity<>(baseResponse, HttpStatus.OK);
        }
        return new ResponseEntity<>(baseResponse.failure("Wrong Password, Please try again!","Un Authorized",401),HttpStatus.UNAUTHORIZED);
    }

}

