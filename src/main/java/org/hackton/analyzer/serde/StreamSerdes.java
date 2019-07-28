package org.hackton.analyzer.serde;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.hackton.analyzer.domain.BarrierEvent;

import java.util.Map;


public final class StreamSerdes {

    public static final Serde<BarrierEvent> barrierEventSerde = SerdeFactory.createPojoSerdeFor(BarrierEvent.class, false);
    public static final Serde<String> stringSerde = Serdes.String();
    public static final Serde<Integer> integerSerde = Serdes.Integer();
    public static final Serde<Map> carParkStatusSerde = SerdeFactory.createPojoSerdeFor(Map.class, false);
}
