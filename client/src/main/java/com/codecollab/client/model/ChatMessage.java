package com.codecollab.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {

    private Long        id;
    private Long        roomId;
    private Long        senderId;
    private String      senderUsername;
    private Long        recipientId;
    private String      recipientUsername;
    private String      messageType;
    private String      content;
    private boolean     deleted;
    private Instant     createdAt;

    public ChatMessage() {}

    public Long   getId()                   { return id; }
    public void   setId(Long id)            { this.id = id; }

    public Long   getRoomId()               { return roomId; }
    public void   setRoomId(Long r)         { this.roomId = r; }

    public Long   getSenderId()             { return senderId; }
    public void   setSenderId(Long s)       { this.senderId = s; }

    public String getSenderUsername()               { return senderUsername; }
    public void   setSenderUsername(String s)       { this.senderUsername = s; }

    public Long   getRecipientId()                  { return recipientId; }
    public void   setRecipientId(Long r)            { this.recipientId = r; }

    public String getRecipientUsername()            { return recipientUsername; }
    public void   setRecipientUsername(String r)    { this.recipientUsername = r; }

    public String getMessageType()                  { return messageType; }
    public void   setMessageType(String m)          { this.messageType = m; }

    public String getContent()                      { return content; }
    public void   setContent(String c)              { this.content = c; }

    public boolean isDeleted()                      { return deleted; }
    public void    setDeleted(boolean d)            { this.deleted = d; }

    public Instant getCreatedAt()                   { return createdAt; }
    public void    setCreatedAt(Instant c)          { this.createdAt = c; }
}
