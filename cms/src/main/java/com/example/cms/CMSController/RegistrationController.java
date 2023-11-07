package com.example.cms.CMSController;

import com.example.cms.UserApplication.LoginDTO;
import com.example.cms.UserApplication.RegistrationDTO;
import com.example.cms.UserApplication.User;
import com.example.cms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationDTO registrationDTO) throws Exception{

        try {
            User user = userService.registerUser(
                    registrationDTO.getFirstName(),
                    registrationDTO.getLastName(),
                    registrationDTO.getDob(),
                    registrationDTO.getEmail(),
                    registrationDTO.getPassword()
            );
            return ResponseEntity.ok(user);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User Already Exist Please Login!");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDTO loginDTO) {
        User user = userService.findByEmail(loginDTO.getEmail());
        if (user != null && passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            // Handle successful login, e.g., creating a session or generating a JWT
            return ResponseEntity.ok("User logged in successfully!");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid login credentials.");
    }

}

