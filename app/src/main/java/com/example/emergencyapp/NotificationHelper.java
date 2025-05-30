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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = "Guest", address = "Not Available", destination = "Not Available",
                        companions = "Not Available", family = "Not Available", bloodGroup = "Not Available";

                if (snapshot.exists()) {
                    // Extract data from Firebase
                    username = snapshot.child("username").getValue(String.class);
                    address = snapshot.child("address").getValue(String.class);
                    destination = snapshot.child("destination").getValue(String.class);
                    companions = snapshot.child("companions").getValue(String.class);
                    family = snapshot.child("family").getValue(String.class);
                    bloodGroup = snapshot.child("bloodGroup").getValue(String.class);

                    // Save to SharedPreferences as backup for offline
                    SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("username", username);
                    editor.putString("address", address);
                    editor.putString("destination", destination);
                    editor.putString("companions", companions);
                    editor.putString("family", family);
                    editor.putString("blood", bloodGroup);
                    editor.apply();
                } else {
                    // Firebase data not found, fallback to stored data
                    SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    username = sp.getString("username", "Guest");
                    address = sp.getString("address", "Not Available");
                    destination = sp.getString("destination", "Not Available");
                    companions = sp.getString("companions", "Not Available");
                    family = sp.getString("family", "Not Available");
                    bloodGroup = sp.getString("blood", "Not Available");
                }

                buildAndShowNotification(context, username, address, destination, companions, family, bloodGroup);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Firebase fetch failed, fallback to stored SharedPreferences
                SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                String username = sp.getString("username", "Guest");
                String address = sp.getString("address", "Not Available");
                String destination = sp.getString("destination", "Not Available");
                String companions = sp.getString("companions", "Not Available");
                String family = sp.getString("family", "Not Available");
                String bloodGroup = sp.getString("blood", "Not Available");

                buildAndShowNotification(context, username, address, destination, companions, family, bloodGroup);
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
                .setSmallIcon(R.drawable.ic_launcher_foreground) // replace with actual icon
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

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
            notificationManager.createNotificationChannel(channel);
        }
    }
}
