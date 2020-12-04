package com.mobapps.covidcontacttracer;

        import android.app.Notification;
        import android.app.NotificationChannel;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.app.Service;
        import android.content.Intent;
        import android.os.Build;
        import android.os.IBinder;
        import android.util.Log;
        import android.widget.Toast;

        import androidx.annotation.NonNull;
        import androidx.annotation.RequiresApi;
        import androidx.core.app.NotificationCompat;

        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.firestore.FirebaseFirestore;
        import com.google.firebase.firestore.QueryDocumentSnapshot;
        import com.google.firebase.firestore.QuerySnapshot;

        import java.util.ArrayList;
        import java.util.Date;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;
        import java.util.Timer;
        import java.util.TimerTask;

public class ContactTracingService extends Service {
    private Timer timer;
    private FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static Map<String,String> MessageList=new HashMap<>();// To display the messaging List
    public ContactTracingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("service", "Service created");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public int onStartCommand(Intent intent, int flags, int startId) {

        auth=FirebaseAuth.getInstance();
        sendNotification("Contact tracing service running");
        startTimer();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopTimer();
    }

    private void stopTimer() {
        Log.d("service", "Service destroyed");
        if (timer != null) {
            timer.cancel();
        }
    }

    private void startTimer() {
        TimerTask task = new TimerTask() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                Log.d("service", "Timer task started");
                db.collection("LocationStamps")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    String currentUserId = auth.getCurrentUser().getUid();
                                    if (ProfileActivity.isPositive){
                                        for(QueryDocumentSnapshot document:task.getResult())
                                        {
                                            Map<String,String> current=new HashMap<>();
                                            if(document.get("Uid").equals( currentUserId) && document.get("AlreadyRead").equals( false ))
                                            {
                                                current.put("Lattitude",(String)document.get("Lattitude"));
                                                current.put("Longitude",(String)document.get("Longitude"));
                                                current.put("TimeStamp",(String)document.get("timeStamp"));
                                                current.put("Current_ID",document.getId());
                                                for (QueryDocumentSnapshot InnerDocument : task.getResult()) {
                                                    Map<String,String> Check=new HashMap<>();
                                                    if (!InnerDocument.get("Uid").equals(currentUserId) && InnerDocument.get("Status").equals("Negative"))
                                                    {
                                                        Check.put("Lattitude",(String)InnerDocument.get("Lattitude"));
                                                        Check.put("Longitude",(String)InnerDocument.get("Longitude"));
                                                        Check.put("TimeStamp",(String)InnerDocument.get("timeStamp"));
                                                        Check.put("Checking ID", InnerDocument.getId());
                                                        Check.put("CheckUserID",(String)InnerDocument.get( "Uid" ));
                                                        CheckAddToMessage(current,Check);
                                                    }
                                                }
                                            }
                                        }

                                    }
                                } else {
                                    Log.d("DB2", "Error getting documents: ", task.getException());
                                }
                            }
                        });

            }
        };
        timer = new Timer(true);
        int delay = 5000;      // 1 second
        int interval = 1000 * 15;   // 15 secs
        timer.schedule(task, delay, interval);

    }

    private void CheckAddToMessage(Map<String, String> current, Map<String, String> check) {
        // Need to check for two things now :
        // The distance should be less than 5 meters.
        // The GPS Location should be less than 5 meters apart.
        boolean addToList=false;

        if(current.get( "Lattitude" ).equals(check.get("Lattitude")) && current.get("Longitude").equals(check.get("Longitude")))
        {
            addToList=true;
        }

        // Calculate The Distance between the 2 Coordinates:
        double Longitude1=Math.toRadians(Double.parseDouble(current.get("Longitude")));
        double Longitude2=Math.toRadians(Double.parseDouble(check.get("Longitude")));
        double Lattitude1=Math.toRadians(Double.parseDouble(check.get("Lattitude")));
        double Lattitude2=Math.toRadians(Double.parseDouble(check.get("Lattitude")));

        double di_lon=Longitude2-Longitude1;
        double di_lat=Lattitude2-Lattitude1;

        double a = Math.pow(Math.sin(di_lat / 2), 2)
                + Math.cos(Lattitude1) * Math.cos(Lattitude2)
                * Math.pow(Math.sin(di_lon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));

        double r = 6371;

        double dist=(c*r)*1000;


        // Checking the number of hours between the two timestamps
        //Double DifferenceInTime=Math.abs(Double.parseDouble(current.get( "timeStamp" ))-Double.parseDouble( check.get("timeStamp")));
        Double DifferenceInTime=Math.abs(Double.parseDouble(current.get("TimeStamp"))-Double.parseDouble(check.get("TimeStamp")))/(60*60*1000);

        Map <String,Object> distance=new HashMap<>();// To Map the Distance ( Debugging mode )
        ArrayList<String> checkUID=new ArrayList<>();// To Prevent multiple entries of same UID Document
        if(dist<=5 && DifferenceInTime<=72)// 3 days- 72 hours
        {
            if(!checkUID.contains(check.get("CheckUserID"))) {
                MessageList.put( "UserID", check.get( "CheckUserID" ) );
                db.collection( "Message List" ).document( check.get( "CheckUserID" ) ).set( MessageList );
                checkUID.add(check.get("CheckUserID"));
            }
            distance.put("Distance In Meters", dist)    ;
            distance.put("Difference in Time in Hours",DifferenceInTime);
           // distance.put("Days Between", Double.parseDouble(String.valueOf(DaysDifference)));
            db.collection("DistanceBetweenPoints").add(distance);
        }
        db.collection( "LocationStamps" ).document(current.get("Current_ID")).update( "AlreadyRead",true );
        //Toast.makeText( ContactTracingService.this, "ID is "+current.get("Current_ID"), Toast.LENGTH_SHORT ).show();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotification(String s) {
        // create the intent for the notification


        Intent notificationIntent = new Intent(this, MapsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        // create the pending intent
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, flags);

        // create the variables for the notification
        int icon = R.drawable.app_bg;
        CharSequence tickerText = "Look into the distance!";
        CharSequence contentTitle = getText(R.string.app_name);
        CharSequence contentText = s;

        NotificationChannel notificationChannel =
                new NotificationChannel("Channel_ID", "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager manager = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(notificationChannel);


        // create the notification and set its data
        Notification notification = new NotificationCompat
                .Builder(this, "Channel_ID")
                .setSmallIcon(icon)
                .setTicker(tickerText)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setChannelId("Channel_ID")
                .build();

        startForeground(2, notification); //TS: a different function to show the notification
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("service", "Service bound - not used!");
        return null;
    }
}