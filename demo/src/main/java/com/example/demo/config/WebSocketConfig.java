package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration; // <-- ADD THIS IMPORT
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // This enables WebSocket message handling
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // ADD THIS FIELD
    private final JwtChannelInterceptor jwtChannelInterceptor;

    // ADD THIS CONSTRUCTOR
    public WebSocketConfig(JwtChannelInterceptor jwtChannelInterceptor) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // This is the endpoint the client will connect to
        // setAllowedOriginPatterns("*") allows all origins (good for development)
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Defines prefixes for messages bound for the broker (e.g., to a topic OR a user)
        // --- THIS LINE IS MODIFIED ---
        registry.enableSimpleBroker("/topic", "/user"); 
        
        // Defines the prefix for messages from clients to the server (e.g., to a @MessageMapping)
        registry.setApplicationDestinationPrefixes("/app");

        // --- ADD THIS LINE ---
        // This enables 1-to-1 messaging by prefixing destinations with /user
        registry.setUserDestinationPrefix("/user");
    }

    // --- ADD THIS ENTIRE METHOD ---
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // This registers our interceptor to validate the JWT on CONNECT messages
        registration.interceptors(jwtChannelInterceptor);
    }
}