#!/bin/bash

# create input topic with two partitions
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic input-topic
# create intermediate topic for KTable (used for key,value extraction & invalid data filter)
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic intermediate-table-topic --config cleanup.policy=compact
# create output topic
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic output-topic  --config cleanup.policy=compact

# launch a Kafka consumer on output-topic
kafka-console-consumer --bootstrap-server localhost:9092 \
    --topic output-topic \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property "key.separator=," \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer

# then produce data to it
kafka-console-producer --broker-list localhost:9092 --topic input-topic --property "key.separator=,"

# package your application as a fat jar
mvn clean package

# run your fat jar
java -jar <your jar here>.jar

# list all topics that we have in Kafka (so we can observe the internal topics)
kafka-topics --list --zookeeper localhost:2181
