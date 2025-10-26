package com.example.demo.model;

public class ChatMessage {

    private String content;
    private String sender;
    private String recipient; // <-- ADDED THIS FIELD
    private MessageType type;

    // Enum for message type
    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }

    // --- Getters and Setters ---
    
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() { // <-- ADDED GETTER
        return recipient;
    }

    public void setRecipient(String recipient) { // <-- ADDED SETTER
        this.recipient = recipient;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}
