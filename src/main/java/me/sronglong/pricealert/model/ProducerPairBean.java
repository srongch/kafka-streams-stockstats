/*
 * Copyright Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.sronglong.pricealert.model;

import java.util.*;

public class ProducerPairBean {

  private String name;
  private String exchange;
  private Set<String> pairList = new HashSet<String>();

  public ProducerPairBean() {}

  public ProducerPairBean(final ProducerPairStats producerPairStats) {

    this.name = producerPairStats.name;
    this.exchange = producerPairStats.exchange;
    this.pairList = producerPairStats.pairList;

//    this.pushToken = pushToken;
//    this.pushToken = userPriceSettingStats.getPushToken();
//
//    HashMap<String, HashMap<String, Set<Double>>> exchangeList = userPriceSettingStats.getPriceList(); // exchage, ticker,priceList
//
//    exchangeList.forEach((exchange,ticker) -> {
//      System.out.println("exchange: "+exchange+" ticker:"+ticker);
//      ticker.forEach((eachTicker,priceSet) -> {
//        System.out.println("eachTicker: "+eachTicker+" priceSet:"+priceSet);
//        for (Double price: priceSet) {
//          System.out.println("eachPrice: "+price);
////          String exchange, String ticker, double price, String pushToken
//          priceList.add(new UserAlert(exchange,eachTicker,price,this.pushToken));
//        }
//      });
//    });
  }

  public String getName() {
    return name;
  }

  public String getExchange() {
    return exchange;
  }

  public Set<String> getPairList() {
    return pairList;
  }

  @Override
  public String toString() {
    return "SongBean{" +
           '}';
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
//    final SongBean that = (SongBean) o;
//    return Objects.equals(artist, that.artist) &&
//           Objects.equals(album, that.album) &&
//           Objects.equals(name, that.name);
    return  false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
