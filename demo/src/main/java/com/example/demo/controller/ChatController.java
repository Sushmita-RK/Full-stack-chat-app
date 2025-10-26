package com.example.demo.controller;

import com.example.demo.model.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

// --- ADD THESE IMPORTS ---
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.security.Principal;
// --- END IMPORTS ---

@Controller
public class ChatController {

    // --- ADD THIS (for sending private messages) ---
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    // --- END ADD ---


    /**
     * This method handles new users joining the chat.
... existing code ...
     * It then broadcasts the "JOIN" message to everyone subscribed to "/topic/public".
     */
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public") 
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor,
                               Principal principal) { // <-- Added Principal
        
        // Use the authenticated username from the Principal
        String username = principal.getName(); 
        
        // Add username to the WebSocket session
        headerAccessor.getSessionAttributes().put("username", username);
        // Set the sender from the authenticated principal
        chatMessage.setSender(username); 
        return chatMessage;
    }

    /**
     * This method handles regular public chat messages.
... existing code ...
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage, Principal principal) { // <-- Added Principal
        // Set the sender from the authenticated principal
        chatMessage.setSender(principal.getName()); 
        return chatMessage; 
    }

    // --- ADD THIS ENTIRE NEW METHOD ---
    /**
     * This method handles private chat messages.
     * A client sends a message to the "/app/chat.sendPrivateMessage" destination.
     * The method sends the message to a user-specific queue: /user/{recipient}/queue/private
     */
    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage, Principal principal) {
        // Set the sender from the authenticated principal
        chatMessage.setSender(principal.getName()); 
        
        // Send the private message to the recipient's queue
        messagingTemplate.convertAndSendToUser(
            chatMessage.getRecipient(), // The recipient's username
            "/queue/private",           // The private queue
            chatMessage                 // The message payload
        );
    }
    // --- END NEW METHOD ---
}
