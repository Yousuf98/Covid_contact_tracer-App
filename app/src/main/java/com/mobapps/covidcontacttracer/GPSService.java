package com.mobapps.covidcontacttracer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GPSService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleApiClient googleApiClient;
    public static final int UPDATE_INTERVAL = 20000;   // 5 secs
    public static final int FASTEST_UPDATE_INTERVAL = 10000;
    private LocationRequest locationRequest;
    private Timer timer;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    private FirebaseAuth auth;
    public static String me = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL);
        googleApiClient.connect();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public int onStartCommand(Intent intent, int flags, int startId) {

        //TS: display a notification here to inform the use of the foreground service
        //This is a must in foreground services
        //This notification cannot be cleared by the user until the service is killed
        //
        //Must add the following persmission to manifest as well
        //<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
        //
        auth=FirebaseAuth.getInstance();
        me = auth.getCurrentUser().getUid();
        //generateNotification("");

        //TS: when the system will try to re-create the service
        //onStartCommand will be called again (not onCreate)
        //So called the startTimer() here
        generateNotification("Location data being uploaded");
        startTimer();

        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void generateNotification(String s) {
        createNotificationChannel();
        Intent notificationIntent;
        if (s.equalsIgnoreCase("Location data being uploaded")){
            notificationIntent = new Intent(this, MainActivity.class);
        } else {
            notificationIntent = new Intent(this, MapsActivity.class);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Contact Tracer Running")
                .setContentText(s)
                .setSmallIcon(R.drawable.app_bg)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification); //TS: a different function to show the notification
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //----------------------------------------------------------------------------------------------
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    //----------------------------------------------------------------------------------------------
    private void startTimer() {
        TimerTask task = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                Log.d("CMP354", "Timer task is running (Checking if I am on message list): " + new Date(System.currentTimeMillis()));
                final String me = auth.getCurrentUser().getUid();
                db.collection("Message List")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d("TAG", document.getId() + " => " + document.getData());
                                        if (document.get("UserID").equals(me) && !ProfileActivity.isPositive){
                                            generateNotification("Alert! You may have been exposed to covid virus. Please get tested!");
                                        }

                                    }
                                } else {
                                    Log.d("TAG", "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        };

        timer = new Timer(true);
        timer.schedule(task, 0, 3000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void onDestroy() {
        Log.d("CMP354", "Service destroyed, and timer stopped !");
        stopTimer();
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Put code to run after connecting here ex. register to receive location updates


        try {
            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (location != null) {
                //LatTextView.setText(location.getLatitude() + "");
                //LonTextView.setText(location.getLongitude() + "");
                Log.d("TS", "" + location.getLatitude() + "|" + location.getLongitude());
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        } catch (SecurityException s) {
            Log.d("TS", "Not able to run location services...");
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 123)
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                onConnected(new Bundle());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onLocationChanged(@NonNull Location location) {

        //Get user Status
        String status;
        if (ProfileActivity.isPositive){status = "Positive";}
        else{status = "Negative";}

        Log.d("TS", "" + location.getLatitude() + "|" + location.getLongitude());
        //generateNotification("" + location.getLatitude() + "|" + location.getLongitude());

        boolean HasBeenRead=false;
        Map<String, Object> data = new HashMap<>();
        data.put("Lattitude",location.getLatitude()+"");
        data.put("Longitude",location.getLongitude()+"");
        data.put("Uid", auth.getCurrentUser().getUid());
        data.put("timeStamp", System.currentTimeMillis()+"");
        data.put("Status", status);
        data.put("AlreadyRead",HasBeenRead);
        Log.d("TS", data.toString());
        db.collection("LocationStamps").add(data);
        //db.collection("LocationStamps").document(auth.getCurrentUser().getUid()).collection("UserLocations").add(data);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Gps connection failed", Toast.LENGTH_SHORT).show();
    }
}