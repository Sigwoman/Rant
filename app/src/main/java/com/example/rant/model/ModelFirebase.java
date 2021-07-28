package com.example.rant.model;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.rant.MyApplication;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.LinkedList;

public class ModelFirebase {
    final static String postsCollection = "posts";

    public interface GetPostsListener {
        void onComplete(List<Post> posts);
    }

    public static void getAllPosts(Long since, GetPostsListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(postsCollection)
                .whereGreaterThanOrEqualTo(Post.LAST_UPDATED, new Timestamp(since, 0))
                .get()
                .addOnCompleteListener(task -> {
                    List<Post> list = new LinkedList<>();

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            list.add(Post.createFromJson(document.getData()));
                        }
                    } else {
                        // TODO
                    }

                    listener.onComplete(list);
                });
    }

    public static void getMyPosts(Long since, GetPostsListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(postsCollection)
                .whereGreaterThanOrEqualTo(Post.LAST_UPDATED, new Timestamp(since, 0))
                .get()
                .addOnCompleteListener(task -> {
                    List<Post> list = new LinkedList<>();

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                            if(document.getData().get(Post.USERNAME).equals(userName)) {
                                list.add(Post.createFromJson(document.getData()));
                            }
                        }
                    } else {
                        // TODO
                    }

                    listener.onComplete(list);
                });
    }

    public interface GetOnePostListener {
        void onComplete(Post post);
    }

    public static void getOnePost(String id, GetOnePostListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(postsCollection)
                .document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            listener.onComplete(Post.createFromJson(document.getData()));
                        } else {
                            Log.d("TAG", "No such document");
                        }
                    } else {
                        Log.d("TAG", "get failed with ", task.getException());
                    }
                });
    }

    public static void savePost(Post newPost, Model.OnCompleteListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Add a new document with a generated ID
        db.collection(postsCollection)
                .document(newPost.getId())
                .set(newPost.toJson())
                .addOnSuccessListener(unused -> listener.onComplete())
                .addOnFailureListener(e -> listener.onComplete());
    }

    public static void uploadImage(Bitmap imageBmp, String name, final Model.UploadImageListener listener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference imagesRef = storage.getReference().child("photos").child(name);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        imageBmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imagesRef.putBytes(data);
        uploadTask
            .addOnFailureListener(exception -> {
                listener.onComplete(null);
            })
            .addOnSuccessListener(taskSnapshot -> {
                imagesRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Uri downloadUrl = uri;
                    listener.onComplete(downloadUrl.toString());
                });
            });
    }

//    public interface OnCompleteListener {
//        void onComplete();
//        void onFail();
//    }
//
//    // Firebase Auth
//    public static void createUserWithUsernameAndPassword(String username, String password, FragmentActivity activity, Bitmap imageBitmap, OnCompleteListener listener) {
//        FirebaseAuth.getInstance().signInWithEmailAndPassword(username + "@mail.com", password)
//            .addOnCompleteListener(activity, task -> {
//                if (task.isSuccessful()) {
//                    if (imageBitmap != null) {
//                        Model.instance.uploadImage(imageBitmap, username, url -> {
//                            setUserDetails(username, url);
//                        });
//                    } else {
//                        setUserDetails(username, "");
//                    }
//                    listener.onComplete();
//                } else {
//                    Log.w("TAG", "createUserWithEmail:failure", task.getException());
//                    listener.onFail();
//                }
//            });
//    }
//
//    private static void setUserDetails(String username, String url) {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                .setDisplayName(username)
//                .setPhotoUri(Uri.parse(url == null ? "" : url))
//                .build();
//
//        user.updateProfile(profileUpdates);
//    }

    public interface Listener<T>
    {
        void onComplete();
        void onFail();
    }

    public static String getAuthUserName() {
        return isAuthUserExist() ? FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "";
    }

    public static String getAuthUserUrl() {
        return isAuthUserExist() ?
                FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null ?
                        FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString()
                        : ""
                : "";
    }
    
    public static Boolean isAuthUserExist() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static void loginUser(final String username, String password, final Listener<Boolean> listener) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

//        if (username != null && !username.equals("") && password != null && !password.equals("")) {
//            if (firebaseAuth.getCurrentUser() != null) {
//                firebaseAuth.signOut();
//            }

            firebaseAuth.signInWithEmailAndPassword(username + "@gmail.com", password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(MyApplication.context, "Welcome!", Toast.LENGTH_SHORT).show();

                    listener.onComplete();
                }).addOnFailureListener(e -> {
                    Toast.makeText(MyApplication.context, "Failed to login: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    listener.onFail();
                });
//        }
//        else {
//            Toast.makeText(MyApplication.context, "Please fill all data fields", Toast.LENGTH_SHORT).show();
//        }
    }

    public static void registerUserAccount(String username, String password, Bitmap imageBitmap, Listener<Boolean> listener) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(username + "@gmail.com", password)
            .addOnSuccessListener(authResult -> {

                if (imageBitmap != null) {
                    Model.instance.uploadImage(imageBitmap, username, url -> {
                        setUserDetails(username, url);
                    });
                } else {
                    setUserDetails(username, "");
                }

                listener.onComplete();
            }).addOnFailureListener(e -> {
                Toast.makeText(MyApplication.context, "Failed registering user", Toast.LENGTH_SHORT).show();

                listener.onFail();
        });
    }

    private static void setUserDetails(String username, String url) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .setPhotoUri(Uri.parse(url == null ? "" : url))
                .build();

        user.updateProfile(profileUpdates);

        Toast.makeText(MyApplication.context, "User registered", Toast.LENGTH_SHORT).show();
    }

    public static void logoutAuth() {
        FirebaseAuth.getInstance().signOut();
    }
}
