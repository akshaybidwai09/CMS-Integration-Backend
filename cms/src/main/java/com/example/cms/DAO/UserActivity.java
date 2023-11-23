package com.example.cms.DAO;

import com.example.cms.UserApplication.Comment;
import org.bson.types.Binary; // Import Binary from the BSON library
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserActivity {

    @Id
    private String id;

    private String userName;

    private String email;

    private String blogText;
    private Date uploadedDate;
    private Binary file; // Change the type to Binary for MongoDB storage
    private String category;

    private boolean isVideo;

    private List<Comment> feedbacks;
    private List<User> likes;

    public List<Comment> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<Comment> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public List<User> getLikes() {
        return likes;
    }

    public void setLikes(List<User> likes) {
        this.likes = likes;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

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
