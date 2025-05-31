package com.example.emergencyapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ContactPage extends AppCompatActivity implements ContactAdapter.OnItemClickListener {

    private static final int PERMISSION_REQUEST_CALL = 100;
    private ContactAdapter adapter;
    private final ArrayList<ContactModel> contactList = new ArrayList<>();
    private String phoneToCall = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_page);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewEntries);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(contactList, this);
        recyclerView.setAdapter(adapter);

        Button addButton = findViewById(R.id.addButton);
        ImageView backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ContactPage.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(ContactPage.this, AddNumberActivity.class);
            startActivityForResult(intent, 101);
        });

        loadContacts();
    }

    private void loadContacts() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        FirebaseDatabase.getInstance().getReference("Users")
                .child(userId)
                .child("Contacts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        contactList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ContactModel contact = dataSnapshot.getValue(ContactModel.class);
                            if (contact != null) {
                                contact.setId(dataSnapshot.getKey());
                                contactList.add(contact);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ContactPage.this, "Failed to load contacts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onCallClick(int position) {
        ContactModel contact = contactList.get(position);
        phoneToCall = contact.getPhone();
        if (phoneToCall == null || phoneToCall.isEmpty()) {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CALL);
        } else {
            startCall();
        }
    }

    private void startCall() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneToCall));
        startActivity(callIntent);
    }

    public void onDeleteClick(int position) {
        ContactModel contactToDelete = contactList.get(position);
        String contactId = contactToDelete.getId();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        if (contactId != null) {
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(userId)
                    .child("Contacts")
                    .child(contactId)
                    .removeValue()
                    .addOnSuccessListener(aVoid -> {
                        contactList.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, contactList.size());
                        Toast.makeText(this, "Contact deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to delete contact: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(this, "Error: Contact ID not found", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            // Remove manual add to list

            // Just reload all contacts from Firebase
            loadContacts();

            Toast.makeText(this, "Contact added", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCall();
            } else {
                Toast.makeText(this, "Call permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
