package org.hackton.analyzer.stream;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.hackton.analyzer.config.CarkParkStreamConfig;
import org.hackton.analyzer.config.ZoneCapacity;
import org.hackton.analyzer.domain.BarrierType;
import org.hackton.analyzer.stream.TestDriverExtention.TopologyTestDriverExtention;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.hackton.analyzer.serde.StreamSerdes.carParkStatusSerde;
import static org.springframework.test.util.ReflectionTestUtils.setField;


@RunWith(SpringRunner.class)
@ContextConfiguration(
        classes = {CarkParkStreamConfig.class, ZoneCapacity.class},
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

    @Autowired
    private Map<String, String> capacity;

    @Before
    public void setup() {
        StreamDataFlowPipe kafkaStream = new StreamDataFlowPipe(inputStreamProperties, capacity);
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


    /**
     * A Car enters through main the barrier, an event is fired.
     * Until we receive another event, we presume that the car has/is parked within the cark park,
     * probably in the GENERAL zone
     * @throws Exception
     */
    @Test
    public void carEntersTheMainBarrierTest() throws Exception {
        final String eventId = UUID.randomUUID().toString();
        testDriver.pipeInput(stringConsumerRecordFactory.create(
                rawSourceTopic,
                eventId,
                generateBarrierEvent(eventId, "1", BarrierType.MAIN.name(), true, "A")
        ));

        JsonDeserializer<Map> carParkStatusDeserializer = new JsonDeserializer<>(Map.class);
        ProducerRecord<String, Map> actualOutput =
                testDriver.readOutput(outputTopic, stringDeserializer, carParkStatusDeserializer);

        Assert.assertEquals("A", actualOutput.key());
        Assert.assertEquals("59", actualOutput.value().get("A~GENERAL"));
        Assert.assertEquals("59", actualOutput.value().get("A~MAIN"));

    }

    private String generateBarrierEvent(String eventId, String barrierId, String barrierType, boolean entry, String carParkId) throws Exception{
        URL url = this.getClass().getClassLoader().getResource("stream/barrier-event.json");
        Path resPath = java.nio.file.Paths.get(url.toURI());
        return new String(java.nio.file.Files.readAllBytes(resPath), StandardCharsets.UTF_8.name())
                .replace("{eventId}", UUID.randomUUID().toString())
                .replace("{barrierId}", barrierId)
                .replace("{barrierType}", barrierType)
                .replace("{entry}", Boolean.toString(entry))
                .replace("{carParkId}", carParkId)
                .replace("{timestamp}", LocalDateTime.now().toString());
    }

}
