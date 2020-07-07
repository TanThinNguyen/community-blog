package com.tanthin.communityblog.Models;

import com.google.firebase.database.ServerValue;

public class Comment {

    // keys save on Firebase Database
    public static final String KEY_COMMENT_MODEL = "Comments";

    private String content, uId, uImg, uName, commentKey;
    private Object timestamp;

    public Comment() {
    }

    public Comment(String content, String uId, String uImg, String uName) {
        this.content = content;
        this.uId = uId;
        this.uImg = uImg;
        this.uName = uName;

        this.timestamp = ServerValue.TIMESTAMP;
    }

    public Comment(String content, String uId, String uImg, String uName, Object timestamp) {
        this.content = content;
        this.uId = uId;
        this.uImg = uImg;
        this.uName = uName;
        this.timestamp = timestamp;
    }

    public String getCommentKey() {
        return commentKey;
    }

    public void setCommentKey(String commentKey) {
        this.commentKey = commentKey;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getuImg() {
        return uImg;
    }

    public void setuImg(String uImg) {
        this.uImg = uImg;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
