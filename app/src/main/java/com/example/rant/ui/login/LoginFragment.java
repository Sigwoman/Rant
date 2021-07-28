package com.example.rant.ui.login;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rant.MyApplication;
import com.example.rant.R;
import com.example.rant.model.ModelFirebase;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class LoginFragment extends Fragment {
    View rootView;
    ProgressBar progressBar;
    NavDirections loginAction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get root view
        rootView = inflater.inflate(R.layout.fragment_login, container, false);

        // Set progress bar
        progressBar = rootView.findViewById(R.id.login_progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        // Set button for register
        TextView registerBtn = rootView.findViewById(R.id.login_register_link);

        registerBtn.setOnClickListener(view -> {
            NavDirections actionToRegister = LoginFragmentDirections.actionLoginFragmentToRegisterFragment();
            Navigation.findNavController(view).navigate(actionToRegister);
        });

        // Manage login
        loginAction = LoginFragmentDirections.actionLoginFragmentToNavigationHome();

        Button loginBtn = rootView.findViewById(R.id.login_btn);

        loginBtn.setOnClickListener(view -> {
            login();
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        BottomNavigationView navBar = getActivity().findViewById(R.id.nav_view);
        navBar.setVisibility(View.GONE);

        // Check if user is signed in (non-null) and update UI accordingly.

        if (ModelFirebase.isAuthUserExist()) {
            NavDirections loginAction = LoginFragmentDirections.actionLoginFragmentToNavigationHome();
            Navigation.findNavController(this.getActivity(), R.id.nav_host_fragment_activity_main).navigate(loginAction);
        }
    }

    private void login() {
        progressBar.setVisibility(View.VISIBLE);

        String username = ((EditText)rootView.findViewById(R.id.login_username_field)).getText().toString();
        String password = ((EditText)rootView.findViewById(R.id.login_password_field)).getText().toString();

        // TODO: check user credentials
        if (username.length() > 0 && password.length() > 0) {
            ModelFirebase.loginUser(username, password, new ModelFirebase.Listener<Boolean>() {
                @Override
                public void onComplete() {
                    Navigation.findNavController(rootView).navigate(loginAction);
                }

                @Override
                public void onFail() {
                    progressBar.setVisibility(View.GONE);
                }
            });
//            FirebaseAuth.getInstance().signInWithEmailAndPassword(username + "@mail.com", password)
//                .addOnCompleteListener(this.getActivity(), task -> {
//                    if (task.isSuccessful()) {
//                        // Sign in success, update UI with the signed-in user's information
//                        Navigation.findNavController(rootView).navigate(loginAction);
//                    } else {
//                        progressBar.setVisibility(View.GONE);
//                        Log.w("TAG", "signInWithEmail:failure", task.getException());
//
//                        // If sign in fails, display a message to the user.
//                        Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
//                    }
//                });
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(MyApplication.context, "Please fill all data fields", Toast.LENGTH_SHORT).show();
        }
    }

}