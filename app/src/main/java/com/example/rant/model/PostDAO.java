package com.example.rant.model;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Insert;
import androidx.room.Delete;
import androidx.room.OnConflictStrategy;
import androidx.lifecycle.LiveData;

import java.util.List;

@Dao
public interface PostDAO {
    @Query("select * from Post")
    LiveData<List<Post>> getAll();

    @Query("select * from Post where username like :myUsername")
    LiveData<List<Post>> getMyPosts(String myUsername);

//    @Query("select * from Post where username like :username and lastUpdated like :lastUpdated")
//    LiveData<List<Post>> getOnePost(String username, Long lastUpdated);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Post... posts);

    @Delete
    void delete(Post post);
}