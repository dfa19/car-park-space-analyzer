package org.hackton.analyzer.transformer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.hackton.analyzer.config.ZoneCapacity;
import org.hackton.analyzer.domain.BarrierEvent;
import org.hackton.analyzer.domain.BarrierType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Slf4j
public class BarrierEventTransformer implements ValueTransformer<BarrierEvent, KeyValue<String, Map>> {

    private KeyValueStore<String, Integer> carParkStore;
    private final String carParkStoreName;
    private ProcessorContext context;
    final Map<String, String> carkZoneCapacity;

    public BarrierEventTransformer(String carParkStoreName, Map<String, String> capacity) {
        Objects.requireNonNull(carParkStoreName,"Store Name can't be null");
        this.carParkStoreName = carParkStoreName;
        this.carkZoneCapacity = capacity;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;
        carParkStore = (KeyValueStore) this.context.getStateStore(carParkStoreName);
    }

    @Override
    public KeyValue<String, Map> transform(BarrierEvent event) {
        String storeId = String.format("%s~%s~%s", event.getCarParkId(), event.getBarrierId(), event.getBarrierType());
        //Initialize the store for this barrier
        carParkStore.putIfAbsent(storeId, 0);

        //Get the current count
        //Handle events from both RESERVED, SHIFT, MAIN (exit and entrance) barriers same way
        int usedCount = carParkStore.get(storeId);
        if(event.getBarrierType().equals(BarrierType.SHIFT.name()) ||
                event.getBarrierType().equals(BarrierType.RESERVED.name()) ||
                event.getBarrierType().equals(BarrierType.MAIN.name())){
            if(event.isEntry()){
                //increment the usedCount
                carParkStore.put(storeId, ++usedCount);
            }else{
                //Must be an exit barrier
                carParkStore.put(storeId, --usedCount);
            }
        }


        Map statusMap = new HashMap<String, String>();
        int capacity, otherCapacity, overallCapacity, availability, otherAvailability, overallAvailability = 0, availabilitySum = 0;
        capacity = Integer.valueOf(carkZoneCapacity.get(event.getBarrierType()));
        availability = capacity - usedCount;
        statusMap.put(String.format("%s~%s", event.getCarParkId(), event.getBarrierType()), availability == 0? "FULL": String.valueOf(availability));
        if (!event.getBarrierType().equals(BarrierType.MAIN.name())) {
            availabilitySum = availabilitySum + availability;
        }else{
            overallAvailability = availability;
        }

        //Collate all the latest counts for this car park into a map
        KeyValueIterator<String, Integer> iterator = carParkStore.all();
        while(iterator.hasNext()){
            KeyValue<String, Integer> keyValue = iterator.next();
            List<String> keyFragments = Arrays.asList(keyValue.key.split("~"));
            String carParkId = keyFragments.get(0);
            String barrierType = keyFragments.get(2);
            if(keyFragments.size() == 3){
                if(!event.getCarParkId().equals(carParkId)){
                    continue;
                }

                if(!event.getBarrierType().equals(barrierType)
                        && !barrierType.equals(BarrierType.GENERAL.name())
                        && !barrierType.equals(BarrierType.MAIN.name())){
                    otherCapacity = Integer.valueOf(carkZoneCapacity.get(barrierType));
                    otherAvailability = otherCapacity - keyValue.value;
                    availabilitySum = availabilitySum + otherAvailability;
                    statusMap.put(String.format("%s~%s", carParkId, barrierType), otherAvailability == 0? "FULL": String.valueOf(otherAvailability));
                }

                if (barrierType.equals(BarrierType.MAIN.name())) {
                    overallCapacity = Integer.valueOf(carkZoneCapacity.get(barrierType));
                    overallAvailability = overallCapacity - keyValue.value;
                    statusMap.put(String.format("%s~%s", carParkId, barrierType), overallAvailability == 0? "FULL": String.valueOf(overallAvailability));
                }
            }
        }

        //Workout the GENERAL car park availability
        //General = car-park-total – (reserved + shift)
        int generalAvailability = overallAvailability - availabilitySum;
        statusMap.put(String.format("%s~%s", event.getCarParkId(), BarrierType.GENERAL.name()), overallAvailability == 0? "FULL": String.valueOf(overallAvailability));

        return KeyValue.pair(event.getCarParkId(), statusMap);
    }

    @Override
    public void close() {
    }
}