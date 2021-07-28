package com.example.rant.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.rant.model.Post;
import com.example.rant.model.Model;

import java.util.List;

public class ProfileViewModel extends ViewModel {
    private LiveData<List<Post>> posts;

    public ProfileViewModel() {
        posts = Model.instance.getMyPosts();
    }

    public LiveData<List<Post>> getMyPosts() {
        return posts;
    }

    public void refresh() {
        Model.instance.getMyPosts();
    }
}