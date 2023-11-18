package com.example.cms.UserApplication;

public class UserFeedDTO {

    private String type;

    private String filterText;

    public void setFilterText(String filterText) {
        this.filterText = filterText;
    }

    private String email;

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
