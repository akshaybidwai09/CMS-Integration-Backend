package com.example.cms.UserApplication;

import java.util.List;
import java.util.Map;

public class UserFeedDTO {

    private String type;

    private String filterText;

    private String email;

    public void setFilterText(String filterText) {
        this.filterText = filterText;
    }

    private String postId;

    private Map<String, String> comment;

    private List<String> like;


    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public Map<String, String> getComment() {
        return comment;
    }

    public void setComment(Map<String, String> comment) {
        this.comment = comment;
    }

    public List<String> getLike() {
        return like;
    }

    public void setLike(List<String> like) {
        this.like = like;
    }



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilterText() {
        return filterText;
    }

}
