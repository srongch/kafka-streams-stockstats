package me.sronglong.pricealert;

import me.sronglong.pricealert.model.AlertMessage;
import me.sronglong.pricealert.model.CrytoPrice;
import me.sronglong.pricealert.model.UserAlert;
import me.sronglong.pricealert.serde.JsonDeserializer;
import me.sronglong.pricealert.serde.JsonSerializer;
import me.sronglong.pricealert.serde.WrapperSerde;
import me.sronglong.pricealert.model.UserPriceSettingStats;
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

import java.util.Properties;
import java.util.concurrent.CountDownLatch;


public class UserPriceListProcessor {

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
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,"127.0.0.1:9092,127.0.0.1:9093,127.0.0.1:9094");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "user_price_list_setting");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, UserAlertSerde.class.getName());
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);
        props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, DEFAULT_HOST + ":" + port);
        // setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
        // Note: To re-run the demo, you need to use the offset reset tool:
        // https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams+Application+Reset+Tool
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        // creating an AdminClient and checking the number of brokers in the cluster, so I'll know how many replicas we want...

        AdminClient ac = AdminClient.create(props);
        DescribeClusterResult dcr = ac.describeCluster();
        int clusterSize = dcr.nodes().get().size();

        if (clusterSize<3)
            props.put("replication.factor",clusterSize);
        else
            props.put("replication.factor",3);

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, UserAlert> source = builder.stream(Constants.USER_PRICE_SETTING_TOPIC);
        source.foreach((key, value) -> System.out.println(key + " => " + value));


        Serde<String> stringSerde = Serdes.String();

        KStream<String, UserPriceSettingStats> stats = source
                .groupBy((key,userPriceSettingStats)->userPriceSettingStats.getPushToken())
                .<UserPriceSettingStats>aggregate(() -> new UserPriceSettingStats(),(k, v, tradestats) -> tradestats.add(v),
                        Materialized.<String, UserPriceSettingStats, KeyValueStore<Bytes, byte[]>>as(Constants.USER_PRICE_STATE_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new UserPriceSettingSerde())

                        )
                .toStream()
                .mapValues((trade) ->trade);

        stats.to(Constants.USER_PRICE_LIST_OUTPUT, Produced.keySerde(stringSerde));

        Topology topology = builder.build();

        KafkaStreams streams = new KafkaStreams(topology, props);

        System.out.println(topology.describe());

        streams.cleanUp();
        streams.start();
        System.out.println("Strema state : "+streams.state().name());

        MetadataService metadataService = new MetadataService(streams);
        metadataService.streamsMetadata();

        System.out.println("Strema all Metadata : "+streams.allMetadata());

        // Start the Restful proxy for servicing remote access to state stores
        final UserPriceListRestService restService = startRestProxy(streams, DEFAULT_HOST, port);


        CountDownLatch latch = new CountDownLatch(1);
        // This is not part of Runtime.getRuntime() block
        try
        {
        //    streams.start();
            ReadOnlyKeyValueStore<String, UserPriceSettingStats> customerStore = waitUntilStoreIsQueryable("user-price-list-aggregates4", QueryableStoreTypes.keyValueStore(),streams);
            //    ReadOnlyKeyValueStore<String , PriceSettingStats> keyValStore =   waitUntilStoreIsQueryable("user-price-aggregates", QueryableStoreTypes.<String , PriceSettingStats>keyValueStore(),streams);

            //      streams.store("user-price-aggregates", QueryableStoreTypes.<String , PriceSettingStats>keyValueStore());
            System.out.println(customerStore.all());
            System.out.println("Strema allMetadata : "+streams.allMetadata());

            // Get the values for all of the keys available in this application instance
            KeyValueIterator<String, UserPriceSettingStats> range = customerStore.all();

            Integer count = 0;

            while (range.hasNext()) {
                KeyValue<String, UserPriceSettingStats> next = range.next();
                System.out.println("Key for " + next.key + ": " + next.value.toString());
                count += 1;
            }
            System.out.println("total count " + count);

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

    static public final class UserPriceSettingSerde extends WrapperSerde<UserPriceSettingStats> {
        public UserPriceSettingSerde() {
            super(new JsonSerializer<UserPriceSettingStats>(), new JsonDeserializer<UserPriceSettingStats>(UserPriceSettingStats.class));
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

    static UserPriceListRestService startRestProxy(final KafkaStreams streams,
                                                                 final String host,
                                                                 final int port) throws Exception {
        final HostInfo hostInfo = new HostInfo(host, port);
        final UserPriceListRestService
                userPriceListRestService = new UserPriceListRestService(streams, hostInfo);
        userPriceListRestService.start(port);
        return userPriceListRestService;
    }
}
