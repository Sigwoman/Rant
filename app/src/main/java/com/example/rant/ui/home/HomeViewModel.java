package com.example.rant.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.rant.model.Post;
import com.example.rant.model.Model;

import java.util.List;

public class HomeViewModel extends ViewModel {
    private LiveData<List<Post>> posts;

    public HomeViewModel() {
        posts = Model.instance.getAllPosts();
    }

    public LiveData<List<Post>> getAllPosts() {
        return posts;
    }

    public void refresh() {
        Model.instance.getAllPosts();
    }
}