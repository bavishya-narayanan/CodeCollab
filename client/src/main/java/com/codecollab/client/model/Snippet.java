package com.codecollab.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Snippet {

    private Long        id;
    private String      title;
    private String      description;
    private String      language;
    private String      content;
    private Long        authorId;
    private String      authorUsername;
    private List<Tag>   tags;
    private int         viewCount;
    private long        voteScore;
    private int         upvoteCount;
    private int         downvoteCount;
    private long        commentCount;
    private String      myVote;      // "UP", "DOWN", or null
    private boolean     deleted;
    private Instant     createdAt;
    private Instant     updatedAt;

    public Snippet() {}

    public Long   getId()                   { return id; }
    public void   setId(Long id)            { this.id = id; }

    public String getTitle()                { return title; }
    public void   setTitle(String t)        { this.title = t; }

    public String getDescription()          { return description; }
    public void   setDescription(String d)  { this.description = d; }

    public String getLanguage()             { return language; }
    public void   setLanguage(String l)     { this.language = l; }

    public String getContent()              { return content; }
    public void   setContent(String c)      { this.content = c; }

    public Long   getAuthorId()             { return authorId; }
    public void   setAuthorId(Long a)       { this.authorId = a; }

    public String getAuthorUsername()                 { return authorUsername; }
    public void   setAuthorUsername(String a)         { this.authorUsername = a; }

    public List<Tag> getTags()              { return tags; }
    public void      setTags(List<Tag> t)   { this.tags = t; }

    public int  getViewCount()              { return viewCount; }
    public void setViewCount(int v)         { this.viewCount = v; }

    public long getVoteScore()              { return voteScore; }
    public void setVoteScore(long v)        { this.voteScore = v; }

    public int  getUpvoteCount()            { return upvoteCount; }
    public void setUpvoteCount(int u)       { this.upvoteCount = u; }

    public int  getDownvoteCount()          { return downvoteCount; }
    public void setDownvoteCount(int d)     { this.downvoteCount = d; }

    public long getCommentCount()           { return commentCount; }
    public void setCommentCount(long c)     { this.commentCount = c; }

    public String getMyVote()               { return myVote; }
    public void   setMyVote(String v)       { this.myVote = v; }

    public boolean isDeleted()              { return deleted; }
    public void    setDeleted(boolean d)    { this.deleted = d; }

    public Instant getCreatedAt()           { return createdAt; }
    public void    setCreatedAt(Instant c)  { this.createdAt = c; }

    public Instant getUpdatedAt()           { return updatedAt; }
    public void    setUpdatedAt(Instant u)  { this.updatedAt = u; }

    @Override public String toString() { return title != null ? title : "Snippet #" + id; }
}
