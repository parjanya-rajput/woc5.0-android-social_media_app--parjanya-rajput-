package com.example.mingle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mingle.Model.User;
import com.example.mingle.fragments.ProfileFragment;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import io.grpc.Context;

public class EditProfile extends AppCompatActivity {

    private ImageView close;
    private CircleImageView imageProfile;
    private TextView save;
    private TextView changePhoto;
    private EditText fullName;
    private EditText userName;
    private EditText bio;

    private Uri imageUri;
    private StorageTask uploadTask;
    private StorageReference storageRef;

    int SELECT_PICTURE = 200;


    FirebaseUser fUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        close = findViewById(R.id.close);
        imageProfile = findViewById(R.id.image_profile);
        save = findViewById(R.id.save);
        changePhoto = findViewById(R.id.change_photo);
        fullName = findViewById(R.id.fullName);
        userName = findViewById(R.id.username);
        bio = findViewById(R.id.bio);

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        storageRef = FirebaseStorage.getInstance().getReference().child("Uploads");

        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        fullName.setText(user.getName());
                        userName.setText(user.getUserName());
                        bio.setText(user.getBio());

                        Picasso.get().load(user.getImageUrl()).into(imageProfile);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });

        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
                startActivity(new Intent(EditProfile.this, ProfileFragment.class));
            }
        });

    }

    private void updateProfile() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("Name", fullName.getText().toString());
        map.put("UserName", userName.getText().toString());
        map.put("bio", bio.getText().toString());

        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid())
                .updateChildren(map);
    }


    private void imageChooser()
    {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(i, "Select Picture"),SELECT_PICTURE);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null){
            if (data.getData() != null){
                imageUri = data.getData();
                ProgressDialog pd = new ProgressDialog(this);
                pd.setMessage("Uploading pfp....");
                pd.show();

                StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpeg");

                uploadTask = fileRef.putFile(imageUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }

                        return fileRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            String url = downloadUri.toString();

                            FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid())
                                    .child("imageUrl").setValue(url);

                            pd.dismiss();
                            Toast.makeText(EditProfile.this,"Upload Success!",Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(EditProfile.this,"Upload Failed!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
            Toast.makeText(EditProfile.this, uploadTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}