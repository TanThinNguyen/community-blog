package com.tanthin.communityblog.Models;

import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Post implements Serializable {

    // keys save on Firebase Database
    public static final String KEY_POSTS_MODEL = "Posts";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_PICTURE = "picture";
    public static final String KEY_POSTKEY = "postKey";
    public static final String KEY_TIMESTAMP = "timeStamp";
    public static final String KEY_TITLE = "title";
    public static final String KEY_USERID = "userId";
    public static final String KEY_USERNAME = "userName";
    public static final String KEY_USERPHOTO = "userPhoto";
    public static final String KEY_VIEWS = "views";

    private String title;
    private String description;
    private String picture;
    private String userName;
    private String userId;
    private String userPhoto;
    private Object timeStamp;
    private String postKey;
    private Map<String, Integer> views;

    public Post() {
        // empty constructor for get data from Firebase
    }

    public Post(String title, String description, String picture, String userName, String userId, String userPhoto) {
        this.title = title;
        this.description = description;
        this.picture = picture;
        this.userName = userName;
        this.userId = userId;
        this.userPhoto = userPhoto;
        this.timeStamp = ServerValue.TIMESTAMP;
        this.views = null;
    }

    public Post(String postKey, String title, String description, String picture, String userName, String userId, String userPhoto) {
        this.postKey = postKey;
        this.title = title;
        this.description = description;
        this.picture = picture;
        this.userName = userName;
        this.userId = userId;
        this.userPhoto = userPhoto;
        this.timeStamp = ServerValue.TIMESTAMP;
        this.views = null;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put(KEY_TITLE, this.title);
        result.put(KEY_DESCRIPTION, this.description);
        result.put(KEY_PICTURE, this.picture);
        result.put(KEY_USERNAME, this.userName);
        result.put(KEY_USERID, this.userId);
        result.put(KEY_USERPHOTO, this.userPhoto);
        result.put(KEY_TIMESTAMP, this.timeStamp);
        result.put(KEY_POSTKEY, this.postKey);
        result.put(KEY_VIEWS, this.views);

        return result;
    }

    public int currentViews() {
        if (views == null) {
            return 0;
        }
        return views.size();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public Object getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Object timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Map<String, Integer> getViews() {
        return views;
    }

    public void setViews(Map<String, Integer> views) {
        this.views = views;
    }
}
