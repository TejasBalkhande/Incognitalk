package com.example.tejasbalkhande;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private EditText Email, Password;
    private Button LoginBtn, CreateBtn;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        Email = findViewById(R.id.LoginEmail);
        Password = findViewById(R.id.LoginPass);
        LoginBtn = findViewById(R.id.LoginBtn);
        CreateBtn = findViewById(R.id.CreateBtn);

        mediaPlayer = MediaPlayer.create(this, R.raw.clicksound);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Login button click listener
        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = Email.getText().toString().trim();
                String password = Password.getText().toString().trim();

                if (mediaPlayer != null) {
                    mediaPlayer.start();  // Play the sound
                }

                // Validate input
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Please enter details.", Toast.LENGTH_SHORT).show();
                } else {
                    // Attempt to log in
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Login successful
                                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    // Login failed
                                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login failed";
                                    Log.e("LoginActivity", "Error: " + errorMessage);
                                    Toast.makeText(LoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        // Create Account button click listener
        CreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Redirect to RegisterActivity
                if (mediaPlayer != null) {
                    mediaPlayer.start();  // Play the sound
                }
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }
}
