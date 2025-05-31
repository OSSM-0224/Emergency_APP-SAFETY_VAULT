package com.example.emergencyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homeactivity); // your layout file name

        // Check if user is logged in
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setupCards();
    }

    private void setupCards() {
        // Setup click listeners for cards with debug Toast
        setupCardClick(R.id.contactCard, ContactPage.class, 0);
        setupCardClick(R.id.userCard, UserDetails.class, 1);
        setupCardClick(R.id.sosCard, SOSActivity.class, 2);
        setupCardClick(R.id.itemsCard, ItemsActivity.class, 3);
    }

    private void setupCardClick(int cardId, Class<?> activityToOpen, int animationDelayMultiplier) {
        CardView card = findViewById(cardId);
        if (card == null) {
            Toast.makeText(this, "Card with ID " + cardId + " not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Slide-in animation with stagger
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
        slideIn.setStartOffset(animationDelayMultiplier * 100);
        card.startAnimation(slideIn);

        card.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.card_click_scale));
            Toast.makeText(HomeActivity.this, "Opening " + activityToOpen.getSimpleName(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, activityToOpen));
        });
    }
}

