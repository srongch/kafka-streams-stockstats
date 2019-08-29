package me.sronglong.pricealert;

import me.sronglong.pricealert.Transformer.UserAlertTransformer;
import me.sronglong.pricealert.model.*;
import me.sronglong.pricealert.serde.JsonDeserializer;
import me.sronglong.pricealert.serde.JsonSerializer;
import me.sronglong.pricealert.serde.WrapperSerde;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.state.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;


public class UserPriceProcessor {

    public static void main(String[] args) throws Exception {

        Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092,127.0.0.1:9093,127.0.0.1:9094");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "user_price_setting");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, UserAlertSerde.class.getName());
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);
        // setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
        // Note: To re-run the demo, you need to use the offset reset tool:
        // https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams+Application+Reset+Tool
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // creating an AdminClient and checking the number of brokers in the cluster, so I'll know how many replicas we want...

//        AdminClient ac = AdminClient.create(props);
//        DescribeClusterResult dcr = ac.describeCluster();
//        int clusterSize = dcr.nodes().get().size();
//
//        if (clusterSize<3)
//            props.put("replication.factor",clusterSize);
//        else
//            props.put("replication.factor",3);

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, UserAlert> source = builder.stream(Constants.USER_PRICE_SETTING_TOPIC);
        source.foreach((key, value) -> System.out.println(key + " => " + value));


        Serde<String> stringSerde = Serdes.String();

        KStream<String, PriceSettingStats> stats = source
                .groupByKey()
                .<PriceSettingStats>aggregate(() -> new PriceSettingStats(),(k, v, tradestats) -> tradestats.add(v),
                        Materialized.<String, PriceSettingStats, KeyValueStore<Bytes, byte[]>>as(Constants.PRICE_STATE_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new PriceSettingSerde())

                        )
                .toStream()
                .mapValues((trade) -> trade.computeAvgPrice());

        stats.to(Constants.USER_PRICE_SETTING_TOPIC_OUTPUT, Produced.keySerde(stringSerde));


        String userPriceStateStore = Constants.PRICE_STATE_STORE;

        KeyValueBytesStoreSupplier storeSupplier = Stores.persistentKeyValueStore(userPriceStateStore);
        StoreBuilder<KeyValueStore<String, PriceSettingStats>> storeBuilder = Stores.keyValueStoreBuilder(storeSupplier, Serdes.String(),new UserPriceProcessor.PriceSettingSerde());


        KStream<String, CrytoPrice> source1 = builder.stream( Constants.CRYPTO_PRICE_aggregated, Consumed.with(stringSerde, new TradingPriceProcessor.CrytoPriceSerde()));

        KStream<String, AlertMessage> filterAlert = source1.transformValues(() ->  new UserAlertTransformer(userPriceStateStore),
                userPriceStateStore);

        KeyValueMapper<String, AlertMessage,String> alertDateAsKey = (key, alertMessage) -> alertMessage.getTicker();
        KStream<String, AlertMessage> filterd = filterAlert.filter((key, alertMessage) -> alertMessage.getHasValue() == true).selectKey(alertDateAsKey);
        KStream<String, Message> flateMap = filterd.flatMap(
                (key, value) -> {
                    List<KeyValue<String, Message>> result = new LinkedList<>();
                    List<PushMessage> messages = value.getPushMessages();
                    for (PushMessage message : messages) {
                        result.add(KeyValue.pair(key, new Message(message.getMessage(),message.getPushToken(),message.getEventTime())));
                    }
                    return result;
                }
        );
        flateMap.to(Constants.USER_ALERTS, Produced.with(Serdes.String(), new TradingPriceProcessor.MessageSerde()));


        Topology topology = builder.build();

        KafkaStreams streams = new KafkaStreams(topology, props);

        System.out.println(topology.describe());

//        streams.cleanUp();
        streams.start();
        System.out.println("Strema state : "+streams.state().name());


        CountDownLatch latch = new CountDownLatch(1);
        // This is not part of Runtime.getRuntime() block
        try
        {
        //    streams.start();
            ReadOnlyKeyValueStore<String, PriceSettingStats> customerStore = waitUntilStoreIsQueryable("user-price-aggregates3", QueryableStoreTypes.keyValueStore(),streams);
            //    ReadOnlyKeyValueStore<String , PriceSettingStats> keyValStore =   waitUntilStoreIsQueryable("user-price-aggregates", QueryableStoreTypes.<String , PriceSettingStats>keyValueStore(),streams);

            //      streams.store("user-price-aggregates", QueryableStoreTypes.<String , PriceSettingStats>keyValueStore());
            System.out.println(customerStore.all());

            // Get the values for all of the keys available in this application instance
            KeyValueIterator<String, PriceSettingStats> range = customerStore.all();
            while (range.hasNext()) {
                KeyValue<String, PriceSettingStats> next = range.next();
                System.out.println("Key for " + next.key + ": " + next.value.toString());
            }

            latch.await();
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
}
