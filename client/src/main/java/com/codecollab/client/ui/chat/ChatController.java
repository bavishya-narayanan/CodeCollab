package com.codecollab.client.ui.chat;

import com.codecollab.client.api.ChatApi;
import com.codecollab.client.api.RoomApi;
import com.codecollab.client.config.AppState;
import com.codecollab.client.model.ChatMessage;
import com.codecollab.client.model.Room;
import com.codecollab.client.service.WebSocketService;
import com.codecollab.client.ui.BaseController;
import com.codecollab.client.ui.components.AlertHelper;
import com.codecollab.client.util.JsonMapper;
import com.codecollab.client.util.TimeFormat;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ChatController extends BaseController implements Initializable {

    @FXML private ListView<Room>  roomList;
    @FXML private VBox            messagesBox;
    @FXML private ScrollPane      messagesScroll;
    @FXML private TextField       messageInput;
    @FXML private Button          sendBtn;
    @FXML private Label           roomLabel;
    @FXML private Label           onlineLabel;
    @FXML private TextField       newRoomField;

    private final RoomApi roomApi = new RoomApi();
    private final ChatApi chatApi = new ChatApi();
    private final WebSocketService ws = WebSocketService.getInstance();

    private Room currentRoom;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        roomList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Room r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null : "#" + r.getName() + "  (" + r.getOnlineCount() + ")");
            }
        });
        roomList.setOnMouseClicked(e -> {
            Room r = roomList.getSelectionModel().getSelectedItem();
            if (r != null) joinRoom(r);
        });
        loadRooms();
        connectWebSocket();
    }

    private void connectWebSocket() {
        if (ws.isConnected()) return;
        String token = AppState.getInstance().getToken();
        ws.connect(token,
            () -> roomLabel.setText("WebSocket connected"),
            err -> roomLabel.setText("WS Error: " + err),
            ()  -> roomLabel.setText("Disconnected")
        );
    }

    private void loadRooms() {
        runAsync(
            () -> roomApi.list(),
            rooms -> roomList.setItems(FXCollections.observableArrayList(rooms)),
            err -> AlertHelper.error("Rooms", err.getMessage())
        );
    }

    private void joinRoom(Room room) {
        // Leave previous room
        if (currentRoom != null && ws.isConnected()) {
            ws.send("/app/presence/leave/" + currentRoom.getId(), "");
        }
        currentRoom = room;
        roomLabel.setText("#" + room.getName());
        messagesBox.getChildren().clear();

        // Load history
        runAsync(
            () -> chatApi.roomHistory(room.getId(), 0, 50),
            resp -> {
                List<ChatMessage> msgs = resp.getContent();
                // history is newest-first, display oldest-first
                for (int i = msgs.size() - 1; i >= 0; i--) {
                    addMessageBubble(msgs.get(i));
                }
                scrollToBottom();
            },
            err -> {}
        );

        // Subscribe to live messages
        if (ws.isConnected()) {
            ws.subscribe("/topic/chat/room/" + room.getId(), body -> {
                ChatMessage msg = JsonMapper.fromJson(body, ChatMessage.class);
                addMessageBubble(msg);
                scrollToBottom();
            });
            ws.subscribe("/topic/presence/room/" + room.getId(), body -> {
                // update online count label
                try {
                    var payload = JsonMapper.mapper().readTree(body);
                    long count = payload.path("onlineCount").asLong(0);
                    onlineLabel.setText(count + " online");
                } catch (Exception ignored) {}
            });
            ws.send("/app/presence/join/" + room.getId(), "");
        }
    }

    @FXML private void handleSend() {
        if (currentRoom == null) { AlertHelper.error("Chat", "Please join a room first."); return; }
        String text = messageInput.getText().trim();
        if (text.isBlank()) return;
        if (!ws.isConnected()) { AlertHelper.error("Chat", "Not connected to server."); return; }

        ws.send("/app/chat/room/" + currentRoom.getId(),
            "{\"content\":\"" + text.replace("\"", "\\\"") + "\"}");
        messageInput.clear();
    }

    @FXML private void handleCreateRoom() {
        String name = newRoomField.getText().trim().toLowerCase();
        if (name.isBlank()) return;
        runAsync(
            () -> roomApi.create(name, ""),
            room -> { newRoomField.clear(); loadRooms(); },
            err -> AlertHelper.error("Create room", err.getMessage())
        );
    }

    private void addMessageBubble(ChatMessage msg) {
        Long myId = state().getCurrentUser() != null ? state().getCurrentUser().getId() : null;
        boolean isMine = myId != null && myId.equals(msg.getSenderId());

        VBox bubble = new VBox(2);
        bubble.getStyleClass().add("chat-message-bubble");
        if (isMine) bubble.getStyleClass().add("mine");
        bubble.setMaxWidth(500);

        Label sender = new Label(msg.getSenderUsername() + "  " + TimeFormat.relative(msg.getCreatedAt()));
        sender.getStyleClass().add("label-muted");
        sender.setStyle("-fx-font-size: 10;");

        Label content = new Label(msg.getContent());
        content.setWrapText(true);

        bubble.getChildren().addAll(sender, content);
        bubble.setPadding(new Insets(6, 10, 6, 10));

        HBox wrapper = new HBox(bubble);
        wrapper.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        wrapper.setPadding(new Insets(2, 8, 2, 8));

        messagesBox.getChildren().add(wrapper);
    }

    private void scrollToBottom() {
        messagesScroll.layout();
        messagesScroll.setVvalue(1.0);
    }
}
