package me.sronglong.pricealert.model;


public class PushMessage {

    String message;
    String pushToken;
    long eventTime;

    public PushMessage(String message, String pushToken,long eventTime) {
        this.message = message;
        this.pushToken = pushToken;
        this.eventTime = eventTime;
    }

    public String getMessage() {
        return message;
    }

    public String getPushToken() {
        return pushToken;
    }

    public long getEventTime() {
        return eventTime;
    }

    @Override
    public String toString() {
        return "PushMessage{" +
                "message='" + message + '\'' +
                ", pushToken='" + pushToken + '\'' +
                '}';
    }
}
