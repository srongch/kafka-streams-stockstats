package me.sronglong.pricealert.model;

import java.util.*;


// process table of exchange and pair


public class ExchangePairStats {

    String exchange = "";
    Set<String> pairList = new HashSet<String>();


    @Override
    public String toString() {
        return "ExchangePair{" +
                "exchange='" + exchange + '\'' +
                ", pairList='" + pairList + '\'' +
                '}';
    }

    public String getExchange() {
        return exchange;
    }

    public boolean checkIfPairExist(UserAlert userAlert){
        System.out.println(pairList.contains(userAlert.ticker));
        return pairList.contains(userAlert.ticker);
    }

    public UserAlert addPair(UserAlert userAlert){
        if (!pairList.contains(userAlert.ticker)){
            pairList.add(userAlert.ticker);
        }

        return new UserAlert(userAlert.exchange ,userAlert.ticker ,0.0,"");
    }

    public void addNewPair(UserAlert userAlert){
        this.exchange = userAlert.exchange;
        this.pairList.add(userAlert.ticker);
        System.out.println("inside " + userAlert.toString());
        System.out.println("inside " + this.toString());
//        return new UserAlert(userAlert.exchange ,userAlert.ticker ,0.0,"");
    }



}
