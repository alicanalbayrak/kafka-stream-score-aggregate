package com.alicana.kafka.streams.score.aggregate;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class SBStreamConfig {

    public static Properties getConfig() {
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
