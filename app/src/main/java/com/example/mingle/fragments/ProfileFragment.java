package com.example.mingle.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mingle.Adapter.PhotoAdapter;
import com.example.mingle.EditProfile;
import com.example.mingle.FollowersActivity;
import com.example.mingle.Model.Post;
import com.example.mingle.Model.User;
import com.example.mingle.PostActivity;
import com.example.mingle.R;
import com.example.mingle.StartActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private List<Post> myPosts;

    private CircleImageView image_profile;
    private Button logOut;
    private TextView posts;
    private TextView following;
    private TextView followers;
    private TextView fullName;
    private TextView bio;
    private TextView userName;
    private Button editProfile;

    private ImageView my_pictures;

    private FirebaseUser fUser;

    String profileId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        String data = getContext().getSharedPreferences("Profile", Context.MODE_PRIVATE).getString("profileId","none");

        if(data.equals("none")){
            profileId = fUser.getUid();
        } else {
            profileId = data;
        }

        image_profile = view.findViewById(R.id.image_profile);
        posts = view.findViewById(R.id.posts);
        logOut = view.findViewById(R.id.log_out);
        followers = view.findViewById(R.id.follower_count);
        following = view.findViewById(R.id.following_count);
        fullName = view.findViewById(R.id.fullName);
        userName = view.findViewById(R.id.username);
        bio = view.findViewById(R.id.bio);
        my_pictures = view.findViewById(R.id.my_pictures);
        editProfile = view.findViewById(R.id.edit_profile);

        myPhotos();

        recyclerView = view.findViewById(R.id.recycler_view_pictures);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        myPosts = new ArrayList<>();
        photoAdapter = new PhotoAdapter(getContext(), myPosts);
        recyclerView.setAdapter(photoAdapter);


        userInfo();

        getFollowersAndFollowingCount();

        getPostCount();

        if(profileId != fUser.getUid()){
            logOut.setVisibility(View.GONE);
        }

        if (profileId.equals(fUser.getUid())){
            editProfile.setText("Edit Profile");
        } else {
            followingStatus();
        }

        editProfile.setOnClickListener(view1 -> {
            String btnText = editProfile.getText().toString();
            if(btnText.equals("Edit Profile")){
                startActivity(new Intent(getContext(), EditProfile.class));
            } else {
                if (btnText.equals("Follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid())
                            .child("Following").child(profileId).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId)
                            .child("Followers").child(fUser.getUid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid())
                            .child("Following").child(profileId).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId)
                            .child("Followers").child(fUser.getUid()).removeValue();
                }
            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getContext(), "Log Out Success!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(), StartActivity.class));
            }
        });

        return view;

    }

    private void myPhotos() {
        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myPosts.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    Post post = snapshot1.getValue(Post.class);

                    if (post.getPublisher().equals(profileId)){
                        myPosts.add(post);
                    }
                }

                Collections.reverse(myPosts);
                photoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void followingStatus() {
        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid()).child("Following")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(profileId).exists()){
                            editProfile.setText("Following");
                        } else {
                            editProfile.setText("Follow");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getPostCount() {
        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int counter = 0;
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    Post post = snapshot1.getValue(Post.class);

                    if (post.getPublisher().equals(profileId)) counter ++;
                }

                posts.setText(String.valueOf(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowersAndFollowingCount() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId);

        ref.child("Followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ref.child("Following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following.setText(""+ snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void userInfo() {

        FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);

                        Picasso.get().load(user.getImageUrl()).into(image_profile);
                        userName.setText(user.getUserName());
                        fullName.setText(user.getName());
                        bio.setText(user.getBio());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}