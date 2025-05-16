package com.thesss.platform.aggregators.event.consumer;

import com.thesss.platform.aggregators.event.dto.crop.CropCycleCreatedEventDTO;
import com.thesss.platform.aggregators.event.dto.crop.HarvestRecordedEventDTO;
import com.thesss.platform.aggregators.projection.handler.CropProjectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class CropEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(CropEventConsumer.class);

    private final CropProjectionHandler cropProjectionHandler;

    public CropEventConsumer(CropProjectionHandler cropProjectionHandler) {
        this.cropProjectionHandler = cropProjectionHandler;
    }

    @KafkaListener(
            topics = "${kafka.topics.crop-events}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleCropCycleCreatedEvent(
            @Payload CropCycleCreatedEventDTO event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Received CropCycleCreatedEvent: {} from partition {} at offset {}", event, partition, offset);
        cropProjectionHandler.handleCropCycleCreated(event)
                .doOnError(e -> log.error("Error processing CropCycleCreatedEvent for cropCycleId {}: {}", event.cropCycleId(), e.getMessage(), e))
                .subscribe(
                        null,
                        error -> log.error("Unhandled error after attempting to process CropCycleCreatedEvent for cropCycleId {}: {}", event.cropCycleId(), error.getMessage(), error)
                );
    }

    @KafkaListener(
            topics = "${kafka.topics.crop-events}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleHarvestRecordedEvent(
            @Payload HarvestRecordedEventDTO event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Received HarvestRecordedEvent: {} from partition {} at offset {}", event, partition, offset);
        cropProjectionHandler.handleHarvestRecorded(event)
                .doOnError(e -> log.error("Error processing HarvestRecordedEvent for cropCycleId {}: {}", event.cropCycleId(), e.getMessage(), e))
                .subscribe(
                        null,
                        error -> log.error("Unhandled error after attempting to process HarvestRecordedEvent for cropCycleId {}: {}", event.cropCycleId(), error.getMessage(), error)
                );
    }

    // As per SDS, CropProjectionHandler also has handleCropCycleStatusUpdated.
    // A DTO and listener method for CropCycleStatusUpdatedEventDTO would be added here
    // when that DTO is part of the generation scope.
    // For now, it's omitted as per the restricted file list.
}