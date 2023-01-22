package com.example.mingle.Adapter;

import android.app.DownloadManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mingle.Model.Notifications;
import com.example.mingle.Model.User;
import com.example.mingle.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

//public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{
//
//    private Context mContext;
//    private List<Notifications> mNotifications;
//
//    public NotificationAdapter(Context mContext, List<Notifications> mNotifications) {
//        this.mContext = mContext;
//        this.mNotifications = mNotifications;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false);
//
//        return new NotificationAdapter.ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//
//        Notifications notifications = mNotifications.get(position);
//
//        getUser(holder.imageProfile,holder.username, notifications.getUserId());
//
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
//    }
//
//    private void getUser(ImageView imageView, TextView textView, String userId) {
//
//        FirebaseDatabase.getInstance().getReference().child("Users").child(userId)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        User user = snapshot.getValue(User.class);
//                        if(user.getImageUrl().equals("default")){
//                            imageView.setImageResource(R.mipmap.ic_launcher_launch);
//                        } else {
//                            Picasso.get().load(user.getImageUrl()).into(imageView);
//                        }
//                        textView.setText(user.getUserName());
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//    }
//
//    @Override
//    public int getItemCount() {
//        return mNotifications.size();
//    }
//
//    public class ViewHolder extends RecyclerView.ViewHolder {
//
//        public ImageView imageProfile;
//        public TextView username;
//        public TextView comment;
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            imageProfile = itemView.findViewById(R.id.image_profile);
//            username = itemView.findViewById(R.id.username);
//            comment = itemView.findViewById(R.id.comment);
//        }
//    }
//}
