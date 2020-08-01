package com.alicana.kafka.streams.score.aggregate;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;

import java.util.Properties;

public class AppMain {

    private static final String INPUT_TOPIC = "input-topic";
    private static final String INTER_TOPIC = "intermediate-table-topic";
    private static final String OUPUT_TOPIC = "output-topic";

    public static void main(String[] args) {

        StreamsBuilder builder = new StreamsBuilder();
        // Reading from source topic
        KStream<String, String> sourceStream = builder.stream(INPUT_TOPIC);

        sourceStream
                .filter((key, value) -> value.contains(","))
                // marks for re-partitioning
                .selectKey((key, value) -> value.split(",")[0].toLowerCase())
                .mapValues((key, value) -> value.split(",")[1].toLowerCase())
                // write back to Kafka (as KStream)
                .to(INTER_TOPIC);

        KTable<String, String> playerScoreTable = builder.table(INTER_TOPIC);

        KTable<String, String> scoreboard = playerScoreTable
                .groupBy((key, value) -> new KeyValue<>(value, key))
                .reduce((s1, s2) -> s1 + "," + s2, (a1, a2) -> a1 + "/" + a2);

        // write to output topic
        scoreboard.toStream().to(OUPUT_TOPIC);

        KafkaStreams streams = new KafkaStreams(builder.build(), getKafkaStreamConfig());
        // Always (and unconditionally) clean local state prior to starting the processing topology.
        // We opt for this unconditional call here because this will make it easier for you to play around with the example
        // when resetting the application for doing a re-run (via the Application Reset Tool,
        // http://docs.confluent.io/current/streams/developer-guide.html#application-reset-tool).
        //
        // The drawback of cleaning up local state prior is that your app must rebuilt its local state from scratch, which
        // will take time and will require reading all the state-relevant data from the Kafka cluster over the network.
        // Thus in a production scenario you typically do not want to clean up always as we do here but rather only when it
        // is truly needed, i.e., only under certain conditions (e.g., the presence of a command line flag for your app).
        // See `ApplicationResetExample.java` for a production-like example.
        streams.cleanUp();
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    static Properties getKafkaStreamConfig() {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "score-aggregator-app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        // DO NOT set to 0 in PROD
        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");
        // If application is disconnected (or first time starting), start read from earlier
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return config;
    }

}
