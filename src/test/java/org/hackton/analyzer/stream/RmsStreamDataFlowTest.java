package org.hackton.analyzer.stream;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.hackton.analyzer.config.CarkParkStreamConfig;
import org.hackton.analyzer.stream.TestDriverExtention.TopologyTestDriverExtention;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Properties;
import java.util.UUID;

import static org.springframework.test.util.ReflectionTestUtils.setField;


@RunWith(SpringRunner.class)
@ContextConfiguration(
        classes = {CarkParkStreamConfig.class},
        initializers = ConfigFileApplicationContextInitializer.class)
public class RmsStreamDataFlowTest{

    private TopologyTestDriver testDriver;
    private StringDeserializer stringDeserializer = new StringDeserializer();
    private ConsumerRecordFactory<String, String> stringConsumerRecordFactory =
            new ConsumerRecordFactory<>(new StringSerializer(), new StringSerializer());

    private KeyValueStore store;

    @Value("${car.park.barrier.event.topic}")
    private String rawSourceTopic;

    @Value("${car.park.store}")
    private String carkParkZoneStoreName;

    @Value("${car.park.availability.output.topic}")
    private String outputTopic;

    @Autowired
    private Properties inputStreamProperties;

    @Before
    public void setup() {
        StreamDataFlowPipe kafkaStream = new StreamDataFlowPipe(inputStreamProperties);
        setField(kafkaStream, "rawSourceTopic", rawSourceTopic);
        setField(kafkaStream, "carkParkZoneStoreName", carkParkZoneStoreName);
        setField(kafkaStream, "outputTopic", outputTopic);

        testDriver = new TopologyTestDriverExtention(kafkaStream.topology(), inputStreamProperties);
        store = testDriver.getKeyValueStore(carkParkZoneStoreName);
    }

    @After
    public void tearDown(){
        testDriver.close();
    }


    @Test
    public void dummyTest() throws Exception {

        testDriver.pipeInput(stringConsumerRecordFactory.create(rawSourceTopic, UUID.randomUUID().toString(), ""));

    }

}
