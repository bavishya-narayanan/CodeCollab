package com.codecollab.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                    ServerHttpResponse response,
                                    WebSocketHandler handler,
                                    Map<String, Object> attributes) {
        String token = null;

        // 1) Try Authorization header
        String auth = request.getHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
        }

        // 2) Fallback to query param ?token=xxx
        if (token == null) {
            String query = request.getURI().getQuery();
            if (query != null) {
                for (String p : query.split("&")) {
                    if (p.startsWith("token=")) {
                        token = p.substring(6);
                        break;
                    }
                }
            }
        }

        if (token == null) {
            log.debug("WebSocket handshake missing token");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put("jwt", token);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                                ServerHttpResponse response,
                                WebSocketHandler handler,
                                Exception exception) { }
}
