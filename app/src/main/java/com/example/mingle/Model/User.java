package com.example.mingle.Model;

public class User {
    private String UserName;
    private String Id;
    private String Email;
    private String bio;
    private String Name;
    private String imageUrl;

    public User() {
    }

    public User(String UserName, String Id, String Email, String bio, String Name, String imageUrl) {
        this.UserName = UserName;
        this.Id = Id;
        this.Email = Email;
        this.bio = bio;
        this.Name = Name;
        this.imageUrl = imageUrl;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String UserName) {
        this.UserName = UserName;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        this.Email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
