package com.example.emergencyapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddNumberActivity extends AppCompatActivity {

    private EditText nameEditText, phoneEditText, bloodGroupEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual_entry_form);

        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        bloodGroupEditText = findViewById(R.id.bloodGroupEditText);
        saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(v -> saveContactToFirebase());
    }

    private void saveContactToFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String bloodGroup = bloodGroupEditText.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || bloodGroup.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference firebaseRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("Contacts");

        String id = firebaseRef.push().getKey();
        if (id != null) {
            ContactModel contact = new ContactModel(name, phone, bloodGroup);
            firebaseRef.child(id).setValue(contact)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddNumberActivity.this, "Contact saved to Firebase", Toast.LENGTH_SHORT).show();
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("name", name);
                        returnIntent.putExtra("phone", phone);
                        returnIntent.putExtra("bloodGroup", bloodGroup);
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(AddNumberActivity.this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }
}
