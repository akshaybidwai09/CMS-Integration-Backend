package com.example.cms.CMSController;

import com.example.cms.ResponseHandler.BaseResponse;
import com.example.cms.UserApplication.LoginDTO;
import com.example.cms.UserApplication.RegistrationDTO;
import com.example.cms.DAO.User;
import com.example.cms.service.UserServiceimpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    @Autowired
    private UserServiceimpl userServiceimpl;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public BaseResponse baseResponse;

    public RegistrationController(BaseResponse baseResponse) {
        this.baseResponse = new BaseResponse();
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationDTO registrationDTO) throws Exception{
        return userServiceimpl.registration(registrationDTO,false);
    }

    @PostMapping("/getUserAdmins")
    public ResponseEntity<?> getUserAdmins(@RequestBody RegistrationDTO registrationDTO) throws Exception{
        return userServiceimpl.getUserAdmins(registrationDTO.isAdmin());
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDTO loginDTO) {
        User user = userServiceimpl.findByEmail(loginDTO.getEmail());
        if (!user.isActive()){
            return new ResponseEntity<>(baseResponse.failure("User Suspended Temporarily","Not Found",401),HttpStatus.UNAUTHORIZED);
        }
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

