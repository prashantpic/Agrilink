package com.thesss.platform.aggregators.event.consumer;

import com.thesss.platform.aggregators.event.dto.land.LandRecordCreatedEventDTO;
import com.thesss.platform.aggregators.event.dto.land.LandRecordUpdatedEventDTO;
import com.thesss.platform.aggregators.projection.handler.LandProjectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class LandEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(LandEventConsumer.class);

    private final LandProjectionHandler landProjectionHandler;

    public LandEventConsumer(LandProjectionHandler landProjectionHandler) {
        this.landProjectionHandler = landProjectionHandler;
    }

    @KafkaListener(
            topics = "${kafka.topics.land-events}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleLandRecordCreatedEvent(
            @Payload LandRecordCreatedEventDTO event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Received LandRecordCreatedEvent: {} from partition {} at offset {}", event, partition, offset);
        landProjectionHandler.handleLandRecordCreated(event)
                .doOnError(e -> log.error("Error processing LandRecordCreatedEvent for landId {}: {}", event.landId(), e.getMessage(), e))
                .subscribe(
                        null,
                        error -> log.error("Unhandled error after attempting to process LandRecordCreatedEvent for landId {}: {}", event.landId(), error.getMessage(), error)
                );
    }

    @KafkaListener(
            topics = "${kafka.topics.land-events}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleLandRecordUpdatedEvent(
            @Payload LandRecordUpdatedEventDTO event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Received LandRecordUpdatedEvent: {} from partition {} at offset {}", event, partition, offset);
        landProjectionHandler.handleLandRecordUpdated(event)
                .doOnError(e -> log.error("Error processing LandRecordUpdatedEvent for landId {}: {}", event.landId(), e.getMessage(), e))
                .subscribe(
                        null,
                        error -> log.error("Unhandled error after attempting to process LandRecordUpdatedEvent for landId {}: {}", event.landId(), error.getMessage(), error)
                );
    }
}