package com.mobapps.covidcontacttracer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private EditText editTextTextEmailAddress, editTextTextPassword;

    private FirebaseAuth mAuth;
    private ProgressBar progressBarLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextTextEmailAddress = (EditText) findViewById(R.id.editTextTextEmailAddress);
        editTextTextPassword = (EditText) findViewById(R.id.editTextTextPassword);
        progressBarLogin = (ProgressBar) findViewById(R.id.progressBarLogin);

        mAuth = FirebaseAuth.getInstance();
    }

    public void registerActivity(android.view.View v) {
        Intent reg = new Intent(this, RegisterUser.class);
        startActivity(reg);
    }

    public void signIn(android.view.View v) {
        String email = editTextTextEmailAddress.getText().toString().trim();
        String pass = editTextTextPassword.getText().toString().trim();
        if (email.isEmpty()) {
            editTextTextEmailAddress.setError("Email is required!");
            editTextTextEmailAddress.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextTextEmailAddress.setError("Please provide valid email!");
            editTextTextEmailAddress.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            editTextTextPassword.setError("Password is required!");
            editTextTextPassword.requestFocus();
            return;
        }
        if (pass.length() < 6) {
            editTextTextPassword.setError("Min password length should be 6 characters!");
            editTextTextPassword.requestFocus();
            return;
        }

        progressBarLogin.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //Redirect to user profile
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    progressBarLogin.setVisibility(View.GONE);
                } else {
                    Toast.makeText(MainActivity.this, "Failed to login! Please check your credentials", Toast.LENGTH_LONG).show();
                    progressBarLogin.setVisibility(View.GONE);
                }
            }
        });


    }
}