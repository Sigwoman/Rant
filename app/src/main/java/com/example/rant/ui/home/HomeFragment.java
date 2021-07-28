package com.example.rant.ui.home;

import android.view.MenuInflater;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.NavDirections;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
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

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;

    PostsAdapter adapter;

    SwipeRefreshLayout swipeRefresh;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Get root view
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Get posts list
        RecyclerView recycler = rootView.findViewById(R.id.home_recycler);
        recycler.setHasFixedSize(true);

        // Connect posts list to manager
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this.getContext());
        recycler.setLayoutManager(manager);

        // Get posts data
        homeViewModel.getAllPosts().observe(getViewLifecycleOwner(), data -> adapter.notifyDataSetChanged());

        // Set adapter to the list
        adapter = new PostsAdapter();
        recycler.setAdapter(adapter);

        // Manage like click
        adapter.setOnLikeClickListener(position -> {
            like(position);
        });

        // Manage add post button
        FloatingActionButton addBtn = rootView.findViewById(R.id.addPost_btn);

        NavDirections addPostAction = HomeFragmentDirections.actionGlobalNewPostFragment("", "", "", 0, false);

        addBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(addPostAction));

        // Handle refresh
        setupProgressListener();

        swipeRefresh = rootView.findViewById(R.id.home_swipeRefresh);
        swipeRefresh.setOnRefreshListener(() -> {
            homeViewModel.refresh();
        });

        return rootView;
    }

    private void like(int position) {
        Post currPost = homeViewModel.getAllPosts().getValue().get(position);

        currPost.like();

        Model.instance.savePost(currPost, () -> {
            adapter.notifyDataSetChanged();
        });
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

    @Override
    public void onResume() {
        super.onResume();

        BottomNavigationView navBar = getActivity().findViewById(R.id.nav_view);
        navBar.setVisibility(View.VISIBLE);
    }

    public interface OnLikeClickListener {
        void onClick(int position);
    }

    static class PostsViewHolder extends RecyclerView.ViewHolder {
        OnLikeClickListener likeListener;

        Post post;
        Button optionsBtn;
        TextView username;
        TextView description;
        ImageView userImage;
        ImageView postImage;
        TextView likesCount;
        ImageView likeBtn;

        public PostsViewHolder(@NonNull @NotNull View itemView, OnLikeClickListener likeListener) {
            super(itemView);

            optionsBtn = itemView.findViewById(R.id.post_optionsBtn);
            username = itemView.findViewById(R.id.profile_username);
            description = itemView.findViewById(R.id.post_description);
            userImage = itemView.findViewById(R.id.post_userImage);
            postImage = itemView.findViewById(R.id.post_postImage);
            likesCount = itemView.findViewById(R.id.post_likes_count);
            likeBtn = itemView.findViewById(R.id.post_like_btn);

            // Handle like
            this.likeListener = likeListener;

            likeBtn.setOnClickListener(view -> {
                if (likeListener != null){
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION){
                        likeListener.onClick(position);
                    }
                }
            });
        }

        public void bind(Post currPost) {
            post = currPost;
            username.setText(currPost.getUsername());
            description.setText(currPost.getDescription());
            likesCount.setText(Integer.toString(currPost.getLikes()));
//            likesCount.setText(String.format ("%d", currPost.getLikes()));

            // Handle options button
            String currentUserName = ModelFirebase.getAuthUserName();

            if (currPost.getUsername().equals(currentUserName)) {
                optionsBtn.setVisibility(View.VISIBLE);

                optionsBtn.setOnClickListener(view -> {
                    showMenu(view);
                });
            }

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

        private void showMenu(View view) {
            PopupMenu popup = new PopupMenu(optionsBtn.getContext(), view);

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.optionMenu_edit:
                        NavDirections addPostAction =
                                HomeFragmentDirections.actionGlobalNewPostFragment(
                                        post.getDescription(), post.getPostImage(),
                                        post.getId(), post.getLikes(), post.getDeleted()
                                );
                        Navigation.findNavController(itemView).navigate(addPostAction);

                        return true;
                    case R.id.optionMenu_delete:
                        Model.instance.delete(post, () -> {});
                        return true;
                    default:
                        return false;
                }
            });
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.options_menu, popup.getMenu());

            popup.show();
        }
    }

    class PostsAdapter extends RecyclerView.Adapter<PostsViewHolder> {
        OnLikeClickListener likeListener;

        @NonNull
        @NotNull
        @Override
        public PostsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.rant_post, parent, false);
            return new PostsViewHolder(view, likeListener);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull PostsViewHolder holder, int position) {
            Post currPost = homeViewModel.getAllPosts().getValue().get(position);
            holder.bind(currPost);
        }

        @Override
        public int getItemCount() {
            if (homeViewModel.getAllPosts().getValue() != null) {
                return homeViewModel.getAllPosts().getValue().size();
            } else {
                return 0;
            }
        }

        public void setOnLikeClickListener(OnLikeClickListener listener) {
            this.likeListener = listener;
        }
    }
}