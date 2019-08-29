package me.sronglong.pricealert.Transformer;

import me.sronglong.pricealert.model.ProducerPairStats;
import me.sronglong.pricealert.model.ProducerTask;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Objects;

//state storename : exchange_pair_state_store

public class ProducerPairTransformer implements ValueTransformer<ProducerTask, ProducerTask> {

    private KeyValueStore<String, ProducerPairStats> stateStore;
    private final String storeName;
    private ProcessorContext context;

    public ProducerPairTransformer(String storeName) {
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
    public ProducerTask transform(ProducerTask value) {
        System.out.println("producerPairStats processing started for " + value.toString());

//        KeyValueIterator<String, ProducerPairStats> range = stateStore.all(); // list of all producer
//
//        while (range.hasNext()) {
//            KeyValue<String, ProducerPairStats> next = range.next();
//
//                System.out.println("ProducerPairStats for exchage : " + next.value.getExchange() + ": " + next.value.toString());
//
//        }

        ProducerPairStats producerPairStats = stateStore.get(value.getName()); //get producer task list by name
        System.out.println("producerPairStats " + producerPairStats);
        if (producerPairStats != null){
            producerPairStats.addPair(value);
            System.out.println("pair added" + producerPairStats) ;
        }else {
            System.out.println("process null") ;
            producerPairStats = new ProducerPairStats(value);
            System.out.println("pair added" + producerPairStats) ;
        }
        stateStore.put(producerPairStats.getName(), producerPairStats);

        return value;


    }

    @Override
    public void close() {
        //no-op
    }
}
