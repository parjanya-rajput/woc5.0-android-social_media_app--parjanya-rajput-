package com.example.mingle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;

public class LoginActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;
    private TextView registerUser;
    private Button login;
    private Button passwordToggle;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        registerUser = findViewById(R.id.register);
        passwordToggle = findViewById(R.id.password_visibility);
        passwordToggle.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        getSupportActionBar().setTitle("MinioWitter");

        password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordToggle.setVisibility(View.VISIBLE);
            }
        });

        registerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,SignupActivty.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();

                if(TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
                    Toast.makeText(LoginActivity.this, "All fields are mandatory!", Toast.LENGTH_SHORT).show();
                } else {
                    logInUser(txt_email, txt_password);
                }
            }
        });

        passwordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (passwordToggle.getText().toString().equals("SHOW")){
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordToggle.setText("HIDE");
                } else {
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordToggle.setText("SHOW");
                }
            }
        });

    }

    private void logInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Log.w("logInWithEmail:success","Successful Login");
                    Toast.makeText(LoginActivity.this,"Successful Login!",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this , MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}