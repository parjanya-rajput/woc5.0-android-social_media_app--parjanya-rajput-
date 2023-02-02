package com.example.mingle;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;
import java.util.Objects;

public class SignupActivty extends AppCompatActivity {

    private EditText username;
    private EditText name;
    private EditText email;
    private EditText password;
    private EditText repass;
    private Button register;
    private TextView loginUser;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_activty);

        username = findViewById(R.id.username);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        repass = findViewById(R.id.repassword);
        register = findViewById(R.id.register);
        loginUser = findViewById(R.id.login_user);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        pd = new ProgressDialog(this);

        getSupportActionBar().setTitle("MinioWitter");

        loginUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignupActivty.this, LoginActivity.class ));
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txtUsername = username.getText().toString();
                String txtName = name.getText().toString();
                String txtEmail = email.getText().toString();
                String txtPassword = password.getText().toString();
                String txtRePassword = repass.getText().toString();

                if(TextUtils.isEmpty(txtUsername) || TextUtils.isEmpty(txtName)
                        || TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword) || TextUtils.isEmpty(txtRePassword)) {
                    Toast.makeText(SignupActivty.this, "All Fields are Mandatory!", Toast.LENGTH_SHORT).show();
                } else if(txtPassword.length() < 8) {
                    Toast.makeText(SignupActivty.this, "Password too short!", Toast.LENGTH_SHORT).show();
                } else if(!txtPassword.equals(txtRePassword)) {
                    Toast.makeText(SignupActivty.this, "Please Re-enter the correct password!", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(txtUsername, txtName, txtEmail, txtPassword);
                }
            }
        });
    }

    private void registerUser(String username, String name, String email, String password) {
        pd.setMessage("Please Wait for few seconds!");
        pd.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("Name", name);
                        map.put("UserName", username);
                        map.put("Email", email);
                        map.put("Id", Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
                        map.put("bio","");
                        map.put("imageUrl","default");

                        mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(map).addOnCompleteListener((Task<Void> task) -> {
                            if (task.isSuccessful()) {
                                pd.dismiss();
                                FirebaseUser user = mAuth.getCurrentUser();
                                Log.w("signInwithEmail:success","Successful signin");
                                Toast.makeText(SignupActivty.this, "Update the profile for better experience",
                                        Toast.LENGTH_LONG).show();
                                SignupActivty.this.updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(SignupActivty.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            updateUI(null);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(SignupActivty.this,e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        Intent intent = new Intent(SignupActivty.this , MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void reload() {
        Toast.makeText(SignupActivty.this,"User Already Registered!",Toast.LENGTH_SHORT).show();
        startActivity(new Intent(SignupActivty.this, MainActivity.class).addFlags
                (Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
}
