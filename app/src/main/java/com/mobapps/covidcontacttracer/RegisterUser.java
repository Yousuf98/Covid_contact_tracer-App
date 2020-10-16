package com.mobapps.covidcontacttracer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUser extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText editTextTextPersonName;
    private EditText editTextTextEmailAddress2;
    private EditText editTextTextPassword2;
    private EditText editTextAge;
    private EditText editTextPhone;
    private ProgressBar progressBarReg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        editTextTextPersonName = (EditText) findViewById(R.id.editTextTextPersonName);
        editTextTextEmailAddress2 = (EditText) findViewById(R.id.editTextTextEmailAddress2);
        editTextTextPassword2 = (EditText) findViewById(R.id.editTextTextPassword2);
        editTextAge = (EditText) findViewById(R.id.editTextAge);
        editTextPhone = (EditText) findViewById(R.id.editTextPhone);
        progressBarReg = (ProgressBar) findViewById(R.id.progressBarReg);
    }

    public void registerUser(android.view.View v) {
        final String name = editTextTextPersonName.getText().toString().trim();
        final String email = editTextTextEmailAddress2.getText().toString().trim();
        String pass = editTextTextPassword2.getText().toString().trim();
        final String age = editTextAge.getText().toString().trim();
        final String phone = editTextPhone.getText().toString().trim();

        if (name.isEmpty()) {
            editTextTextPersonName.setError("Full name is required!");
            editTextTextPersonName.requestFocus();
            return;
        }
        if (age.isEmpty()) {
            editTextAge.setError("Age is required!");
            editTextAge.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            editTextTextEmailAddress2.setError("Email is required!");
            editTextTextEmailAddress2.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextTextEmailAddress2.setError("Please provide valid email!");
            editTextTextEmailAddress2.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            editTextPhone.setError("Phone number is required!");
            editTextPhone.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            editTextTextPassword2.setError("Password is required!");
            editTextTextPassword2.requestFocus();
            return;
        }
        if (pass.length() < 6) {
            editTextTextPassword2.setError("Min password length should be 6 characters!");
            editTextTextPassword2.requestFocus();
            return;
        }
        progressBarReg.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            FirebaseUser AddedUser = FirebaseAuth.getInstance().getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                            AddedUser.updateProfile(profileUpdates);
                            User user = new User(name, email, age, phone);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterUser.this, "User has been registered successfully!", Toast.LENGTH_LONG).show();
                                        progressBarReg.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(RegisterUser.this, "Failed to register. Try again!", Toast.LENGTH_LONG).show();
                                        progressBarReg.setVisibility(View.GONE);
                                    }
                                }
                            });
                        } else {
                            Log.d("debuggg", "This fail?");
                            Toast.makeText(RegisterUser.this, "Failed to register. Try again!", Toast.LENGTH_LONG).show();
                            progressBarReg.setVisibility(View.GONE);
                        }
                    }
                });
    }

}