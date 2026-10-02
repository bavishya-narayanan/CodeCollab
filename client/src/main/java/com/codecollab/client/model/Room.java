package com.codecollab.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Room {
    private Long    id;
    private String  name;
    private String  description;
    private Long    createdBy;
    private boolean defaultRoom;
    private long    onlineCount;
    private Instant createdAt;

    public Room() {}

    public Long   getId()                   { return id; }
    public void   setId(Long id)            { this.id = id; }

    public String getName()                 { return name; }
    public void   setName(String n)         { this.name = n; }

    public String getDescription()          { return description; }
    public void   setDescription(String d)  { this.description = d; }

    public Long   getCreatedBy()            { return createdBy; }
    public void   setCreatedBy(Long c)      { this.createdBy = c; }

    public boolean isDefaultRoom()                  { return defaultRoom; }
    public void    setDefaultRoom(boolean d)        { this.defaultRoom = d; }

    public long getOnlineCount()                    { return onlineCount; }
    public void setOnlineCount(long o)              { this.onlineCount = o; }

    public Instant getCreatedAt()                   { return createdAt; }
    public void    setCreatedAt(Instant c)          { this.createdAt = c; }

    @Override public String toString() { return "#" + name; }
}
