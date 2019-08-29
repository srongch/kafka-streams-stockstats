package me.sronglong.pricealert.TimeStampExtractor;

import me.sronglong.pricealert.model.CrytoPrice;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;


public class UserPriceTimestampExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long previousTimestamp) {
        CrytoPrice purchasePurchaseTransaction = (CrytoPrice) record.value();
        return purchasePurchaseTransaction.getEventTime();
    }
}
