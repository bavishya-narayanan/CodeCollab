package com.codecollab.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private Long    id;
    private String  username;
    private String  email;
    private String  role;
    private String  status;
    private Instant createdAt;

    public User() {}

    public Long    getId()                { return id; }
    public void    setId(Long id)         { this.id = id; }

    public String  getUsername()              { return username; }
    public void    setUsername(String u)      { this.username = u; }

    public String  getEmail()                 { return email; }
    public void    setEmail(String e)         { this.email = e; }

    public String  getRole()                  { return role; }
    public void    setRole(String r)          { this.role = r; }

    public String  getStatus()                { return status; }
    public void    setStatus(String s)        { this.status = s; }

    public Instant getCreatedAt()             { return createdAt; }
    public void    setCreatedAt(Instant c)    { this.createdAt = c; }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    @Override public String toString() { return username; }
}
