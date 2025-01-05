package com.example.tejasbalkhande;

public class Message {
    private String username;
    private String message;
    private long timestamp;  // Store timestamp in milliseconds

    public Message(String username, String message, long timestamp) {
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
