package com.thesss.platform.aggregators.event.consumer;

import com.thesss.platform.aggregators.event.dto.farmer.FarmerProfileUpdatedEventDTO;
import com.thesss.platform.aggregators.event.dto.farmer.FarmerRegisteredEventDTO;
import com.thesss.platform.aggregators.projection.handler.FarmerProjectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class FarmerEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(FarmerEventConsumer.class);

    private final FarmerProjectionHandler farmerProjectionHandler;

    public FarmerEventConsumer(FarmerProjectionHandler farmerProjectionHandler) {
        this.farmerProjectionHandler = farmerProjectionHandler;
    }

    @KafkaListener(
            topics = "${kafka.topics.farmer-events}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory" // Assumes this bean name from KafkaConsumerConfig
    )
    public void handleFarmerRegisteredEvent(
            @Payload FarmerRegisteredEventDTO event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Received FarmerRegisteredEvent: {} from partition {} at offset {}", event, partition, offset);
        farmerProjectionHandler.handleFarmerRegistered(event)
                .doOnError(e -> log.error("Error processing FarmerRegisteredEvent for farmerId {}: {}", event.farmerId(), e.getMessage(), e))
                .subscribe(
                        null, // onNext is not applicable for Mono<Void>
                        error -> log.error("Unhandled error after attempting to process FarmerRegisteredEvent for farmerId {}: {}", event.farmerId(), error.getMessage(), error)
                );
    }

    @KafkaListener(
            topics = "${kafka.topics.farmer-events}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleFarmerProfileUpdatedEvent(
            @Payload FarmerProfileUpdatedEventDTO event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Received FarmerProfileUpdatedEvent: {} from partition {} at offset {}", event, partition, offset);
        farmerProjectionHandler.handleFarmerProfileUpdated(event)
                .doOnError(e -> log.error("Error processing FarmerProfileUpdatedEvent for farmerId {}: {}", event.farmerId(), e.getMessage(), e))
                .subscribe(
                        null,
                        error -> log.error("Unhandled error after attempting to process FarmerProfileUpdatedEvent for farmerId {}: {}", event.farmerId(), error.getMessage(), error)
                );
    }
}