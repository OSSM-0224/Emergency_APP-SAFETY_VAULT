package com.example.emergencyapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            // User not logged in, open SignupActivity
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
        } else {
            // User already logged in, open UserDetails
            // Pass username from currentUser if you want
            String username = currentUser.getDisplayName(); // agar set kiya ho to
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.putExtra("username_key", username);
            startActivity(intent);
        }
        finish();
    }
}
