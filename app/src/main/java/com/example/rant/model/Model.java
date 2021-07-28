package com.example.rant.model;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Model {
    public static final Model instance = new Model();

    ExecutorService executorService = Executors.newFixedThreadPool(4);

    public enum LoadingState {
        loaded,
        loading,
        error
    }

    public MutableLiveData<LoadingState> postsLoadingState = new MutableLiveData<>(LoadingState.loaded);

    LiveData<List<Post>> allPosts = AppLocalDB.db.postDAO().getAll();

    public LiveData<List<Post>> getAllPosts() {
        postsLoadingState.setValue(LoadingState.loading);

        Long localLastUpdate = Post.getLocalAllPostsLastUpdateTime();

        ModelFirebase.getAllPosts(localLastUpdate, posts -> {
            executorService.execute(() -> {
                Long currLastUpdate = new Long(0);

                for (Post currPost : posts) {
                    if(currPost.getDeleted()) {
                        AppLocalDB.db.postDAO().delete(currPost);
                    } else {
                        AppLocalDB.db.postDAO().insertAll(currPost);
                    }

                    currLastUpdate = Math.max(currLastUpdate, currPost.getLastUpdated());
                }

                Post.setLocalAllPostsLastUpdateTime(currLastUpdate);

                postsLoadingState.postValue(LoadingState.loaded);
            });
        });

        return allPosts;
    }

    LiveData<List<Post>> myPosts = AppLocalDB.db.postDAO().getMyPosts(ModelFirebase.getAuthUserName());

    public LiveData<List<Post>> getMyPosts() {
        postsLoadingState.setValue(LoadingState.loading);

        Long localLastUpdate = Post.getLocalMyPostsLastUpdateTime();

        ModelFirebase.getMyPosts(localLastUpdate, posts -> {
            executorService.execute(() -> {
                Long currLastUpdate = new Long(0);

                for (Post currPost : posts) {
                    if(currPost.getDeleted()) {
                        AppLocalDB.db.postDAO().delete(currPost);
                    } else {
                        AppLocalDB.db.postDAO().insertAll(currPost);
                    }

                    currLastUpdate = Math.max(currLastUpdate, currPost.getLastUpdated());
                }

                Post.setLocalMyPostsLastUpdateTime(currLastUpdate);

                postsLoadingState.postValue(LoadingState.loaded);
            });
        });

        return myPosts;
    }

    public interface OnCompleteListener {
        void onComplete();
    }

    public void savePost(Post newPost, OnCompleteListener listener) {
        postsLoadingState.setValue(LoadingState.loading);

        // TODO
        ModelFirebase.savePost(newPost, () -> {
            getAllPosts();
            getMyPosts();

            listener.onComplete();
        });
    }

    public void delete(Post post, OnCompleteListener listener) {
        post.setDeleted(true);
        savePost(post, listener);
    }

    public interface UploadImageListener {
        void onComplete(String uri);
    }

    public void uploadImage(Bitmap imageBmp, String name, final UploadImageListener listener) {
        ModelFirebase.uploadImage(imageBmp, name, listener);
    }
}
