package com.tanthin.communityblog.Models;

import com.tanthin.communityblog.Activities.HomeActivity;
import com.tanthin.communityblog.Activities.LoginActivity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {

    // keys save on Firebase Database
    public static final String KEY_USER_MODEL = "Users";
    public static final String KEY_USER_POSTS_MODEL = "UserPosts";
    public static final String KEY_NAME = "name";
    public static final String KEY_MAIL = "mail";
    public static final String KEY_UID = "uid";
    public static final String KEY_ALL_POSTS = "allPosts";
    public static final String KEY_BIO = "userBio";
    public static final String KEY_COVER = "userCover";
    public static final String KEY_PHOTO = "userPhoto";

    public static final String EMPTY_USER_COVER = "empty";
    public static final String BLANK_USER_BIO = "Blank bio";

    private String name, mail, uid, userPhoto, userCover, userBio;
    private Map<String, String> allPosts;   // <postKey, title>
    private Map<String, String> favorite;   // <postKey, title>

    public User() {
        // empty constructor for get data from Firebase
    }

    public User(String name, String mail, String uid, String userPhoto) {
        this.name = name;
        this.mail = mail;
        this.uid = uid;
        this.userPhoto = userPhoto;
        this.userCover = LoginActivity.getDefaultCoverUri();
        this.userBio = BLANK_USER_BIO;
        this.allPosts = new HashMap<>();
        this.favorite = new HashMap<>();
    }

//    public void addPost(String postKey, String title) {
//        if (allPosts == null) {
//            allPosts = new HashMap<>();
//        }
//        allPosts.put(postKey, title);
//    }

    public String getUserBio() {
        return userBio;
    }

    public void setUserBio(String userBio) {
        this.userBio = userBio;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getUserCover() {
        return userCover;
    }

    public void setUserCover(String userCover) {
        this.userCover = userCover;
    }

    public Map<String, String> getAllPosts() {
        return allPosts;
    }

    public void setAllPosts(Map<String, String> allPosts) {
        this.allPosts = allPosts;
    }
}
