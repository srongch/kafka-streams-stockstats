package me.sronglong.pricealert;

import me.sronglong.pricealert.TimeStampExtractor.UserPriceTimestampExtractor;
import me.sronglong.pricealert.model.CrytoPrice;
import me.sronglong.pricealert.serde.JsonDeserializer;
import me.sronglong.pricealert.serde.JsonSerializer;
import me.sronglong.pricealert.serde.WrapperSerde;
import me.sronglong.pricealert.model.AlertMessage;
import me.sronglong.pricealert.model.Message;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.*;

import java.util.Properties;

/**
 * Input is a stream of trades
 * Output is a average of 10second of all the trading pair.
 *
 */
public class TradingPriceProcessor {

    public static void main(String[] args) throws Exception {

        Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092,127.0.0.1:9093,127.0.0.1:9094");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "crypto-price-processor");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, CrytoPriceSerde.class.getName());
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, UserPriceTimestampExtractor.class);
        props.put(StreamsConfig.RETRIES_CONFIG,3);
        props.put(StreamsConfig.RETRY_BACKOFF_MS_CONFIG,5099000);
//        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG,0);
//        props["commit.interval.ms"] = 0;
        // setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
        // Note: To re-run the demo, you need to use the offset reset tool:
        // https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams+Application+Reset+Tool
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

//        // creating an AdminClient and checking the number of brokers in the cluster, so I'll know how many replicas we want...
//
//        AdminClient ac = AdminClient.create(props);
//        DescribeClusterResult dcr = ac.describeCluster();
//        int clusterSize = dcr.nodes().get().size();
//
//        if (clusterSize<3)
//            props.put("replication.factor",clusterSize);
//        else
//            props.put("replication.factor",3);

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, CrytoPrice> source = builder.stream("trading-events-partition");

        KStream<String, CrytoPrice> stats = source
                .groupByKey()
                .windowedBy(TimeWindows.of(10000))
                .<CrytoPrice>aggregate(() -> new CrytoPrice(),(k, v, cryptoPrice) -> cryptoPrice.add(v),
                        Materialized.<String, CrytoPrice, WindowStore<Bytes, byte[]>>as("crypto-aggregates")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new CrytoPriceSerde()))
                .toStream()
                .map((key,value) -> new KeyValue<>(key.key(),value.computeAvgPrice()));
        stats.to(Constants.CRYPTO_PRICE_aggregated, Produced.keySerde(Serdes.String()));

        Topology topology = builder.build();

        KafkaStreams streams = new KafkaStreams(topology, props);

        System.out.println(topology.describe());


        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }

    static public final class CrytoPriceSerde extends WrapperSerde<CrytoPrice> {
        public CrytoPriceSerde() {
            super(new JsonSerializer<CrytoPrice>(), new JsonDeserializer<CrytoPrice>(CrytoPrice.class));
        }
    }

    static public final class MessageSerde extends WrapperSerde<Message> {
        public MessageSerde() {
            super(new JsonSerializer<Message>(), new JsonDeserializer<Message>(Message.class));
        }
    }



}
