package com.example.cms.UserApplication;


import com.example.cms.DAO.User;
import lombok.Data;

import java.util.Date;

@Data
public class Comment {
    private User user;
    private String comment;
    private Date commentDate;
}
