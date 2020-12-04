package com.mobapps.covidcontacttracer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/*
Purpose of this file:
- This displays the main welcome page for the logged in user
 */
//Added One Line of Code..
public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView WelcomeText;
    private TextView statusTextView;
    private Button updateStatusButton;
    public static boolean isPositive; // Stores whether the user is +ve or -ve in Boolean Format
    private FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean isFirstRun = true;
    private ProgressBar progressBar;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        WelcomeText = (TextView) findViewById(R.id.WelcomeText);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            String name = user.getDisplayName();

            WelcomeText.setText("Welcome, "+name);
        } else {
            WelcomeText.setText("Some error took Place!");

        }
        auth=FirebaseAuth.getInstance();
        statusTextView=findViewById( R.id.statusTextView );
        updateStatusButton=findViewById( R.id.updateStatusButton );
        updateStatusButton.setOnClickListener( this );

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        statusTextView.setVisibility(View.GONE);
        updateStatusButton.setVisibility(View.GONE);
        CheckAndUpdateStatus();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.menu,menu );
        return super.onCreateOptionsMenu( menu );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                Toast.makeText( this, "User signed out successfully!", Toast.LENGTH_SHORT ).show();
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                Intent serviceIntent = new Intent(getApplicationContext(), MyService.class);
                stopService(serviceIntent);
                Intent serviceIntent2 = new Intent(getApplicationContext(), ContactTracingService.class);
                stopService(serviceIntent2);
                break;
            case R.id.view_map:
                intent=new Intent(getApplicationContext(),MapsActivity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected( item );
    }

    public void confirmDialog(View view){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Confirm status update");
        alertDialogBuilder.setMessage("Are you sure you want to update your status?");
                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                if(isPositive)
                                {
                                    Toast.makeText( ProfileActivity.this, "Status Updated to : Negative", Toast.LENGTH_SHORT ).show();
                                    db.collection("users").document(auth.getCurrentUser().getUid()).update("Status","Negative");

                                }
                                else
                                {

                                    Toast.makeText( ProfileActivity.this, "Status Updated to : Positive", Toast.LENGTH_SHORT ).show();
                                    db.collection("users").document(auth.getCurrentUser().getUid()).update("Status","Positive");

                                }
                                progressBar.setVisibility(View.VISIBLE);
                                statusTextView.setVisibility(View.GONE);
                                updateStatusButton.setVisibility(View.GONE);
                                CheckAndUpdateStatus();
                            }
                        });

        alertDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do nothing
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.updateStatusButton:
                confirmDialog(view);

        }
    }

    private void CheckAndUpdateStatus() {
        try {
            db.collection("users").document(auth.getCurrentUser().getUid()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d("YS", (String) document.get("Status"));
                                    String status = (String) document.get("Status");
                                    if (status.equals("Negative")) {
                                        isPositive = false;
                                    } else {
                                        isPositive = true;
                                    }
                                    progressBar.setVisibility(View.GONE);
                                    statusTextView.setVisibility(View.VISIBLE);
                                    updateStatusButton.setVisibility(View.VISIBLE);
                                    UpdateUi();
                                    if (isFirstRun) {
                                        Intent serviceIntent = new Intent(ProfileActivity.this, MyService.class);
                                        ContextCompat.startForegroundService(ProfileActivity.this, serviceIntent);

                                        Intent serviceIntent2 = new Intent(ProfileActivity.this, ContactTracingService.class);
                                        ContextCompat.startForegroundService(ProfileActivity.this, serviceIntent2);
                                        isFirstRun = false;
                                    }
                                }
                            } else {
                                //Log.d("YS", "User signed out");
                            }
                        }
                    });
        } catch (NullPointerException e) {
            //Catch the case when user is signed out
            Log.d("YS", "User signed out");
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            Intent serviceIntent = new Intent(getApplicationContext(), MyService.class);
            stopService(serviceIntent);
            Intent serviceIntent2 = new Intent(getApplicationContext(), ContactTracingService.class);
            stopService(serviceIntent2);
        }
    }

    private void UpdateUi()
    {
        if(isPositive)
        {
            updateStatusButton.setText( "UPDATE STATUS TO NEGATIVE" );
            statusTextView.setText( "POSITIVE" );
            statusTextView.setTextColor((getResources().getColor(R.color.negativeGreen)));
        }
        else
        {
            updateStatusButton.setText( "UPDATE STATUS TO POSITIVE" );
            statusTextView.setText( "NEGATIVE" );
            statusTextView.setTextColor((getResources().getColor(R.color.positiveRed)));
        }
    }

}