package com.example.emergencyapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class NotificationHelper {

    private static final String CHANNEL_ID = "safety_vault_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final String PREFS_NAME = "UserPrefs";

    public static void showNotification(Context context) {
        createNotificationChannel(context);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.e("NotificationHelper", "No user logged in.");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        DatabaseReference detailsRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        final String[] username = {"Guest"};
        final String[] address = {"Not Available"};
        final String[] destination = {"Not Available"};
        final String[] companions = {"Not Available"};
        final String[] family = {"Not Available"};
        final String[] bloodGroup = {"Not Available"};

        // Fetch data asynchronously
        usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("username")) {
                    username[0] = snapshot.child("username").getValue(String.class);
                }

                detailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (snapshot.hasChild("address"))
                                address[0] = snapshot.child("address").getValue(String.class);
                            if (snapshot.hasChild("destination"))
                                destination[0] = snapshot.child("destination").getValue(String.class);
                            if (snapshot.hasChild("companions"))
                                companions[0] = snapshot.child("companions").getValue(String.class);
                            if (snapshot.hasChild("family"))
                                family[0] = snapshot.child("family").getValue(String.class);
                            if (snapshot.hasChild("bloodGroup"))
                                bloodGroup[0] = snapshot.child("bloodGroup").getValue(String.class);

                            // Save to SharedPreferences
                            SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("username", username[0]);
                            editor.putString("address", address[0]);
                            editor.putString("destination", destination[0]);
                            editor.putString("companions", companions[0]);
                            editor.putString("family", family[0]);
                            editor.putString("blood", bloodGroup[0]);
                            editor.apply();
                        }

                        buildAndShowNotification(context, username[0], address[0], destination[0], companions[0], family[0], bloodGroup[0]);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        buildAndShowNotification(context, username[0], address[0], destination[0], companions[0], family[0], bloodGroup[0]);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                buildAndShowNotification(context, username[0], address[0], destination[0], companions[0], family[0], bloodGroup[0]);
            }
        });
    }

    private static void buildAndShowNotification(Context context, String username, String address,
                                                 String destination, String companions, String family, String bloodGroup) {

        String title = "Safety Vault Emergency Info for " + username;
        String message = "📍 Address: " + address + "\n" +
                "🚩 Destination: " + destination + "\n" +
                "👥 Companions: " + companions + "\n" +
                "📞 Family: " + family + "\n" +
                "🩸 Blood Group: " + bloodGroup;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)  // apna icon yahan use karo
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)          // <-- Ye banata hai notification persistent, swipe se clear nahi hoga
                .setAutoCancel(false);     // Notification tap pe bhi nahi jayega automatically

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Safety Vault Alerts";
            String description = "Emergency notifications for Safety Vault";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
