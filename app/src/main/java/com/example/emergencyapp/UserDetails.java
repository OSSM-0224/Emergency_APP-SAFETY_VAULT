package com.example.emergencyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


public class UserDetails extends AppCompatActivity {

    ImageView backBtn, profileImage;
    EditText txtAddress, txtDestination, txtBlood, txtFamily, txtCompanions;
    Button btnUploadPhoto, btnSave, btnUpdate, btnLogout;

    FirebaseAuth auth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Initialize views
        TextView usernameTV = findViewById(R.id.username);
        backBtn = findViewById(R.id.backBtn);
        profileImage = findViewById(R.id.profileImage);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);

        txtAddress = findViewById(R.id.txtAddress);
        txtDestination = findViewById(R.id.txtDestination);
        txtBlood = findViewById(R.id.txtBlood);
        txtFamily = findViewById(R.id.txtFamily);
        txtCompanions = findViewById(R.id.txtCompanions);

        btnSave = findViewById(R.id.btnSave);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnLogout = findViewById(R.id.btnLogout);

        auth = FirebaseAuth.getInstance();

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        backBtn.setOnClickListener(v -> finish());

        btnUploadPhoto.setOnClickListener(v -> {
            Toast.makeText(this, "Upload photo clicked (implement later)", Toast.LENGTH_SHORT).show();
        });

        // Get current user id
        String userId = auth.getCurrentUser().getUid();

        // Load user data from Firebase and set to views
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        usernameTV.setText(user.username);
                        txtAddress.setText(user.address);
                        txtDestination.setText(user.destination);
                        txtBlood.setText(user.bloodGroup);
                        txtFamily.setText(user.family);
                        txtCompanions.setText(user.companions);

                        // Disable save if details exist, enable update
                        btnSave.setEnabled(false);
                        btnUpdate.setEnabled(true);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UserDetails.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });

        // We don't need to get username from Intent anymore
        // String username = getIntent().getStringExtra("username_key");
        String email = auth.getCurrentUser().getEmail(); // Get logged-in user's email

        btnSave.setOnClickListener(v -> saveUserDetails(usernameTV.getText().toString(), email));

        btnUpdate.setOnClickListener(v -> updateUserDetails());

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(UserDetails.this, LoginActivity.class));
            finish();
        });
    }


    private void updateUserDetails() {
        // Similar to save, you can implement update if needed
        Toast.makeText(this, "Update feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(txtAddress.getText().toString().trim())) {
            txtAddress.setError("Address required");
            txtAddress.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(txtDestination.getText().toString().trim())) {
            txtDestination.setError("Destination required");
            txtDestination.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(txtBlood.getText().toString().trim())) {
            txtBlood.setError("Blood Group required");
            txtBlood.requestFocus();
            return false;
        }
        return true;
    }

    // User class to hold data
    public static class User {
        public String username, email, address, destination, bloodGroup, family, companions;

        // Default constructor required for Firebase
        public User() {
        }

        public User(String username, String email, String address, String destination,
                    String bloodGroup, String family, String companions) {
            this.username = username;
            this.email = email;
            this.address = address;
            this.destination = destination;
            this.bloodGroup = bloodGroup;
            this.family = family;
            this.companions = companions;
        }
    }
}
