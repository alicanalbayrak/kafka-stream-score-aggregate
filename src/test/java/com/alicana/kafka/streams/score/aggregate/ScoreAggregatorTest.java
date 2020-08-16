package com.alicana.kafka.streams.score.aggregate;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class ScoreAggregatorTest {

    private final Serde<String> stringSerde = new Serdes.StringSerde();
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, String> outputTopic;

    @Before
    public void setUp() {

        ScoreAggregator scoreAggregator = new ScoreAggregator();

        Topology topology = scoreAggregator.createScoreAggregatorTopology();
        Properties config = getTestProperties();

        this.testDriver = new TopologyTestDriver(topology, config);
        this.inputTopic = testDriver.createInputTopic(ScoreAggregator.INPUT_TOPIC, stringSerde.serializer(), stringSerde.serializer());
        this.outputTopic = testDriver.createOutputTopic(ScoreAggregator.OUPUT_TOPIC, stringSerde.deserializer(), stringSerde.deserializer());
    }

    @After
    public void tearDown() {
        this.testDriver.close();
    }

    @Test
    public void testDriver() {

        inputTopic.pipeInput("quaresma,12");
        inputTopic.pipeInput("cenk,17");
        inputTopic.pipeInput("ali,17");
        inputTopic.pipeInput("cenk,20");

        assertThat(outputTopic.readKeyValue()).isEqualTo(new KeyValue<>("12", "quaresma"));
        assertThat(outputTopic.readKeyValue()).isEqualTo(new KeyValue<>("17", "cenk"));
        assertThat(outputTopic.readKeyValue()).isEqualTo(new KeyValue<>("17", "cenk,ali"));
        assertThat(outputTopic.readKeyValue()).isEqualTo(new KeyValue<>("17", "ali"));
        assertThat(outputTopic.readKeyValue()).isEqualTo(new KeyValue<>("20", "cenk"));
    }

    private Properties getTestProperties() {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        return config;
    }
}
