package org.hackton.analyzer.factory;

import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.hackton.analyzer.serde.StreamSerdes;
import org.springframework.stereotype.Component;

@Component
public class StreamFactory {


    public static StoreBuilder<KeyValueStore<String, Integer>> createInMemoryStore(final String storeName){
        KeyValueBytesStoreSupplier storeSupplier = Stores.inMemoryKeyValueStore(storeName);
        return Stores.keyValueStoreBuilder(storeSupplier, StreamSerdes.stringSerde, StreamSerdes.integerSerde);
    }
}
