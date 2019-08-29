package me.sronglong.pricealert.model;

import java.math.BigInteger;
import java.util.List;


public class Message {

    String pushMessages;
    String pushToken;
    long timeStamp;

    public Message(String pushMessages, String pushToken,long timeStamp) {
        this.pushMessages = pushMessages;
        this.pushToken = pushToken;
        this.timeStamp = timeStamp;
//        this.eventTime = eventTime;
    }

//    public double getPrice() {
//        return price;
//    }\



    @Override
    public String toString() {
        return "Message{" +
                "pushMessages='" + pushMessages + '\'' +
                ", pushToken='" + pushToken + '\'' +
                '}';
    }
}
