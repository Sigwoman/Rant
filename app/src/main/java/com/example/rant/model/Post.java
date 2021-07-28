package com.example.rant.model;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import com.example.rant.MyApplication;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import org.jetbrains.annotations.NotNull;

@Entity
public class Post {
    public final static String ID = "id";
    public final static String USERNAME = "username";
    public final static String DESCRIPTION = "description";
    public final static String LIKES = "likes";
    public final static String AVATAR = "avatar";
    public final static String POST_IMAGE = "postImage";
    public final static String LAST_UPDATED = "lastUpdated";
    public final static String IS_DELETED = "isDeleted";

    @NonNull
    @PrimaryKey
    private String id;;
    private String username;
    private String description;
    private Integer likes;
    private String avatar;
    private String postImage;
    private Long lastUpdated;
    private Boolean isDeleted;

    // Constructor
    public Post(String username, String description, String avatar, String postImage, Long lastUpdated) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.description = description;
        this.likes = 0;
        this.avatar = avatar;
        this.postImage = postImage;
        this.lastUpdated = lastUpdated;
        this.isDeleted = false;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDescription() {
        return description;
    }

    public Integer getLikes() {
        return likes;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getPostImage() {
        return postImage;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    // Setters
    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public void like() {
        this.likes++;
        // TODO
//        Model.instance.savePost(this, () -> {});
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
        this.lastUpdated = Long.valueOf(new Date().getTime());
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    // Json functions
    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<>();

        json.put(ID, id);
        json.put(USERNAME, username);
        json.put(DESCRIPTION, description);
        json.put(LIKES, likes);
        json.put(AVATAR, avatar);
        json.put(POST_IMAGE, postImage);
        json.put(LAST_UPDATED, FieldValue.serverTimestamp());
        json.put(IS_DELETED, isDeleted);
        return json;
    }

    // TODO: make null checks
    public static Post createFromJson(Map<String, Object> json) {
        String newID = json.get(ID) != null ? (String)json.get(ID) : "";
        String newUsername = json.get(USERNAME) != null ? (String)json.get(USERNAME) : "";
        String newDescription = json.get(DESCRIPTION) != null ? (String)json.get(DESCRIPTION) : "";
        Integer newLikes = json.get(LIKES) != null ? ((Long)json.get(LIKES)).intValue() : 0;
        String newAvatar = json.get(AVATAR) != null ? (String)json.get(AVATAR) : "";
        String newPostImage = json.get(POST_IMAGE) != null ? (String)json.get(POST_IMAGE) : "";
        Timestamp ts = (Timestamp)json.get(LAST_UPDATED);
        Long newLastUpdated = Long.valueOf(ts != null ? ts.getSeconds() : 0);
        Boolean newIsDeleted = json.get(IS_DELETED) != null ? (Boolean)json.get(IS_DELETED) : false;

        Post newPost = new Post(newUsername, newDescription, newAvatar, newPostImage, newLastUpdated);
        newPost.setId(newID);
        newPost.setLikes(newLikes);
        newPost.setDeleted(newIsDeleted);

        return newPost;
    }

    // LastUpdated functions
    private static final String ALL_POSTS_LAST_UPDATE_DATE = "AllPostsLastUpdate";

    public static Long getLocalAllPostsLastUpdateTime() {
        return MyApplication.context.getSharedPreferences("TAG", Context.MODE_PRIVATE).getLong(ALL_POSTS_LAST_UPDATE_DATE, 0);
    }

    public static void setLocalAllPostsLastUpdateTime(Long time) {
        SharedPreferences.Editor editor = MyApplication.context.getSharedPreferences("TAG", Context.MODE_PRIVATE).edit();
        editor.putLong(ALL_POSTS_LAST_UPDATE_DATE, time);
        editor.commit();
    }

    private static final String MY_POSTS_LAST_UPDATE_DATE = "MyPostsLastUpdate";

    public static Long getLocalMyPostsLastUpdateTime() {
        return MyApplication.context.getSharedPreferences("TAG", Context.MODE_PRIVATE).getLong(MY_POSTS_LAST_UPDATE_DATE, 0);
    }

    public static void setLocalMyPostsLastUpdateTime(Long time) {
        SharedPreferences.Editor editor = MyApplication.context.getSharedPreferences("TAG", Context.MODE_PRIVATE).edit();
        editor.putLong(MY_POSTS_LAST_UPDATE_DATE, time);
        editor.commit();
    }
}
