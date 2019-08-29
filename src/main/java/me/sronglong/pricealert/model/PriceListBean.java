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

public class PriceListBean {

  private String pushToken;
  private Integer count;
  private List<UserAlert>priceList = new ArrayList();

  public PriceListBean() {}

  public PriceListBean(final UserPriceSettingStats userPriceSettingStats) {

//    this.pushToken = pushToken;
    this.pushToken = userPriceSettingStats.getPushToken();

    HashMap<String, HashMap<String, Set<Double>>> exchangeList = userPriceSettingStats.getPriceList(); // exchage, ticker,priceList

    exchangeList.forEach((exchange,ticker) -> {
      System.out.println("exchange: "+exchange+" ticker:"+ticker);
      ticker.forEach((eachTicker,priceSet) -> {
        System.out.println("eachTicker: "+eachTicker+" priceSet:"+priceSet);
        for (Double price: priceSet) {
          System.out.println("eachPrice: "+price);
//          String exchange, String ticker, double price, String pushToken
          priceList.add(new UserAlert(exchange,eachTicker,price,this.pushToken));
        }
      });
    });
  }

  public String getPushToken() {
    return pushToken;
  }

  public void setPushToken(String pushToken) {
    this.pushToken = pushToken;
  }

  public List<UserAlert> getPriceList() {
    return priceList;
  }

  public Integer getCount() {
    return priceList.size();
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
    return Objects.hash(pushToken);
  }
}
