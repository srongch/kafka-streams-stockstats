package me.sronglong.pricealert;

import me.sronglong.pricealert.serde.JsonDeserializer;
import me.sronglong.pricealert.serde.JsonSerializer;
import me.sronglong.pricealert.serde.WrapperSerde;
import me.sronglong.pricealert.Transformer.ExchangePairTransformer;
import me.sronglong.pricealert.Transformer.ProducerPairTransformer;
import me.sronglong.pricealert.Transformer.ProducerTaskTransformer;
import me.sronglong.pricealert.Transformer.ProducerTransformer;
import me.sronglong.pricealert.model.*;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.state.*;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;


public class ExchangePairProcessor {

    static final String DEFAULT_HOST = "localhost";

    public static void main(String[] args) throws Exception {

        if (args.length == 0 || args.length > 2) {
            throw new IllegalArgumentException("usage: ... <portForRestEndPoint> [<bootstrap.servers> (optional)]");
        }

        Properties props;
        if (args.length==1)
            props = LoadConfigs.loadConfig(args[0]);
        else
            props = LoadConfigs.loadConfig();

        final int port = Integer.parseInt(args[0]);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092,127.0.0.1:9093,127.0.0.1:9094");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "exchange_pair_processor");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, UserAlertSerde.class.getName());
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);
        // setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
        // Note: To re-run the demo, you need to use the offset reset tool:
        // https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams+Application+Reset+Tool
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, DEFAULT_HOST + ":" + port);



        StreamsBuilder builder = new StreamsBuilder();
        Serde<String> stringSerde = Serdes.String();


        KeyValueBytesStoreSupplier storeSupplier = Stores.persistentKeyValueStore(Constants.EXCHANGE_PAIR_STATE_STORES);
        StoreBuilder<KeyValueStore<String, ExchangePairStats>> storeBuilder = Stores.keyValueStoreBuilder(storeSupplier, Serdes.String(), new ExchangePairStatsSerde());

        KeyValueBytesStoreSupplier storeProducer  = Stores.persistentKeyValueStore(Constants.PRODUCER_LIST_STATE_STORES);
        StoreBuilder<KeyValueStore<String, Producer>> producerBuilder = Stores.keyValueStoreBuilder(storeProducer, Serdes.String(), new ProducerSerde());

        KeyValueBytesStoreSupplier taskPairProducer  = Stores.persistentKeyValueStore(Constants.PRODUCER_PAIR_STATE_STORES);
        StoreBuilder<KeyValueStore<String, ProducerPairStats>> producerTaskPairBuilder = Stores.keyValueStoreBuilder(taskPairProducer, Serdes.String(), new ProducerPairStatsSerde());

        builder.addStateStore(storeBuilder);
        builder.addStateStore(producerBuilder);
        builder.addStateStore(producerTaskPairBuilder);

        KStream<String, UserAlert> source = builder.stream(Constants.USER_PRICE_SETTING_TOPIC);
        KStream<String, UserAlert> trasform = source.transformValues(() ->  new ExchangePairTransformer(Constants.EXCHANGE_PAIR_STATE_STORES),
                Constants.EXCHANGE_PAIR_STATE_STORES);

        KeyValueMapper<String, UserAlert,String> alertDateAsKey = (key, alertMessage) -> alertMessage.getTicker();
        KStream<String, UserAlert> filter = trasform.filter((key, userAlert) -> userAlert.getExchange().length() > 0).selectKey(alertDateAsKey);;
                filter.to(Constants.ALL_EXCHANGE_PAIR, Produced.with(Serdes.String(), new UserAlertSerde()));

        // Process producer list
        KStream<String, Producer> producers = builder.stream(Constants.PRODUCER_LIST, Consumed.with(stringSerde, new ProducerSerde()));
        producers.transformValues(() ->  new ProducerTransformer(Constants.PRODUCER_LIST_STATE_STORES),
                Constants.PRODUCER_LIST_STATE_STORES);


        // Process producer task list
        KStream<String, UserAlert> pairStream = builder.stream(Constants.ALL_EXCHANGE_PAIR, Consumed.with(stringSerde, new UserAlertSerde()));
        KStream<String, ProducerTask> producerTasks = pairStream.transformValues(() ->  new ProducerTaskTransformer(Constants.PRODUCER_LIST_STATE_STORES),
                Constants.PRODUCER_LIST_STATE_STORES);
        producerTasks.filter((key, producerTask) -> producerTask.getTicker().length() > 0).to(Constants.PRODUCER_NEW_TASK, Produced.with(Serdes.String(), new ProducerTaskSerde()));


        //Process producer + pair list
        KStream<String, ProducerTask> producernewtaskstream = builder.stream(Constants.PRODUCER_NEW_TASK, Consumed.with(stringSerde, new ProducerTaskSerde()));
        KStream<String, ProducerTask> producerPairTasks = producernewtaskstream.transformValues(() ->  new ProducerPairTransformer(Constants.PRODUCER_PAIR_STATE_STORES),
                Constants.PRODUCER_PAIR_STATE_STORES);
        producerPairTasks.to(Constants.PRODUCER_TASK_UPDATED);


        Topology topology = builder.build();

        KafkaStreams streams = new KafkaStreams(topology, props);

        System.out.println(topology.describe());

//        streams.cleanUp();
        streams.start();
        System.out.println("Strema state : "+streams.state().name());

        MetadataService metadataService = new MetadataService(streams);
        metadataService.streamsMetadata();

        System.out.println("Strema allMetadata : "+streams.allMetadata());

        // Start the Restful proxy for servicing remote access to state stores
        final ExchangePairRestService restService = startRestProxy(streams, DEFAULT_HOST, port);

        CountDownLatch latch = new CountDownLatch(1);
        // This is not part of Runtime.getRuntime() block
        try
        {
        //    streams.start();
//            ReadOnlyKeyValueStore<String, ExchangePairStats> customerStore = waitUntilStoreIsQueryable("exchange_pair_stream", QueryableStoreTypes.keyValueStore(),streams);
//            //    ReadOnlyKeyValueStore<String , PriceSettingStats> keyValStore =   waitUntilStoreIsQueryable("user-price-aggregates", QueryableStoreTypes.<String , PriceSettingStats>keyValueStore(),streams);
//
//            //      streams.store("user-price-aggregates", QueryableStoreTypes.<String , PriceSettingStats>keyValueStore());
//            System.out.println(customerStore.all());
//
//            // Get the values for all of the keys available in this application instance
//            KeyValueIterator<String, ExchangePairStats> range = customerStore.all();
//            while (range.hasNext()) {
//                KeyValue<String, ExchangePairStats> next = range.next();
//                System.out.println("Key for " + next.key + ": " + next.value.toString());
//            }
//
//            latch.await();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }



        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }

    static public final class UserAlertSerde extends WrapperSerde<UserAlert> {
        public UserAlertSerde() {
            super(new JsonSerializer<UserAlert>(), new JsonDeserializer<UserAlert>(UserAlert.class));
        }
    }

    static public final class ExchangePairStatsSerde extends WrapperSerde<ExchangePairStats> {
        public ExchangePairStatsSerde() {
            super(new JsonSerializer<ExchangePairStats>(), new JsonDeserializer<ExchangePairStats>(ExchangePairStats.class));
        }
    }


//
//    static public final class TradeStatsSerde extends WrapperSerde<TradeStats> {
//        public TradeStatsSerde() {
//            super(new JsonSerializer<TradeStats>(), new JsonDeserializer<TradeStats>(TradeStats.class));
//        }
//    }

    static public final class PriceSettingSerde extends WrapperSerde<PriceSettingStats> {
        public PriceSettingSerde() {
            super(new JsonSerializer<PriceSettingStats>(), new JsonDeserializer<PriceSettingStats>(PriceSettingStats.class));
        }
    }

    static public final class CrytoPriceSerde extends WrapperSerde<CrytoPrice> {
        public CrytoPriceSerde() {
            super(new JsonSerializer<CrytoPrice>(), new JsonDeserializer<CrytoPrice>(CrytoPrice.class));
        }
    }

    static public final class AlertSerde extends WrapperSerde<AlertMessage> {
        public AlertSerde() {
            super(new JsonSerializer<AlertMessage>(), new JsonDeserializer<AlertMessage>(AlertMessage.class));
        }
    }

    static public final class ProducerSerde extends WrapperSerde<Producer> {
        public ProducerSerde() {
            super(new JsonSerializer<Producer>(), new JsonDeserializer<Producer>(Producer.class));
        }
    }
    static public final class ProducerTaskSerde extends WrapperSerde<ProducerTask> {
        public ProducerTaskSerde() {
            super(new JsonSerializer<ProducerTask>(), new JsonDeserializer<ProducerTask>(ProducerTask.class));
        }
    }

    static public final class ProducerPairStatsSerde extends WrapperSerde<ProducerPairStats> {
        public ProducerPairStatsSerde() {
            super(new JsonSerializer<ProducerPairStats>(), new JsonDeserializer<ProducerPairStats>(ProducerPairStats.class));
        }
    }

    public static <T> T waitUntilStoreIsQueryable(final String storeName, final QueryableStoreType<T> queryableStoreType, final KafkaStreams streams) throws InterruptedException
    {
        while (true)
        {
            try
            {
                return streams.store(storeName, queryableStoreType);
            }

            catch (InvalidStateStoreException ignored)
            {
                // store not yet ready for querying
                Thread.sleep(100);
            }
        }
    }

    static ExchangePairRestService startRestProxy(final KafkaStreams streams,
                                                   final String host,
                                                   final int port) throws Exception {
        final HostInfo hostInfo = new HostInfo(host, port);
        final ExchangePairRestService
                exchangePairRestService = new ExchangePairRestService(streams, hostInfo);
        exchangePairRestService.start(port);
        return exchangePairRestService;
    }
}
