package com.mobapps.covidcontacttracer;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static List<Object> listOfUsersDisplayed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    public void getMarkerCoordinates()
    {
            db.collection("LocationStamps").whereEqualTo( "Status","Positive" )
            .get().addOnCompleteListener( new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for(QueryDocumentSnapshot document:task.getResult())
                    {
                        Log.d("CHECK", "The ID Encountered is "+document.get("Uid"));
                        if(!listOfUsersDisplayed.contains( (String)document.get("Uid")))
                        {

                            LatLng newSet=new LatLng( Double.parseDouble( (String)document.get("Lattitude") ), Double.parseDouble( (String)document.get("Lattitude") ) );
                            Toast.makeText( MapsActivity.this, "The UID is "+document.get("Uid"), Toast.LENGTH_SHORT ).show();
                            mMap.addMarker(new MarkerOptions()
                                    .position(newSet)
                                    .title("Positive Profile"));
                            listOfUsersDisplayed.add(document.get("Uid"));
                        }
                    }
                }
                else
                {
                    Toast.makeText( MapsActivity.this, "Error extracting coordinates!", Toast.LENGTH_SHORT ).show();
                }
            }
        } );

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Setting the zoom controls for the user
        mMap.getUiSettings().setZoomControlsEnabled( true );

        listOfUsersDisplayed=new ArrayList<>();
        getMarkerCoordinates();


        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }
}