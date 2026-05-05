package com.chatservice.global;

public enum Messages {
    CS001(Constants._RESOURCE_NOT_FOUND,"Chat ID not found"),
    CS002(Constants._SERVICE_UNAVAILABLE,"Service unavailable"),
    CS003(Constants._BAD_REQUEST,"Invalid request User ID is not provided"),
    CS004(Constants._INTERNAL_SERVER_ERROR,"Internal server error"),
    CS005(Constants._FORBIDDEN,"Chat Room is full"),
    CS006(Constants._FORBIDDEN,"User is already in a chat room");
    
    private final int statusCode;
     Messages(int statusCode, String message){
        this.statusCode = statusCode;
        this.message = message;
    }
    private final String message;
    public String getMessage() {
        return message;
    }
    public int getStatusCode() {
        return statusCode;
    }
}
