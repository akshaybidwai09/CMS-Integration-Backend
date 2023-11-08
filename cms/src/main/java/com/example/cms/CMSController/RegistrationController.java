package com.example.cms.CMSController;

import com.example.cms.ResponseHandler.BaseResponse;
import com.example.cms.UserApplication.LoginDTO;
import com.example.cms.UserApplication.RegistrationDTO;
import com.example.cms.UserApplication.User;
import com.example.cms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

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


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationDTO registrationDTO) throws Exception{
        baseResponse = new BaseResponse();
        try {
            User user = userService.registerUser(
                    registrationDTO.getFirstName(),
                    registrationDTO.getLastName(),
                    registrationDTO.getDob(),
                    registrationDTO.getEmail(),
                    registrationDTO.getPassword()
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

