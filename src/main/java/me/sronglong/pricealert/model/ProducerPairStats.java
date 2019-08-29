package me.sronglong.pricealert.model;

import java.util.HashSet;
import java.util.Set;


// process table of exchange and pair


public class ProducerPairStats {

    String name = "";
    String exchange = "";
    Set<String> pairList = new HashSet<String>();


    @Override
    public String toString() {
        return "ProducerPairStats{" +
                "name='" + name + '\'' +
                ", pairList='" + pairList + '\'' +
                '}';
    }

    public String getExchange() {
        return exchange;
    }

    public String getName() {
        return name;
    }

    public boolean checkIfPairExist(UserAlert userAlert){
        System.out.println(pairList.contains(userAlert.ticker));
        return pairList.contains(userAlert.ticker);
    }

    public void addPair(ProducerTask producerTask){
        if (!pairList.contains(producerTask.ticker)){
            pairList.add(producerTask.ticker);
        }

//        return new UserAlert(userAlert.exchange ,userAlert.ticker ,0.0,"");
    }

    public ProducerPairStats(ProducerTask producerTask){
        this.name = producerTask.name;
        this.exchange = producerTask.exchange;
        this.pairList.add(producerTask.ticker);
    }



}
