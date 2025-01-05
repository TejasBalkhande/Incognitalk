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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JoinGroup extends AppCompatActivity {

    private EditText groupNameInput;
    private Button joinGroupButton;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        groupNameInput = findViewById(R.id.entergroupname);
        joinGroupButton = findViewById(R.id.joinbtn);
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("GroupNames");

        mediaPlayer = MediaPlayer.create(this, R.raw.clicksound);


        joinGroupButton.setOnClickListener(view -> {

            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
            String groupName = groupNameInput.getText().toString().trim();
            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(JoinGroup.this, "Please enter a group name", Toast.LENGTH_SHORT).show();
            } else {
                joinGroup(groupName);
            }
        });
    }

    private void joinGroup(String groupNameToJoin) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = currentUser.getEmail();
        if (userEmail == null) {
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean groupFound = false;

                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    String groupName = groupSnapshot.child("Grpname").getValue(String.class);

                    if (groupName != null && groupName.equalsIgnoreCase(groupNameToJoin)) {
                        groupFound = true;

                        // Check if the user is already a member
                        boolean alreadyMember = false;
                        for (DataSnapshot memberSnapshot : groupSnapshot.child("Members").getChildren()) {
                            String memberEmail = memberSnapshot.getValue(String.class);
                            if (memberEmail != null && memberEmail.equals(userEmail)) {
                                alreadyMember = true;
                                break;
                            }
                        }

                        if (alreadyMember) {
                            Toast.makeText(JoinGroup.this, "You are already a member of this group", Toast.LENGTH_SHORT).show();
                        } else {
                            // Add user to the Members list
                            groupSnapshot.getRef().child("Members").push().setValue(userEmail)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(JoinGroup.this, "Successfully joined group!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(JoinGroup.this, MainActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(JoinGroup.this, "Failed to join group", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        break;
                    }
                }

                if (!groupFound) {
                    Toast.makeText(JoinGroup.this, "Group not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(JoinGroup.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("JoinGroup", error.getMessage());
            }
        });
    }
}
