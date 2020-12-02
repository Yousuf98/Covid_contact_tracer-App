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

        import androidx.annotation.NonNull;
        import androidx.annotation.RequiresApi;
        import androidx.core.app.NotificationCompat;

        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.firestore.FirebaseFirestore;
        import com.google.firebase.firestore.QueryDocumentSnapshot;
        import com.google.firebase.firestore.QuerySnapshot;

        import java.util.Timer;
        import java.util.TimerTask;

public class ContactTracingService extends Service {
    private Timer timer;
    private FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
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
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        if (!document.get("Uid").equals(currentUserId) && document.get("Status").equals("Negative")){
                                        Log.d("DB2", document.getId() + " => " + document.get("Uid"));}
                                    }
                                    }
                                } else {
                                    Log.d("DB2", "Error getting documents: ", task.getException());
                                }
                            }
                        });
                sendNotification("Test notification");
            }
        };
        timer = new Timer(true);
        int delay = 5000;      // 1 second
        int interval = 1000 * 15;   // 15 secs
        timer.schedule(task, delay, interval);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotification(String s) {
        // create the intent for the notification
        Intent notificationIntent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        // create the pending intent
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, flags);

        // create the variables for the notification
        int icon = R.drawable.ic_launcher_background;
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
