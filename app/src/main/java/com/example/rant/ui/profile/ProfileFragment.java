package com.example.rant.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.rant.R;
import com.example.rant.model.Model;
import com.example.rant.model.ModelFirebase;
import com.example.rant.model.Post;
import com.example.rant.ui.CircleTransform;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

public class ProfileFragment extends Fragment {
    private ProfileViewModel profileViewModel;

    MyAdapter adapter;

    View rootView;
    SwipeRefreshLayout swipeRefresh;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Get root view
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Display user's details
        TextView userName = rootView.findViewById(R.id.profile_username);
        userName.setText(ModelFirebase.getAuthUserName());

        ImageView userImage = rootView.findViewById(R.id.post_userImage);
        userImage.setImageResource(R.drawable.user_placeholder);

        if (!ModelFirebase.getAuthUserUrl().equals("")) {
            Picasso.get().load(ModelFirebase.getAuthUserUrl())
                    .placeholder(R.drawable.user_placeholder).error(R.drawable.user_placeholder)
                    .into(userImage);
        }

        // Get my posts list
        RecyclerView recycler = rootView.findViewById(R.id.profile_recycler);
        recycler.setHasFixedSize(true);

        // Connect posts list to manager
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this.getContext());
        recycler.setLayoutManager(manager);

        // Get my posts data
        profileViewModel.getMyPosts().observe(getViewLifecycleOwner(), data -> adapter.notifyDataSetChanged());

        // Set adapter to the list
        adapter = new MyAdapter();
        recycler.setAdapter(adapter);

        // Manage logout button
        Button logoutBtn = rootView.findViewById(R.id.profile_logout_btn);

        NavDirections logoutAction = ProfileFragmentDirections.actionNavigationProfileToLoginFragment();

        logoutBtn.setOnClickListener(v -> {
            ModelFirebase.logoutAuth();

            Navigation.findNavController(v).navigate(logoutAction);
        });

        // Manage add new post button
        FloatingActionButton addPostBtn = rootView.findViewById(R.id.profile_add_post_btn);

        NavDirections addPostAction = ProfileFragmentDirections.actionGlobalNewPostFragment("", "", "", 0, false);

        addPostBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(addPostAction));

        // Handle refresh
        setupProgressListener();

        swipeRefresh = rootView.findViewById(R.id.home_swipeRefresh);
        swipeRefresh.setOnRefreshListener(() -> {
            profileViewModel.refresh();
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        BottomNavigationView navBar = getActivity().findViewById(R.id.nav_view);
        navBar.setVisibility(View.VISIBLE);
    }

    private void setupProgressListener() {
        Model.instance.postsLoadingState.observe(getViewLifecycleOwner(), (state) -> {
            switch (state) {
                case loaded:
                    swipeRefresh.setRefreshing(false);
                    break;

                case loading:
                    swipeRefresh.setRefreshing(true);
                    break;

                case error:
                    // TODO: display error message
                    break;
            }
        });
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        Post post;
        Button optionsBtn;
        TextView username;
        TextView description;
        ImageView userImage;
        ImageView postImage;
        TextView likesCount;

        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            optionsBtn = itemView.findViewById(R.id.post_optionsBtn);
            username = itemView.findViewById(R.id.profile_username);
            description = itemView.findViewById(R.id.post_description);
            userImage = itemView.findViewById(R.id.post_userImage);
            postImage = itemView.findViewById(R.id.post_postImage);
            likesCount = itemView.findViewById(R.id.post_likes_count);

            // Hide irrelevant
            itemView.findViewById(R.id.post_divider).setVisibility(View.GONE);
            itemView.findViewById(R.id.post_like_btn).setVisibility(View.GONE);
        }

        public void bind(Post currPost) {
            post = currPost;
            username.setText(currPost.getUsername());
            description.setText(currPost.getDescription());
            likesCount.setText(Integer.toString(currPost.getLikes()));

            // Handle user image
            userImage.setImageResource(R.drawable.user_placeholder);

            if (currPost.getAvatar() != null && !currPost.getAvatar().equals("")) {
                Picasso.get().load(currPost.getAvatar()).transform(new CircleTransform())
                        .placeholder(R.drawable.user_placeholder).error(R.drawable.user_placeholder)
                        .into(userImage);
            }

            // Handle post image
            if (currPost.getPostImage() != null && !currPost.getPostImage().equals("")) {
                postImage.setVisibility(View.VISIBLE);
                Picasso.get().load(currPost.getPostImage())
                        .placeholder(R.drawable.image_placeholder).error(R.drawable.image_placeholder)
                        .into(postImage);
            }
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        @NonNull
        @NotNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.rant_post, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, int position) {
            Post currPost = profileViewModel.getMyPosts().getValue().get(position);
            holder.bind(currPost);
        }

        @Override
        public int getItemCount() {
            if (profileViewModel.getMyPosts().getValue() != null) {
                return profileViewModel.getMyPosts().getValue().size();
            } else {
                return 0;
            }

        }
    }
}