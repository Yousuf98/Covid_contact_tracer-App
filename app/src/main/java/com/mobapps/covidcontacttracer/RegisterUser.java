package com.mobapps.covidcontacttracer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class RegisterUser extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText editTextTextPersonName;
    private EditText editTextTextEmailAddress2;
    private EditText editTextTextPassword2;
    private EditText editTextAge;
    private EditText editTextPhone;
    private ProgressBar progressBarReg;
    private EditText editTextResidenceCity;
    private Spinner GenderSpinner;
    private RadioGroup StatusRadioGroup;
    private RadioButton radioButtonNve;
    private RadioButton radioButtonPve;
    private String Status = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checking if the user is already logged in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }

        setContentView(R.layout.activity_register_user);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        editTextTextPersonName = (EditText) findViewById(R.id.editTextTextPersonName);
        editTextTextEmailAddress2 = (EditText) findViewById(R.id.editTextTextEmailAddress2);
        editTextTextPassword2 = (EditText) findViewById(R.id.editTextTextPassword2);
        editTextAge = (EditText) findViewById(R.id.editTextAge);
        editTextPhone = (EditText) findViewById(R.id.editTextPhone);
        progressBarReg = (ProgressBar) findViewById(R.id.progressBarReg);
        editTextResidenceCity = (EditText) findViewById(R.id.editTextResidenceCity);
        GenderSpinner = (Spinner) findViewById(R.id.GenderSpinner);
        StatusRadioGroup = (RadioGroup) findViewById(R.id.StatusRadioGroup);
        radioButtonNve = (RadioButton) findViewById(R.id.radioButtonNve);
        radioButtonPve = (RadioButton) findViewById(R.id.radioButtonPve);
        radioButtonNve.setChecked(true);
        //Setting the spinner to M/F Option:
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_dropdown_item);
        GenderSpinner.setAdapter(adapter);

        //Setting Radio Button Listener:
        StatusRadioGroup.setOnCheckedChangeListener(this);

        //--remove the code
        getDatabaseRecordsStatus();
    }

    //remove the code
    public void getDatabaseRecordsStatus()
    {
        db.collection("users").get()
                .addOnCompleteListener( new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            for(QueryDocumentSnapshot document:task.getResult())
                            {
                                Log.d("database", "The status is" +document.get( "Status" ));
                            }
                        }
                    }
                } );
    }

    public void registerUser(android.view.View v) {
        final String name = editTextTextPersonName.getText().toString().trim();
        final String email = editTextTextEmailAddress2.getText().toString().trim();
        String pass = editTextTextPassword2.getText().toString().trim();
        final String age = editTextAge.getText().toString().trim();
        final String phone = editTextPhone.getText().toString().trim();
        final String city_of_residence = editTextResidenceCity.getText().toString().trim();
        final String gender = (String) GenderSpinner.getSelectedItem();

        //Get status
        int selectedId = StatusRadioGroup.getCheckedRadioButtonId();

        // find the radiobutton by returned id
        RadioButton radioButton = (RadioButton) findViewById(selectedId);
        Status = radioButton.getText().toString().trim();

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

        if (city_of_residence.isEmpty()) {
            editTextResidenceCity.setText("The City of Residence is required!");
            editTextResidenceCity.requestFocus();
        }

        progressBarReg.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, pass) // Creating a new user using this function
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser AddedUser = FirebaseAuth.getInstance().getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                            AddedUser.updateProfile(profileUpdates);
                            User user = new User(name, email, age, phone, city_of_residence, gender, Status); // Creating a new user object
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterUser.this, "User has been registered successfully!", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(RegisterUser.this, "Failed to register. Try again!", Toast.LENGTH_LONG).show();
                                    }
                                    progressBarReg.setVisibility(View.GONE);
                                    sendEmailVerification();
                                }
                            });

                            //Attempt to add user to cloud firestore
                            // Create a new user with a first and last name
                            /*Map<String, Object> user = new HashMap<>();
                            user.put("first", "Ada");
                            user.put("last", "Lovelace");
                            user.put("born", 1815);*/
                            // Add a new document with a generated ID
                            db.collection("users")
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .set(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d("debuggg", "Document added to firestore successfully");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("debuggg", "Error adding document", e);
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

    public void sendEmailVerification() {
        FirebaseAuth auth = FirebaseAuth.getInstance(); // Sending Email Verification to the User
        FirebaseUser user = auth.getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("debuggg", "Email has been sent to the user");
                        } else {
                            Log.d("debuggg", "The email was not sent succesfully");
                        }
                    }
                });
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i) {
            case R.id.radioButtonPve:
                Status = "Positive";
                break;
            case R.id.radioButtonNve:
                Status = "Negative";
                break;
        }
    }
}