package com.example.tejasbalkhande;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageActivity extends AppCompatActivity {

    private RecyclerView messageRecyclerView;
    private MessageAdapter messageAdapter;
    private TextView groupNameTextView;

    private ArrayList<Message> messagesList;
    private EditText textMessageInput;
    private Button sendButton;

    private String groupName;
    private DatabaseReference messagesDatabaseRef;
    private HashMap<String, String> userMap;
    private static HashMap<String, String> staticUserMap = new HashMap<>(); // Cached user data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        groupName = getIntent().getStringExtra("groupName");
        if (groupName == null) {
            Toast.makeText(this, "Group not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI elements
        messagesList = new ArrayList<>();
        userMap = new HashMap<>();
        messageRecyclerView = findViewById(R.id.RecyclerMessage);
        textMessageInput = findViewById(R.id.TextMessage);
        sendButton = findViewById(R.id.SendBtn);
        groupNameTextView = findViewById(R.id.textView2);

        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messagesList);
        messageRecyclerView.setAdapter(messageAdapter);

        // Firebase reference for messages
        messagesDatabaseRef = FirebaseDatabase.getInstance().getReference("Messages").child(groupName);

        // Fetch user details and messages
        fetchUserMap();
        fetchMessages();

        // Set the group name to the TextView
        groupNameTextView.setText(groupName);

        // Handle message sending
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void fetchUserMap() {
        // Use cached user data if available
        if (!staticUserMap.isEmpty()) {
            userMap.putAll(staticUserMap);
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    String email = data.child("email").getValue(String.class);
                    String name = data.child("name").getValue(String.class);
                    if (email != null && name != null) {
                        userMap.put(email, name);
                    }
                }
                staticUserMap.putAll(userMap); // Cache the user data
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessageActivity.this, "Error fetching users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMessages() {
        messagesDatabaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot data, String previousChildName) {
                String email = data.child("email").getValue(String.class);
                String message = data.child("message").getValue(String.class);
                Long timestamp = data.child("timestamp").getValue(Long.class);

                if (email != null && message != null && timestamp != null) {
                    String username = userMap.get(email);
                    if (username == null) {
                        username = "Unknown User";
                    }
                    messagesList.add(new Message(username, message, timestamp));
                    messageAdapter.notifyDataSetChanged();
                    messageRecyclerView.scrollToPosition(messagesList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot data, String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot data) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot data, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessageActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String message = textMessageInput.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email == null) {
            Toast.makeText(this, "Unable to identify user", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis(); // Current timestamp in milliseconds
        String messageId = messagesDatabaseRef.push().getKey();
        if (messageId != null) {
            HashMap<String, Object> messageMap = new HashMap<>();
            messageMap.put("email", email);
            messageMap.put("message", message);
            messageMap.put("timestamp", timestamp);

            messagesDatabaseRef.child(messageId).setValue(messageMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            textMessageInput.setText(""); // Clear the input field
                        } else {
                            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}