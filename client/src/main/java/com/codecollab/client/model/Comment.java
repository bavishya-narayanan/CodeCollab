package com.codecollab.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Comment {

    private Long          id;
    private Long          snippetId;
    private Long          authorId;
    private String        authorUsername;
    private String        content;
    private Long          parentId;
    private List<Comment> replies;
    private boolean       edited;
    private boolean       helpful;
    private boolean       deleted;
    private Instant       createdAt;
    private Instant       editedAt;

    public Comment() {}

    public Long   getId()                   { return id; }
    public void   setId(Long id)            { this.id = id; }

    public Long   getSnippetId()                    { return snippetId; }
    public void   setSnippetId(Long s)              { this.snippetId = s; }

    public Long   getAuthorId()                     { return authorId; }
    public void   setAuthorId(Long a)               { this.authorId = a; }

    public String getAuthorUsername()               { return authorUsername; }
    public void   setAuthorUsername(String a)       { this.authorUsername = a; }

    public String getContent()                      { return content; }
    public void   setContent(String c)              { this.content = c; }

    public Long   getParentId()                     { return parentId; }
    public void   setParentId(Long p)               { this.parentId = p; }

    public List<Comment> getReplies()               { return replies; }
    public void          setReplies(List<Comment> r){ this.replies = r; }

    public boolean isEdited()                       { return edited; }
    public void    setEdited(boolean e)             { this.edited = e; }

    public boolean isHelpful()                      { return helpful; }
    public void    setHelpful(boolean h)            { this.helpful = h; }

    public boolean isDeleted()                      { return deleted; }
    public void    setDeleted(boolean d)            { this.deleted = d; }

    public Instant getCreatedAt()                   { return createdAt; }
    public void    setCreatedAt(Instant c)          { this.createdAt = c; }

    public Instant getEditedAt()                    { return editedAt; }
    public void    setEditedAt(Instant e)           { this.editedAt = e; }
}
