package com.example.cms.CMSController;

import com.example.cms.UserApplication.RegistrationDTO;
import com.example.cms.service.UserServiceimpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdministratorController {

    @Autowired
    private UserServiceimpl userServiceimpl;


    @PostMapping("/create")
    public ResponseEntity<?> registerAdmin(@RequestBody RegistrationDTO registrationDTO) throws Exception{
        return userServiceimpl.registration(registrationDTO,true);
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deactivateUser(@RequestBody RegistrationDTO registrationDTO) throws Exception{
        return userServiceimpl.deactivateUser(registrationDTO.getEmail(),registrationDTO.isAdmin());
    }

}
