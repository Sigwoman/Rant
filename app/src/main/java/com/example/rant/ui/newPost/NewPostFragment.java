package com.example.rant.ui.newPost;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Button;

import com.example.rant.R;
import com.example.rant.model.ModelFirebase;
import com.example.rant.model.Post;
import com.example.rant.model.Model;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FieldValue;
import com.squareup.picasso.Picasso;

import java.util.Date;

public class NewPostFragment extends Fragment {
    View rootView;
    Post _post;
    String _id;
    String _image;
    String _description;
    int _likes;
    Boolean _deleted;

    EditText postDescription;
    ProgressBar progressBar;
    ImageView postImage;
    Button addImageBtn;
    Button postBtn;

    Bitmap imageBitmap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Hide NavBar
        BottomNavigationView navBar = getActivity().findViewById(R.id.nav_view);
        navBar.setVisibility(View.GONE);

        // Get args
        _id = NewPostFragmentArgs.fromBundle(getArguments()).getId();
        _image = NewPostFragmentArgs.fromBundle(getArguments()).getImage();
        _description = NewPostFragmentArgs.fromBundle(getArguments()).getDescription();
        _likes = NewPostFragmentArgs.fromBundle(getArguments()).getLikes();
        _deleted = NewPostFragmentArgs.fromBundle(getArguments()).getIsDeleted();

        ModelFirebase.getOnePost(_id, post -> {
            _post = post;
        });

        // Get root view
        rootView = inflater.inflate(R.layout.fragment_new_post, container, false);

        postDescription = rootView.findViewById(R.id.newPost_text_view);
        postDescription.setText(_description);

        postImage = rootView.findViewById(R.id.newPost_postImage);
        if (_image != null && !_image.equals("")) {
            Picasso.get().load(_image).into(postImage);
        }

        // Set progress bar
        progressBar = rootView.findViewById(R.id.newPost_progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        // Manage image button
        addImageBtn = rootView.findViewById(R.id.newPost_addImageBtn);
        addImageBtn.setOnClickListener(view -> takePicture());

        // Manage post
        postBtn = rootView.findViewById(R.id.newPost_btn);
        if (!_id.equals("")) {
            postBtn.setText("REPOST");
        }

        postBtn.setOnClickListener(view -> {
            if (imageBitmap != null) {
                Model.instance.uploadImage(imageBitmap, postDescription.getText().toString(), url -> {
                    post(url);
                });
            } else {
                post("");
            }
        });

        return rootView;
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            postImage.setImageBitmap(imageBitmap);
        }
    }

    private void post(String url) {
        progressBar.setVisibility(View.VISIBLE);
        postBtn.setEnabled(false);

        String newDescription = postDescription.getText().toString();
        String newPostImage = url == null ? "" : url;
        Long newLastUpdated= Long.valueOf(new Date().getTime());
        String newAvatar = ModelFirebase.getAuthUserUrl();

        Post newPost = new Post(ModelFirebase.getAuthUserName(), newDescription, newAvatar, newPostImage, newLastUpdated);
        if (!_id.equals("")) {
            newPost.setId(_id);
            newPost.setLikes(_likes);
            newPost.setDeleted(_deleted);
        }

        Model.instance.savePost(newPost, () -> {
            Navigation.findNavController(rootView).navigateUp();
        });
    }
}