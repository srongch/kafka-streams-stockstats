package me.sronglong.pricealert.model;

import java.util.List;


public class AlertMessage {

    String exchange;
    String ticker;
    Boolean hasValue;
    long eventTime;
    List<PushMessage> pushMessages;

    public AlertMessage(String exchange, String ticker, Boolean hasValue,long eventTime  ) {
        this.exchange = exchange;
        this.ticker = ticker;
        this.hasValue = hasValue;
        this.eventTime = eventTime;
    }

//    public double getPrice() {
//        return price;
//    }\


    public long getEventTime() {
        return eventTime;
    }

    public String getTicker() {
        return ticker;
    }

    public void setPushMessages(List<PushMessage> pushMessages) {
        this.pushMessages = pushMessages;
    }

    public List<PushMessage> getPushMessages() {
        return pushMessages;
    }

    public Boolean getHasValue() {
        return hasValue;
    }

    public void setHasValue(Boolean hasValue) {
        this.hasValue = hasValue;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "exchange='" + exchange + '\'' +
                ", ticker='" + ticker + '\'' +
                '}';
    }
}
