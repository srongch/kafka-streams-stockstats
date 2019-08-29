package me.sronglong.pricealert.model;


public class UserAlert {

    String exchange;
    String ticker;
    double price;
    String pushToken;

    public UserAlert(){
        this.exchange = "";
        this.ticker = "";
        this.price = 0.0;
        this.pushToken = "";
    }

    public UserAlert(String exchange, String ticker, double price, String pushToken) {
        this.exchange = exchange;
        this.ticker = ticker;
        this.price = price;
        this.pushToken = pushToken;
    }

    public String getPushToken() {
        return pushToken;
    }

    public String getExchange() {
        return exchange;
    }

    public double getPrice() {
        return price;
    }

    public String getTicker() {
        return ticker;
    }

    @Override
    public String toString() {
        return "UserAlert{" +
                "exchange='" + exchange + '\'' +
                ", ticker='" + ticker + '\'' +
                ", price=" + price +
                ", notification=" + pushToken +
                '}';
    }
}
