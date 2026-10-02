package com.codecollab.client.service;

import com.codecollab.client.config.ClientConfig;
import com.codecollab.client.util.JsonMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.application.Platform;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * STOMP-over-SockJS WebSocket client using OkHttp.
 *
 * Protocol:
 *  1. Connect to ws://host/ws/{server}/{session}/websocket?token=JWT
 *  2. Wait for SockJS open frame "o"
 *  3. Send STOMP CONNECT frame (wrapped in SockJS array)
 *  4. Wait for STOMP CONNECTED
 *  5. subscribe() / send() for real-time messaging
 */
public class WebSocketService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);
    private static final WebSocketService INSTANCE = new WebSocketService();

    private final OkHttpClient             okHttp       = new OkHttpClient();
    private final AtomicInteger            subCounter   = new AtomicInteger(0);
    private final Map<String, List<Consumer<String>>> subscriptions = new ConcurrentHashMap<>();

    private WebSocket webSocket;
    private volatile boolean connected = false;

    // callbacks
    private Runnable         onConnected;
    private Consumer<String> onError;
    private Runnable         onDisconnected;

    private WebSocketService() {}

    public static WebSocketService getInstance() { return INSTANCE; }

    // ── public API ─────────────────────────────────────────────────────────

    public void connect(String jwt, Runnable onConnected, Consumer<String> onError, Runnable onDisconnected) {
        if (connected) return;
        this.onConnected    = onConnected;
        this.onError        = onError;
        this.onDisconnected = onDisconnected;

        String server  = String.format("%03d", new Random().nextInt(1000));
        String session = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String url     = String.format("%s/%s/%s/websocket?token=%s",
                ClientConfig.WS_BASE_URL, server, session, jwt);

        log.debug("Connecting to {}", url);
        Request req = new Request.Builder().url(url).build();
        webSocket = okHttp.newWebSocket(req, new WsListener());
    }

    public String subscribe(String destination, Consumer<String> handler) {
        String subId = "sub-" + subCounter.getAndIncrement();
        subscriptions.computeIfAbsent(destination, k -> Collections.synchronizedList(new ArrayList<>()))
                     .add(handler);
        sendStompFrame("SUBSCRIBE", Map.of("destination", destination, "id", subId), "");
        return subId;
    }

    public void send(String destination, String jsonBody) {
        sendStompFrame("SEND", Map.of(
            "destination", destination,
            "content-type", "application/json"
        ), jsonBody);
    }

    public void disconnect() {
        if (webSocket != null) {
            try { sendStompFrame("DISCONNECT", Map.of(), ""); } catch (Exception ignored) {}
            webSocket.close(1000, "Client logout");
        }
        connected = false;
        subscriptions.clear();
    }

    public boolean isConnected() { return connected; }

    // ── internals ──────────────────────────────────────────────────────────

    private void sendStompFrame(String command, Map<String, String> headers, String body) {
        if (webSocket == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append(command).append("\n");
        headers.forEach((k, v) -> sb.append(k).append(":").append(v).append("\n"));
        sb.append("\n").append(body).append("\u0000");
        String sockjsMsg = "[" + JsonMapper.toJson(sb.toString()) + "]";
        webSocket.send(sockjsMsg);
    }

    private void handleSockJsMessage(String text) {
        if ("o".equals(text)) {
            // SockJS open → send STOMP CONNECT
            sendStompFrame("CONNECT", Map.of(
                "accept-version", "1.1,1.0",
                "heart-beat",     "10000,10000"
            ), "");
        } else if (text.startsWith("a")) {
            try {
                List<String> frames = JsonMapper.fromJson(text.substring(1),
                    new TypeReference<List<String>>() {});
                frames.forEach(this::processStompFrame);
            } catch (Exception e) {
                log.error("Failed to parse SockJS message", e);
            }
        } else if (text.startsWith("c")) {
            connected = false;
            if (onDisconnected != null) Platform.runLater(onDisconnected);
        }
        // "h" = heartbeat, ignored
    }

    private void processStompFrame(String frame) {
        if (frame.isBlank()) return;
        int firstNewline = frame.indexOf('\n');
        String command = firstNewline < 0 ? frame.trim() : frame.substring(0, firstNewline).trim();

        if ("CONNECTED".equals(command)) {
            connected = true;
            log.info("STOMP connected");
            if (onConnected != null) Platform.runLater(onConnected);

        } else if ("MESSAGE".equals(command)) {
            int bodyStart = frame.indexOf("\n\n");
            if (bodyStart < 0) return;
            String headerBlock = frame.substring(0, bodyStart);
            String body = frame.substring(bodyStart + 2).replace("\u0000", "");

            String destination = Arrays.stream(headerBlock.split("\n"))
                .filter(l -> l.startsWith("destination:"))
                .map(l -> l.substring("destination:".length()).trim())
                .findFirst().orElse(null);

            if (destination != null) {
                List<Consumer<String>> handlers = subscriptions.get(destination);
                if (handlers != null) {
                    final String finalBody = body;
                    handlers.forEach(h -> Platform.runLater(() -> h.accept(finalBody)));
                }
            }

        } else if ("ERROR".equals(command)) {
            log.error("STOMP ERROR: {}", frame);
            if (onError != null) Platform.runLater(() -> onError.accept("WebSocket error"));
        }
    }

    // ── listener ───────────────────────────────────────────────────────────

    private class WsListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket ws, Response response) {
            log.debug("WebSocket opened");
        }

        @Override
        public void onMessage(WebSocket ws, String text) {
            handleSockJsMessage(text);
        }

        @Override
        public void onFailure(WebSocket ws, Throwable t, Response response) {
            log.error("WebSocket failure: {}", t.getMessage());
            connected = false;
            if (onError != null) Platform.runLater(() -> onError.accept(t.getMessage()));
        }

        @Override
        public void onClosed(WebSocket ws, int code, String reason) {
            log.info("WebSocket closed: {} {}", code, reason);
            connected = false;
            if (onDisconnected != null) Platform.runLater(onDisconnected);
        }
    }
}
