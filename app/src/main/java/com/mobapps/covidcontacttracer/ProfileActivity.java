package com.mobapps.covidcontacttracer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/*
Purpose of this file:
- This displays the main welcome page for the logged in user
 */

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView WelcomeText;
    private TextView statusTextView;
    private Button updateStatusButton;
    private String ExtractedStatus;
    private boolean PositiveOrNegative; // True if Positive
    FirebaseAuth auth;
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
        UpdateUi();

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
                startActivity( intent );

        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.updateStatusButton:

                if(PositiveOrNegative)
                {
                    FirebaseDatabase.getInstance().getReference("Users").child(auth.getCurrentUser().getUid()).child( "Status" )
                            .setValue( "Negative" );
                    Toast.makeText( this, "Status Updated to : Negative", Toast.LENGTH_SHORT ).show();
                }
                else
                {
                    FirebaseDatabase.getInstance().getReference("Users").child(auth.getCurrentUser().getUid()).child( "Status" )
                            .setValue( "Positive" );
                    Toast.makeText( this, "Status Updated to : Positive", Toast.LENGTH_SHORT ).show();
                }
                CheckAndUpdateStatus();
                UpdateUi();
        }
    }

    private void CheckAndUpdateStatus() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users").child( auth.getCurrentUser().getUid()).child("Status");
        ref.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ExtractedStatus=(String)dataSnapshot.getValue();
                if(ExtractedStatus.equals("Positive"))
                {
                    Toast.makeText( ProfileActivity.this, "POSITIVE CHECKED!", Toast.LENGTH_SHORT ).show();
                    PositiveOrNegative=true;
                }
                else
                {
                    Toast.makeText( ProfileActivity.this, "NEGATIVE CHECKED!", Toast.LENGTH_SHORT ).show();
                    PositiveOrNegative=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                    // No Code.
            }
        } );
    }

    private void UpdateUi()
    {
        if(PositiveOrNegative)// If Positive
        {
            updateStatusButton.setText( "UPDATE STATUS TO POSITIVE" );
            statusTextView.setText( "NEGATIVE" );
        }
        else
        {
            updateStatusButton.setText( "UPDATE STATUS TO NEGATIVE" );
            statusTextView.setText( "POSITIVE" );
        }
    }
}