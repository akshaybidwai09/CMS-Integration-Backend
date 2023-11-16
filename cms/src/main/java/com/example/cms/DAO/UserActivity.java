package com.example.cms.DAO;

import org.bson.types.Binary; // Import Binary from the BSON library
import java.util.Date;

public class UserActivity {
    private String blogText;
    private Date uploadedDate;
    private Binary file; // Change the type to Binary for MongoDB storage

    public String getBlogText() {
        return blogText;
    }

    public void setBlogText(String blogText) {
        this.blogText = blogText;
    }

    public Binary getFile() { // Change return type to Binary
        return file;
    }

    public void setFile(Binary file) { // Change parameter type to Binary
        this.file = file;
    }

    public Date getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(Date uploadedDate) {
        this.uploadedDate = uploadedDate;
    }
}
