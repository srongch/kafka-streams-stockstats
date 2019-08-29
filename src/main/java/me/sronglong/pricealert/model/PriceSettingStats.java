package me.sronglong.pricealert.model;

import java.util.*;


public class PriceSettingStats {

    String exchange;
    String ticker;
    HashMap<String, HashMap<Double, Set<String>>> priceMap = new HashMap<>();
//    HashMap<Double, String> priceMap = new HashMap<>();

    @Override
    public String toString() {
        return "Setting{" +
                "exchange='" + exchange + '\'' +
                ", ticker='" + ticker + '\'' +
                ", priceMap=" + priceMap +
                '}';
    }

    public String getExchange() {
        return exchange;
    }

    public PriceSettingStats add(UserAlert userAler) {

        System.out.println(userAler.toString());

        if (this.exchange == null){
//            this.exchange = userAler.exchange;
        }

        if (this.ticker == null){
            this.ticker = userAler.ticker;
        }

        if (this.priceMap.containsKey(userAler.exchange)){
            HashMap<Double, Set<String>> priceArray = priceMap.get(userAler.exchange);

            if (priceArray.containsKey(userAler.price)){
                Set<String> userPrice = priceArray.get(userAler.price);
                userPrice.add(userAler.pushToken);
                priceArray.replace(userAler.price,userPrice);
                this.priceMap.replace(userAler.exchange,priceArray);
            }else {
                Set<String> hash_Set = new HashSet<String>();
                hash_Set.add(userAler.pushToken);
                priceArray.put(userAler.price,hash_Set);
                this.priceMap.replace(userAler.exchange,priceArray);
            }

        }else {
            Set<String> hash_Set = new HashSet<String>();
            hash_Set.add(userAler.pushToken);
            HashMap<Double, Set<String>> priceArray = new HashMap<>();
            priceArray.put(userAler.price,hash_Set);
            this.priceMap.put(userAler.exchange,priceArray);
        }


    //    System.out.println(" size of hansmap" + this.priceMap);

        //if (this.priceMap == null){
//            this.priceMap.put(userAler.price,userAler.pushToken);
       // }


//        if (trade.type == null || trade.ticker == null)
//            throw new IllegalArgumentException("Invalid trade to aggregate: " + trade.toString());
//
//        if (this.type == null)
//            this.type = trade.type;
//        if (this.ticker == null)
//            this.ticker = trade.ticker;
//
//        if (!this.type.equals(trade.type) || !this.ticker.equals(trade.ticker))
//            throw new IllegalArgumentException("Aggregating stats for trade type " + this.type + " and ticker " + this.ticker + " but recieved trade of type " + trade.type +" and ticker " + trade.ticker );
//
//        if (countTrades == 0) this.minPrice = trade.price;
//
//        this.countTrades = this.countTrades+1;
//        this.sumPrice = this.sumPrice + trade.price;
//        this.minPrice = this.minPrice < trade.price ? this.minPrice : trade.price;

        return this;
    }

    public PriceSettingStats computeAvgPrice() {
      //  this.avgPrice = this.sumPrice / this.countTrades;
        return this;
    }

    public List<PushMessage> FilterWithPrice(CrytoPrice crytoPrice){

        List listA = new ArrayList();

        if (this.priceMap.containsKey(crytoPrice.exchange)){
            HashMap<Double, Set<String>> priceArray = priceMap.get(crytoPrice.exchange);

            List<Double> allPrice = new ArrayList<Double>(priceArray.keySet());

            allPrice.forEach((temp) -> {
                if (calculatePercentage(crytoPrice.price,temp) < 0.2){
                    Set<String> allUser = priceArray.get(temp);
                    // enhanced for loop also uses an iterator behind the scenes
                    for (String item: allUser) {
                        System.out.println(item);
                        listA.add(new PushMessage("Price : |" +temp +"| " + crytoPrice.ticker+ " on " + crytoPrice.exchange + " is |" + crytoPrice.price,item,crytoPrice.getEventTime()));
                    }
                }

            });

        }

        return listA;

    }

    public double calculatePercentage(double value1, double vale2){
        double daltaValue = Math.abs(value1 - vale2);
        double ziqma = (value1 + vale2)/2;
        return (daltaValue/ziqma) * 100;

    }

}
