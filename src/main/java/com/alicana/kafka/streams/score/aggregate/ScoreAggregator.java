package com.alicana.kafka.streams.score.aggregate;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ScoreAggregator {

    public static final String INPUT_TOPIC = "input-topic";
    public static final String INTER_TOPIC = "intermediate-table-topic";
    public static final String OUPUT_TOPIC = "output-topic";

    private static final String COMMA = ",";
    private final BiFunction<String, String, String> scorerAppender = (s1, s2) -> s1 + "," + s2;
    private final BiFunction<String, String, String> scorerRemover = (s1, s2) -> {

        List<String> scorerList = Arrays.asList(s1.split(COMMA));
        return scorerList.stream()
                .filter(scorer -> !scorer.equalsIgnoreCase(s2))
                .collect(Collectors.joining(COMMA));
    };

    public static void main(String[] args) {

        final ScoreAggregator scoreAggregator = new ScoreAggregator();
        final Topology topology = scoreAggregator.createScoreAggregatorTopology();
        final Properties scoreAggregatorStreamConfig = SBStreamConfig.getConfig();

        KafkaStreams streams = new KafkaStreams(topology, scoreAggregatorStreamConfig);
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

    public Topology createScoreAggregatorTopology() {

        StreamsBuilder builder = new StreamsBuilder();
        // Reading from source topic
        KStream<String, String> sourceStream = builder.stream(INPUT_TOPIC);

        sourceStream
                .filter((key, value) -> value.contains(COMMA))
                // marks for re-partitioning
                .selectKey((key, value) -> value.split(COMMA)[0].toLowerCase())
                .mapValues((key, value) -> value.split(COMMA)[1].toLowerCase())
                // write back to Kafka (as KStream)
                .to(INTER_TOPIC);

        KTable<String, String> playerScoreTable = builder.table(INTER_TOPIC);

        KTable<String, String> scoreboard = playerScoreTable
                .groupBy((key, value) -> new KeyValue<>(value, key))
                .reduce(scorerAppender::apply, scorerRemover::apply);

        // write to output topic
        scoreboard.toStream().to(OUPUT_TOPIC);

        return builder.build();
    }

}
