package com.codecollab.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Wrapper matching the backend ApiResponse<T> envelope. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse<T> {

    private boolean success;
    private String  message;
    private T       data;
    private String  code;

    public ApiResponse() {}

    public boolean isSuccess()          { return success; }
    public void    setSuccess(boolean s){ this.success = s; }

    public String getMessage()          { return message; }
    public void   setMessage(String m)  { this.message = m; }

    public T   getData()                { return data; }
    public void setData(T data)         { this.data = data; }

    public String getCode()             { return code; }
    public void   setCode(String code)  { this.code = code; }
}
