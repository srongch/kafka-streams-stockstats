package me.sronglong.pricealert.model;

import java.util.*;


public class UserPriceSettingStats {

    String pushToken;
    HashMap<String, HashMap<String, Set<Double>>> priceList = new HashMap<>(); // exchage, ticker,priceList

    @Override
    public String toString() {
        return "Setting{" +
                "pushToken='" + pushToken + '\'' +
                ", priceList='" + priceList + '\'' +
                '}';
    }

    public String getPushToken() {
        return pushToken;
    }

    public HashMap<String, HashMap<String, Set<Double>>> getPriceList() {
        return priceList;
    }

    public UserPriceSettingStats add(UserAlert userAler) {

        System.out.println(userAler.toString());

        if (this.pushToken == null){
            this.pushToken = userAler.pushToken;
        }

        if (this.priceList.containsKey(userAler.exchange)){
            HashMap<String, Set<Double>> priceArray = priceList.get(userAler.exchange);

            if (priceArray.containsKey(userAler.ticker)){ // get ech ticker
                Set<Double> userPrice = priceArray.get(userAler.ticker);
                userPrice.add(userAler.price);
                priceArray.replace(userAler.ticker,userPrice);
                this.priceList.replace(userAler.exchange,priceArray);
            }else {
                Set<Double> hash_Set = new HashSet<Double>();
                hash_Set.add(userAler.price);
                priceArray.put(userAler.ticker,hash_Set);
                this.priceList.replace(userAler.exchange,priceArray);
            }

        }else {
            Set<Double> hash_Set = new HashSet<Double>();
            hash_Set.add(userAler.price);
            HashMap<String, Set<Double>> priceArray = new HashMap<>();
            priceArray.put(userAler.ticker,hash_Set);
            this.priceList.put(userAler.exchange,priceArray);
        }



        return this;
    }

    public UserPriceSettingStats computeAvgPrice() {
      //  this.avgPrice = this.sumPrice / this.countTrades;
        return this;
    }


}
