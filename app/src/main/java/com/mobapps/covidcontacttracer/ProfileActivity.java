package com.mobapps.covidcontacttracer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
                break;
            case R.id.view_map:
                intent=new Intent(getApplicationContext(),MapsActivity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.updateStatusButton:

                if(isPositive)
                {
                    Toast.makeText( this, "Status Updated to : Positive", Toast.LENGTH_SHORT ).show();
                    db.collection("users").document(auth.getCurrentUser().getUid()).update("Status","Negative");

                }
                else
                {

                    Toast.makeText( this, "Status Updated to : Negative", Toast.LENGTH_SHORT ).show();
                    db.collection("users").document(auth.getCurrentUser().getUid()).update("Status","Positive");

                }
                CheckAndUpdateStatus();

        }
    }

    private void CheckAndUpdateStatus() {
        db.collection("users").document(auth.getCurrentUser().getUid()).get()
                .addOnCompleteListener( new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()){
                                Log.d("YS", (String) document.get("Status"));
                                String status = (String) document.get("Status");
                                if (status.equals("Negative")){
                                    isPositive = false;
                                }
                                else {
                                    isPositive = true;
                                }

                                UpdateUi();
                            }
                        }
                    }
                } );
    }

    private void UpdateUi()
    {
        if(isPositive)
        {
            updateStatusButton.setText( "UPDATE STATUS TO NEGATIVE" );
            statusTextView.setText( "POSITIVE" );
        }
        else
        {
            updateStatusButton.setText( "UPDATE STATUS TO POSITIVE" );
            statusTextView.setText( "NEGATIVE" );
        }
    }
}