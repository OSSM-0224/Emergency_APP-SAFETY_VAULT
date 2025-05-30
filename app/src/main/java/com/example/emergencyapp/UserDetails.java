package com.example.emergencyapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class UserDetails extends AppCompatActivity {

    ImageView backBtn, profileImage;
    EditText txtAddress, txtDestination, txtBlood, txtFamily, txtCompanions;
    Button btnUploadPhoto, btnSave, btnUpdate, btnLogout;

    FirebaseAuth auth;
    DatabaseReference databaseReference;

    TextView usernameTV;

    Uri imageUri;
    StorageReference storageReference;

    String username;  // store username once

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Init views FIRST before using them
        usernameTV = findViewById(R.id.username);
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

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        username = auth.getCurrentUser().getDisplayName();
        if (username == null || username.isEmpty()) {
            username = "Guest";  // fallback
        }
        usernameTV.setText(username);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        backBtn.setOnClickListener(v -> finish());

        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        // Setup image picker launcher
        ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        try {
                            profileImage.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri));
                            uploadPhotoToFirebase(uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        btnUploadPhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Load profile image from Firebase if exists
        userRef.child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String imageUrl = snapshot.getValue(String.class);
                    Glide.with(UserDetails.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.default_profile)
                            .into(profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UserDetails.this, "Failed to load profile photo", Toast.LENGTH_SHORT).show();
            }
        });

        // Load username from database (overwrite if exists)
        userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fetchedUsername = snapshot.getValue(String.class);
                    if (fetchedUsername != null && !fetchedUsername.isEmpty()) {
                        usernameTV.setText(fetchedUsername);
                    } else {
                        usernameTV.setText("Guest");
                    }
                } else {
                    usernameTV.setText("Guest");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                usernameTV.setText("Guest");
                Toast.makeText(UserDetails.this, "Failed to load username", Toast.LENGTH_SHORT).show();
            }
        });

        btnSave.setOnClickListener(v -> saveUserDetails());

        btnUpdate.setOnClickListener(v -> updateUserDetails());

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(UserDetails.this, LoginActivity.class));
            finish();
        });

        loadUserData(userId);
    }

    private void loadUserData(String userId) {
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserDetailsData userDetails = snapshot.getValue(UserDetailsData.class);
                    if (userDetails != null) {
                        txtAddress.setText(userDetails.address);
                        txtDestination.setText(userDetails.destination);
                        txtBlood.setText(userDetails.bloodGroup);
                        txtFamily.setText(userDetails.family);
                        txtCompanions.setText(userDetails.companions);

                        btnSave.setEnabled(false);
                        btnUpdate.setEnabled(true);
                    }
                } else {
                    btnSave.setEnabled(true);
                    btnUpdate.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UserDetails.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserDetails() {
        if (!validateInputs()) return;

        String userId = auth.getCurrentUser().getUid();

        UserDetailsData userDetails = new UserDetailsData(
                txtAddress.getText().toString().trim(),
                txtDestination.getText().toString().trim(),
                txtBlood.getText().toString().trim(),
                txtFamily.getText().toString().trim(),
                txtCompanions.getText().toString().trim()
        );

        databaseReference.child(userId).setValue(userDetails)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Details saved successfully!", Toast.LENGTH_SHORT).show();
                        btnSave.setEnabled(false);
                        btnUpdate.setEnabled(true);
                        saveToPreferencesAndShowNotification(userId);
                    } else {
                        Toast.makeText(this, "Failed to save details: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateUserDetails() {
        if (!validateInputs()) return;

        String userId = auth.getCurrentUser().getUid();

        UserDetailsData updatedDetails = new UserDetailsData(
                txtAddress.getText().toString().trim(),
                txtDestination.getText().toString().trim(),
                txtBlood.getText().toString().trim(),
                txtFamily.getText().toString().trim(),
                txtCompanions.getText().toString().trim()
        );

        databaseReference.child(userId).setValue(updatedDetails)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Details updated successfully!", Toast.LENGTH_SHORT).show();
                        saveToPreferencesAndShowNotification(userId);
                    } else {
                        Toast.makeText(this, "Failed to update details: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveToPreferencesAndShowNotification(String userId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId);
        ref.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String address = snapshot.child("address").getValue(String.class);
                String destination = snapshot.child("destination").getValue(String.class);
                String companions = snapshot.child("companions").getValue(String.class);
                String family = snapshot.child("family").getValue(String.class);
                String blood = snapshot.child("bloodGroup").getValue(String.class);

                SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();

                editor.putString("address", (address != null && !address.isEmpty()) ? address : "None");
                editor.putString("destination", (destination != null && !destination.isEmpty()) ? destination : "None");
                editor.putString("companions", (companions != null && !companions.isEmpty()) ? companions : "None");
                editor.putString("family", (family != null && !family.isEmpty()) ? family : "None");
                editor.putString("blood", (blood != null && !blood.isEmpty()) ? blood : "None");
                editor.putString("username", (username != null && !username.isEmpty()) ? username : "Guest");

                editor.apply();

                Intent serviceIntent = new Intent(this, NotificationService.class);
                startService(serviceIntent);
            }
        });
    }

    private void uploadPhotoToFirebase(Uri uri) {
        String userId = auth.getCurrentUser().getUid();
        StorageReference fileRef = storageReference.child(userId + ".jpg");

        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    FirebaseDatabase.getInstance().getReference("Users").child(userId)
                            .child("profileImageUrl").setValue(downloadUri.toString());

                    Toast.makeText(this, "Photo uploaded successfully!", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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

    // Inner class for user details stored in database (without username and email)
    public static class UserDetailsData {
        public String address, destination, bloodGroup, family, companions;

        public UserDetailsData() {}

        public UserDetailsData(String address, String destination, String bloodGroup, String family, String companions) {
            this.address = address;
            this.destination = destination;
            this.bloodGroup = bloodGroup;
            this.family = family;
            this.companions = companions;
        }
    }
}
