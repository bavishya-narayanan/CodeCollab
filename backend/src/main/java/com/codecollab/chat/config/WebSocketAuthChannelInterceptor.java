package com.codecollab.chat.config;

import com.codecollab.security.CustomUserDetailsService;
import com.codecollab.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor
            .getAccessor(message, StompHeaderAccessor.class);
        if (acc == null) return message;

        if (StompCommand.CONNECT.equals(acc.getCommand())) {
            // Session attributes from handshake
            Map<String, Object> sessionAttrs = acc.getSessionAttributes();
            String token = (sessionAttrs != null)
                ? (String) sessionAttrs.get("jwt") : null;

            // Also allow sending token in CONNECT frame headers
            if (token == null) {
                String authHeader = acc.getFirstNativeHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }

            if (token == null || !jwtService.isValid(token)) {
                throw new MessagingException("Unauthorized");
            }

            Long userId = jwtService.extractUserId(token);
            UserDetails ud = userDetailsService.loadById(userId);

            UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(
                    ud, null, ud.getAuthorities());
            acc.setUser(auth);
            acc.setLeaveMutable(true);
        }
        return message;
    }
}
