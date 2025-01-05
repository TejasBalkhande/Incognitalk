package com.example.tejasbalkhande;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private Button joinBtn, createBtn;
    private ImageButton LogoutBtn;
    private RecyclerView recyclerView;
    private GroupAdapter groupAdapter;
    private ArrayList<String> groupList;
    private FirebaseAuth auth;

    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        joinBtn = findViewById(R.id.JoinGroupBtn);
        createBtn = findViewById(R.id.CreateGroupBtn);
        LogoutBtn = findViewById(R.id.imageButton);

        auth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclergroups);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mediaPlayer = MediaPlayer.create(this, R.raw.clicksound);


        groupList = new ArrayList<>();
        groupAdapter = new GroupAdapter(groupList, groupName -> {
            Intent intent = new Intent(MainActivity.this, MessageActivity.class);
            intent.putExtra("groupName", groupName);
            startActivity(intent);
        });
        recyclerView.setAdapter(groupAdapter);

        LogoutBtn.setOnClickListener(view -> {
            auth.signOut();
            Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        fetchGroupNames();

        createBtn.setOnClickListener(view -> {
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
            startActivity(new Intent(MainActivity.this, CreateGroupActivity.class));
        });

        joinBtn.setOnClickListener(view -> {
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }

            startActivity(new Intent(MainActivity.this, JoinGroup.class));
        });
    }

    private void fetchGroupNames() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(MainActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserEmail = currentUser.getEmail();
        Log.d("MainActivity", "Current User Email: " + currentUserEmail);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("GroupNames");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashSet<String> uniqueGroups = new HashSet<>();
                groupList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String groupName = snapshot.child("Grpname").getValue(String.class);
                    DataSnapshot membersSnapshot = snapshot.child("Members");

                    if (membersSnapshot != null && groupName != null) {
                        for (DataSnapshot member : membersSnapshot.getChildren()) {
                            String memberEmail = member.getValue(String.class);
                            if (currentUserEmail != null && currentUserEmail.equals(memberEmail)) {
                                uniqueGroups.add(groupName);
                                Log.d("MainActivity", "Group found: " + groupName);
                            }
                        }
                    }
                }

                groupList.addAll(uniqueGroups);
                groupAdapter.notifyDataSetChanged();

                if (uniqueGroups.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No groups found for your email", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", databaseError.getMessage());
            }
        });
    }
}