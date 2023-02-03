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
import android.util.Base64;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private ImageView close;
    private ImageView imageAdded;
    private TextView post;
    private EditText description;
    private Button selectImage;
    private Button takeImage;
    private Uri imageUri;
    private String imageUrl;
    private int flag = 0;
    private int SELECT_PICTURE = 100;
    private int CAPTURE_IMAGE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        imageAdded = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);
        selectImage = findViewById(R.id.select_image_gallery);
        takeImage = findViewById(R.id.select_image_camera);

        getSupportActionBar().setTitle("MinioWitter");

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PostActivity.this, MainActivity.class));
                finish();
            }
        });
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
                if (imageUri != null) {
                    imageAdded.setVisibility(View.VISIBLE);
                    description.setVisibility(View.VISIBLE);
                    selectImage.setVisibility(View.GONE);
                    takeImage.setVisibility(View.GONE);
                }
            }
        });

        takeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageCapture();
                imageAdded.setVisibility(View.VISIBLE);
                description.setVisibility(View.VISIBLE);
                selectImage.setVisibility(View.GONE);
                takeImage.setVisibility(View.GONE);
            }
        });
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag == 1) {
                    upload2();
                } else {
                    upload();
                }
            }
        });
    }


    private void imageCapture() {
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(camera_intent, CAPTURE_IMAGE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // BitMap is data structure of image file which store the image in memory
        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                imageUri = data.getData();
                if (null != imageUri) {
                    // update the preview image in the layout
                    imageAdded.setImageURI(imageUri);
                    imageAdded.setScaleType(ImageView.ScaleType.FIT_XY);
                }
            } else if(requestCode == CAPTURE_IMAGE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imageAdded.setScaleType(ImageView.ScaleType.FIT_XY);
                // Set the image in imageview for display
                imageAdded.setImageBitmap(photo);
                firebaseUploadBitmap(photo);
                flag = 1;
            }
        }
    }

    private void firebaseUploadBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] data = stream.toByteArray();
        StorageReference filePath = FirebaseStorage.getInstance().getReference("Posts").child(System.currentTimeMillis() + ".png");
        Task<Uri> urlTask = filePath.putBytes(data).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            // Continue with the task to get the download URL
            return filePath.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                imageUrl = downloadUri.toString();
            } else {
                Toast.makeText(this,task.getException().toString(),Toast.LENGTH_SHORT);
            }
        });

    }

    private void upload() {

        if (imageUri != null) {
            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Uploading...");
            pd.show();
            StorageReference filePath = FirebaseStorage.getInstance().getReference("Posts").child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            StorageTask uploadtask = filePath.putFile(imageUri);
            uploadtask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Uri downloadUri = task.getResult();
                    imageUrl = downloadUri.toString();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
                    String postId = ref.push().getKey();

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("postId", postId);
                    map.put("imageUrl", imageUrl);
                    map.put("description", description.getText().toString());
                    map.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());

                    ref.child(postId).setValue(map);

                    pd.dismiss();
                    startActivity(new Intent(PostActivity.this, MainActivity.class));
                    Toast.makeText(PostActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                    finish();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(PostActivity.this, "No Image was Selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));
    }

    private void upload2() {

        if (imageUrl != null) {
            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Uploading...");
            pd.show();

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
            String postId = ref.push().getKey();
            HashMap<String, Object> map = new HashMap<>();
            map.put("postId", postId);
            map.put("imageUrl", imageUrl);
            map.put("description", description.getText().toString());
            map.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());

            ref.child(postId).setValue(map);
            pd.dismiss();
            startActivity(new Intent(PostActivity.this, MainActivity.class));
            Toast.makeText(PostActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Something went Wrong! Please try again..", Toast.LENGTH_SHORT).show();
        }
    }


    private void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }
}
