package me.sronglong.pricealert.Transformer;

import me.sronglong.pricealert.model.Producer;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Objects;

//state storename : exchange_pair_state_store

public class ProducerTransformer implements ValueTransformer<Producer, Producer> {

    private KeyValueStore<String, Producer> stateStore;
    private final String storeName;
    private ProcessorContext context;

    public ProducerTransformer(String storeName) {
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
    public Producer transform(Producer value) {
        System.out.println("Stream processing started for " + value.toString());

//        Producer userAlert = new Producer();

        Producer producer = stateStore.get(value.getTicker()); // get producer by name
        System.out.println("Procer store is :" + producer);

        if (producer != null){
            System.out.println("already have in the list");
        }else {
            stateStore.put(value.getTicker(), value);
            producer = value;
        }

       return producer;
    }

    @Override
    public void close() {
        //no-op
    }
}
