package com.codecollab.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Tag {
    private Long   id;
    private String name;

    public Tag() {}
    public Tag(String name) { this.name = name; }

    public Long   getId()           { return id; }
    public void   setId(Long id)    { this.id = id; }

    public String getName()             { return name; }
    public void   setName(String name)  { this.name = name; }

    @Override public String toString() { return name; }
}
