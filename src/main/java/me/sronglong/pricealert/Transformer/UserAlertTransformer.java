package me.sronglong.pricealert.Transformer;

import me.sronglong.pricealert.model.AlertMessage;
import me.sronglong.pricealert.model.CrytoPrice;
import me.sronglong.pricealert.model.PriceSettingStats;
import me.sronglong.pricealert.model.PushMessage;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.List;
import java.util.Objects;


public class UserAlertTransformer implements ValueTransformer<CrytoPrice, AlertMessage> {

    private KeyValueStore<String, PriceSettingStats> stateStore;
    private final String storeName;
    private ProcessorContext context;

    public UserAlertTransformer(String storeName) {
        Objects.requireNonNull(storeName,"Store Name can't be null");
        this.storeName = storeName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;
        stateStore = (KeyValueStore) this.context.getStateStore(storeName);
    }

    @Override
    public AlertMessage transform(CrytoPrice value) {
//        RewardAccumulator rewardAccumulator = RewardAccumulator.builder(value).build();
        System.out.println("Stream processing started for " + value.toString());
        AlertMessage message = new AlertMessage(value.getExchange(),value.getTicker(),false,value.getEventTime());

        PriceSettingStats  priceSettingStats = stateStore.get(value.getTicker());

        // Get the values for all of the keys available in this application instance
//        KeyValueIterator<String, PriceSettingStats> range = stateStore.all();
//        while (range.hasNext()) {
//            KeyValue<String, PriceSettingStats> next = range.next();
//     //       System.out.println("Transform for " + next.key + ": " + next.value.toString());
//        }

        if (priceSettingStats != null ) {
        //    System.out.println(" Process of value " + priceSettingStats.toString());

            List <PushMessage> pushList = priceSettingStats.FilterWithPrice(value);

            if (pushList.size() > 0){
          //      System.out.println("has value" + priceSettingStats.toString());
                message.setHasValue(true);
                message.setPushMessages(pushList);
            }

        }else {
            System.out.println("no statestore");
        }

        return message;
    }

//    @Override
    @SuppressWarnings("deprecation")
//    public RewardAccumulator punctuate(long timestamp) {
//        return null;  //no-op null values not forwarded.
//    }

    @Override
    public void close() {
        //no-op
    }
}
