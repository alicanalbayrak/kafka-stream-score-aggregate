# Kafka Scoreboard Aggregator App

This is a very trivial project to test `KStream` and `KTable` APIs. Basically, fetches (`playerName,totalGoal`) and 
updates scoreboard in on update.

## Getting Started

### Prerequisites

* Java 8+ (tested with v.13)
* Kafka cluster (and required topcis)
> TODO: auto-generate topics

### Build

> TODO: runnable jar
* `mvn clean package`

### Run on your local

1. Go to `/docker` directory and run `docker-compose up -d`
    ```
    ➜  docker docker-compose up -d
    Creating network "docker_default" with the default driver
    Creating docker_kafka-cluster_1 ... done
    ```
    > Note the name of the started instance (docker_kafka-cluster_1), we'll use it in the next step.
   
1. Connect to docker instance
    ```
    docker exec -it docker_kafka-cluster_1  /bin/bash
    ``` 
1. In `docker_kafka-cluster_1` run following topic creation commands
    ```bash
    root@fast-data-dev / $ kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic input-topic
    Created topic input-topic.
    root@fast-data-dev / $ kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic intermediate-table-topic --config cleanup.policy=compact
    Created topic intermediate-table-topic.
    root@fast-data-dev / $ kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic output-topic  --config cleanup.policy=compact
    Created topic output-topic.
    ```
1. Start producer
    ```bash
    kafka-console-producer --broker-list localhost:9092 --topic input-topic --property "key.separator=,"
    ```
1. Start consumer
    ```bash
    kafka-console-consumer --bootstrap-server localhost:9092 \
        --topic output-topic \
        --from-beginning \
        --formatter kafka.tools.DefaultMessageFormatter \
        --property print.key=true \
        --property print.value=true \
        --property "key.separator=," \
        --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
        --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
    ```
1. Start application
    * TODO
    ```
        java -jar app.jar
    ```   
