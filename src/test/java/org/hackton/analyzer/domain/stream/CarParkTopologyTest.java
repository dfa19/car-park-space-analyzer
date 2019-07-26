/*
package org.hackton.analyzer.domain.stream;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.hackton.analyzer.domain.config.TopologyConfiguration;
import org.junit.Ignore;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static java.util.Collections.unmodifiableList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.TestCase.assertNotNull;
import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG;
import static org.awaitility.Awaitility.await;

@RunWith(SpringRunner.class)
@ContextConfiguration(
        classes = {TopologyConfiguration.class, CarParkTopology.class}
        , initializers = ConfigFileApplicationContextInitializer.class)
public class CarParkTopologyTest {

    private TopologyTestDriver testDriver;

    private ConsumerRecordFactory<String, String> stringRecordFactory =
            new ConsumerRecordFactory<>(new StringSerializer(), new StringSerializer());

    private static final List<String> barrierType = unmodifiableList(Arrays.asList(
            "Restricted", "UnRestricted"));


    private static final Random random = new Random();


    @Autowired
    private CarParkTopology titleTopology;


    @Autowired
    private ObjectMapper objectMapper;

    private static Properties kafkaProperties;

    @BeforeClass
    public static void setup() {
        kafkaProperties = new Properties();
        kafkaProperties.put(APPLICATION_ID_CONFIG, "test");
        kafkaProperties.put(BOOTSTRAP_SERVERS_CONFIG, "test.server:1234");
        kafkaProperties.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        kafkaProperties.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        kafkaProperties.put(CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");
    }


    @Ignore // Given 100 Car spaces
    // one Car In  99
    // one Car Out  100
    public void oneCarInOneCarOut() {

        List<ConsumerRecord<byte[], byte[]>> consumerRecords = new ArrayList<>();
        consumerRecords.add(getBarrierEventRecord(true));

        testDriver = new TopologyTestDriver(titleTopology.topology(), kafkaProperties);
        testDriver.pipeInput(consumerRecords);

        await().atMost(10, SECONDS).untilAsserted(() -> {
            ProducerRecord<String, String> titleRecord = testDriver.readOutput(
                    "available-spaces", new StringDeserializer(), new StringDeserializer());
            assertNotNull(titleRecord);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            String title = objectMapper.readTree(titleRecord.value()).toString();
            //assertEquals(itemId, JsonPath.read(title, "$.itemId"));
        });
    }


    private ConsumerRecord<byte[], byte[]> getBarrierEventRecord(boolean direction) {


        return stringRecordFactory.create("car-park-barrier-events", UUID.randomUUID().toString(), getBarrierEvent(objectMapper.createObjectNode(), direction));
    }


    public String getBarrierEvent(ObjectNode barrierEvent, boolean direction) {

        barrierEvent.put("barrierId", random.nextInt());
        barrierEvent.put("barrierType", barrierType.get(random.nextInt(barrierType.size())));
        barrierEvent.put("direction", direction);
        barrierEvent.put("carParkId", random.nextInt());
        barrierEvent.put("timestamp", now().toString());

        String itemAsJson;
        try {
            itemAsJson = objectMapper.writeValueAsString(barrierEvent);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
        return itemAsJson;
    }

    @After
    public void closeTestDriver() {
        testDriver.close();
    }

}*/
