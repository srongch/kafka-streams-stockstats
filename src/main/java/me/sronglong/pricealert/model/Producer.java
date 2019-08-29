package me.sronglong.pricealert.model;



//         "key" : "producer01",
//        "exchange" : "binance",
//        "status" : "online",
//        "tasks" : 0
public class Producer implements Comparable<Producer>{

    String ticker;
    String exchange;
    String status;
    Integer tasks;

    public String getTicker() {
        return ticker;
    }

    public String getExchange() {
        return exchange;
    }

    public Producer(){
        this.ticker = "";
        this.exchange = "";
        this.status ="";
        this.tasks = 0;
    }

    public Producer(String ticker, String exchange, String status, Integer tasks) {
        this.exchange = exchange;
        this.ticker = ticker;
        this.status = status;
        this.tasks = tasks;
    }

    public void addTask(){
        this.tasks = this.tasks  + 1;
    }


    @Override
    public String toString() {
        return "Producer{" +
                "exchange='" + exchange + '\'' +
                ", ticker='" + ticker + '\'' +
                ", status=" + status +
                ", tasks=" + tasks +
                '}';
    }

    @Override
    public int compareTo(Producer o) {
        return this.tasks.compareTo(o.tasks);
    }
}
