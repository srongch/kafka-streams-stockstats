package me.sronglong.pricealert.Transformer;

import me.sronglong.pricealert.model.ExchangePairStats;
import me.sronglong.pricealert.model.UserAlert;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Objects;

//state storename : exchange_pair_state_store

public class ExchangePairTransformer implements ValueTransformer<UserAlert, UserAlert> {

    private KeyValueStore<String, ExchangePairStats> stateStore;
    private final String storeName;
    private ProcessorContext context;

    public ExchangePairTransformer(String storeName) {
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
    public UserAlert transform(UserAlert value) {
//        RewardAccumulator rewardAccumulator = RewardAccumulator.builder(value).build();
        System.out.println("Stream processing started for " + value.toString());
        System.out.println("Exchange for " + value.getExchange());
//        // Get the values for all of the keys available in this application instance
//        KeyValueIterator<String, ExchangePairStats> range = stateStore.all();
//        while (range.hasNext()) {
//            KeyValue<String, ExchangePairStats> next = range.next();
//            System.out.println("Transform for " + next.key + ": " + next.value.toString());
//        }

        UserAlert userAlert = new UserAlert();

        ExchangePairStats  exchangePairStats = stateStore.get(value.getExchange()); // get pair from exchange

        if (exchangePairStats !=null && exchangePairStats.getExchange().length() > 0 ) {
            if (exchangePairStats.checkIfPairExist(value)) {
                System.out.println("has pair");
                return userAlert;
            } else {
                System.out.println("exchange exit exist");
                System.out.println(exchangePairStats);
                UserAlert userAlet = exchangePairStats.addPair(value);
                stateStore.put(value.getExchange(), exchangePairStats);
                System.out.println(userAlet);
                System.out.println(exchangePairStats);
                return userAlet;
            }
        }else {
            System.out.println("no pair");
            exchangePairStats = new ExchangePairStats();
            exchangePairStats.addNewPair(value);
            System.out.println("New alert");
            System.out.println(value);
            System.out.println("New exchangePairStats");
            System.out.println(exchangePairStats);
            stateStore.put(value.getExchange(), exchangePairStats);
            return value;
        }
    }

    @Override
    public void close() {
        //no-op
    }
}
