package me.sronglong.pricealert.Transformer;

import me.sronglong.pricealert.model.Producer;
import me.sronglong.pricealert.model.ProducerTask;
import me.sronglong.pricealert.model.UserAlert;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

//state storename : exchange_pair_state_store

public class ProducerTaskTransformer implements ValueTransformer<UserAlert, ProducerTask> {

    private KeyValueStore<String, Producer> stateStore;
    private final String storeName;
    private ProcessorContext context;

    public ProducerTaskTransformer(String storeName) {
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
    public ProducerTask transform(UserAlert value) {
    //    System.out.println("Stream processing started for " + value.toString());

//        Producer userAlert = new Producer();
        // Get the values for all of the keys available in this application instance

        KeyValueIterator<String, Producer> range = stateStore.all(); // list of all producer
        List<Producer> producers = new ArrayList();

        while (range.hasNext()) {
            KeyValue<String, Producer> next = range.next();
            if (next.value.getExchange().equals(value.getExchange())){
                producers.add(next.value);
                System.out.println("Proder for exchage : " + next.value.getExchange() + ": " + next.value.toString());
            }
        }

        System.out.println("Total producer is :" + producers.size());

        Producer eachProducer = new Producer();
        //Sort Producer task ids in reverse order

        if (producers.size() > 0){
           if (producers.size() > 1) Collections.sort(producers);

           eachProducer = producers.get(0);
           eachProducer.addTask();
            stateStore.put(eachProducer.getTicker(), eachProducer);
            System.out.println("Select producer is :" + eachProducer.toString());
           return new ProducerTask(eachProducer.getTicker(),eachProducer.getExchange(),value.getTicker());

        }else {
            // no producer
            return new ProducerTask();
        }







//        Producer producer = stateStore.all(); // get all
//        System.out.println("Procer store is :" + producer);
//
//        if (producer != null){
//            System.out.println("already have in the list");
//        }else {
//            stateStore.put(value.getTicker(), value);
//            producer = value;
//        }


    }

    @Override
    public void close() {
        //no-op
    }
}
