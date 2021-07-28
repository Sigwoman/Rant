package com.example.rant.ui.register;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.rant.R;
import com.example.rant.model.ModelFirebase;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class RegisterFragment extends Fragment {
    View rootView;

    EditText usernameET;
    EditText passwordEt;
    ImageButton userImage;
    ProgressBar progressBar;
    Button registerBtn;

    Bitmap imageBitmap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Hide NavBar
        BottomNavigationView navBar = getActivity().findViewById(R.id.nav_view);

        navBar.setVisibility(View.GONE);

        // Get root view
        rootView = inflater.inflate(R.layout.fragment_register, container, false);

        usernameET = rootView.findViewById(R.id.register_username_field);
        passwordEt = rootView.findViewById(R.id.register_password_field);

        // Set progress bar
        progressBar = rootView.findViewById(R.id.register_progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        // Manage image button
        userImage = rootView.findViewById(R.id.register_image);
        userImage.setOnClickListener(view -> takePicture());

        // Manage register
        registerBtn = rootView.findViewById(R.id.register_btn);

        registerBtn.setOnClickListener(view -> {
//            register();
            progressBar.setVisibility(View.VISIBLE);

            String username = usernameET.getText().toString();
            String password = passwordEt.getText().toString();

            if (username != null && username.length() > 0 &&
                    password != null && password.length() > 0) {
                ModelFirebase.registerUserAccount(username, password, imageBitmap,
                        new ModelFirebase.Listener<Boolean>() {
                            @Override
                            public void onComplete() {
                                Navigation.findNavController(rootView).navigateUp();
                            }

                            @Override
                            public void onFail() {
                                progressBar.setVisibility(View.GONE);
                            }
                        });
            } else {
                progressBar.setVisibility(View.GONE);

                Toast.makeText(getActivity(), "Please fill all input fieldsin", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    // Camera functions
    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            userImage.setImageBitmap(imageBitmap);
        }
    }

//    // Register functions
//    void register() {
//        progressBar.setVisibility(View.VISIBLE);
//
//        String username = usernameET.getText().toString();
//        String password = passwordEt.getText().toString();
//
//        // TODO: check details
//        if (username.length() > 0 && password.length() > 0) {
//            FirebaseAuth.getInstance().createUserWithEmailAndPassword(username + "@mail.com", password)
//                    .addOnCompleteListener(this.getActivity(), task -> {
//                        if (task.isSuccessful()) {
//                            if (imageBitmap != null) {
//                                Model.instance.uploadImage(imageBitmap, username, url -> {
//                                    setUserDetails(url);
//                                });
//                            } else {
//                                setUserDetails("");
//                            }
//
//                            Navigation.findNavController(rootView).navigateUp();
//                        } else {
//                            progressBar.setVisibility(View.GONE);
//                            // TODO
//                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
//                            Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        } else {
//            progressBar.setVisibility(View.GONE);
//
//            Toast.makeText(getActivity(), "Invalid fields, try again", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    void setUserDetails(String url) {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                .setDisplayName(usernameET.getText().toString())
//                .setPhotoUri(Uri.parse(url == null ? "" : url))
//                .build();
//
//        user.updateProfile(profileUpdates);
//    }
}