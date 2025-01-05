package com.example.tejasbalkhande;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class CreateGroupActivity extends AppCompatActivity {

    private EditText groupNameInput;
    private Button createGroupButton;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_group);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        groupNameInput = findViewById(R.id.UniqueNameGrp);
        createGroupButton = findViewById(R.id.CreateNewGroupBtn);

        mediaPlayer = MediaPlayer.create(this, R.raw.clicksound);


        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("GroupNames");

        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
                String grpname = groupNameInput.getText().toString().trim();
                if(TextUtils.isEmpty(grpname)){
                    Toast.makeText(CreateGroupActivity.this, "Please enter a group name", Toast.LENGTH_SHORT).show();
                }
                else{
                    checkAndAddGroup(grpname);
                }
            }
        });

    }

    private void checkAndAddGroup(String grpname) {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = currentUser.getEmail();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean groupExists = false;
                for (DataSnapshot child : snapshot.getChildren()) {
                    String existingGroupName = child.child("Grpname").getValue(String.class);
                    if (existingGroupName != null && existingGroupName.equalsIgnoreCase(grpname)) {
                        groupExists = true;
                        break;
                    }
                }
                if (groupExists) {
                    Toast.makeText(CreateGroupActivity.this, "Group name already exists!", Toast.LENGTH_SHORT).show();
                } else {
                    String groupId = databaseReference.push().getKey();
                    if (groupId != null) {
                        DatabaseReference groupRef = databaseReference.child(groupId);
                        groupRef.child("Grpname").setValue(grpname);
                        groupRef.child("Members").child("Member1").setValue(userEmail);

                        Toast.makeText(CreateGroupActivity.this, "Group added successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CreateGroupActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(CreateGroupActivity.this, "Error adding group!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateGroupActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    }
