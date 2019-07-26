package org.hackton.analyzer.serde;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.hackton.analyzer.domain.BarrierEvent;


public final class StreamSerdes {

    public static final Serde<BarrierEvent> barrierEventSerde = SerdeFactory.createPojoSerdeFor(BarrierEvent.class, false);
    public static final Serde<String> stringSerde = Serdes.String();
}
