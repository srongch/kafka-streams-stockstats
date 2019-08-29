package me.sronglong.pricealert.model;


public class CrytoPrice {

    String exchange;
    String ticker;
    double price;
    long eventTime;
    int count; // count


    public CrytoPrice(){

    }

    public CrytoPrice(String exchange, String ticker, double price, long eventTime) {
        this.exchange = exchange;
        this.ticker = ticker;
        this.price = price;
        this.eventTime = eventTime;
        this.count = 0;
    }
    public String getExchange() {
        return exchange;
    }

    public String getTicker() {
        return ticker;
    }

    public CrytoPrice add(CrytoPrice trade) {


        if (trade.exchange == null || trade.ticker == null)
            throw new IllegalArgumentException("Invalid trade to aggregate: " + trade.toString());

        if (this.exchange == null)
            this.exchange = trade.exchange;
        if (this.ticker == null)
            this.ticker = trade.ticker;

        this.count = this.count+1;
//        this.price = this.price + trade.price;
        this.price = trade.price;
        this.eventTime = trade.eventTime;
        System.out.println(this.toString());
        return this;
    }

    public CrytoPrice computeAvgPrice() {
      //  this.price = this.price / this.count;
        return this;
    }

    public double getPrice() {
        return price;
    }

    public long getEventTime() {
        return eventTime;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "exchange='" + exchange + '\'' +
                ", ticker='" + ticker + '\'' +
                ", price=" + price +
                ", eventTime=" + eventTime +
                '}';
    }
}
