package me.sronglong.pricealert.model;



//         "key" : "producer01",
//        "exchange" : "binance",
//        "status" : "online",
//        "tasks" : 0
public class ProducerTask {

    String name;
    String exchange;
    String ticker;

    public String getTicker() {
        return ticker;
    }

    public ProducerTask(){
        this.name = "";
        this.exchange = "";
        this.ticker ="";
    }

    public ProducerTask(String name, String exchange, String ticker) {
        this.name = name;
        this.ticker = ticker;
        this.exchange = exchange;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ProducerTask{" +
                "name='" + name + '\'' +
                ", exchange='" + exchange + '\'' +
                ", ticker=" + ticker +
                '}';
    }
}
