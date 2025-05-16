package com.thesss.platform.land.infrastructure.messaging;

import com.thesss.platform.land.application.port.out.FarmLandRecordEventPublisherPort;
import com.thesss.platform.land.domain.event.FarmLandRecordCreatedEvent;
import com.thesss.platform.land.domain.event.FarmLandRecordUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

@Service
public class FarmLandRecordEventPublisherImpl implements FarmLandRecordEventPublisherPort {

    private static final Logger logger = LoggerFactory.getLogger(FarmLandRecordEventPublisherImpl.class);
    private final StreamBridge streamBridge;

    // Binding names should match the configuration in application.yml
    // e.g., spring.cloud.stream.bindings.farmLandRecordCreated-out-0.destination=land-record-created-topic
    private static final String BINDING_FARM_LAND_RECORD_CREATED = "farmLandRecordCreated-out-0";
    private static final String BINDING_FARM_LAND_RECORD_UPDATED = "farmLandRecordUpdated-out-0";

    @Autowired
    public FarmLandRecordEventPublisherImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public void publishFarmLandRecordCreatedEvent(FarmLandRecordCreatedEvent event) {
        logger.info("Publishing FarmLandRecordCreatedEvent for LandRecordId: {}", event.getLandRecordId().getValue());
        Message<FarmLandRecordCreatedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader("partitionKey", event.getLandRecordId().getValue().toString()) // Example partition key
                .setHeader("contentType", MimeTypeUtils.APPLICATION_JSON_VALUE)
                .build();
        try {
            boolean sent = streamBridge.send(BINDING_FARM_LAND_RECORD_CREATED, message);
            if (sent) {
                logger.debug("Successfully sent FarmLandRecordCreatedEvent for LandRecordId: {}", event.getLandRecordId().getValue());
            } else {
                logger.warn("Failed to send FarmLandRecordCreatedEvent for LandRecordId: {} (StreamBridge returned false)", event.getLandRecordId().getValue());
                // Consider retry mechanisms or dead-letter queueing for critical events if not handled by Stream framework
            }
        } catch (Exception e) {
            logger.error("Exception occurred while publishing FarmLandRecordCreatedEvent for LandRecordId: {}", event.getLandRecordId().getValue(), e);
            // Handle exception, possibly rethrow or log for monitoring
        }
    }

    @Override
    public void publishFarmLandRecordUpdatedEvent(FarmLandRecordUpdatedEvent event) {
        logger.info("Publishing FarmLandRecordUpdatedEvent for LandRecordId: {}", event.getLandRecordId().getValue());
        Message<FarmLandRecordUpdatedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader("partitionKey", event.getLandRecordId().getValue().toString()) // Example partition key
                .setHeader("contentType", MimeTypeUtils.APPLICATION_JSON_VALUE)
                .build();
        try {
            boolean sent = streamBridge.send(BINDING_FARM_LAND_RECORD_UPDATED, message);
            if (sent) {
                logger.debug("Successfully sent FarmLandRecordUpdatedEvent for LandRecordId: {}", event.getLandRecordId().getValue());
            } else {
                logger.warn("Failed to send FarmLandRecordUpdatedEvent for LandRecordId: {} (StreamBridge returned false)", event.getLandRecordId().getValue());
            }
        } catch (Exception e) {
            logger.error("Exception occurred while publishing FarmLandRecordUpdatedEvent for LandRecordId: {}", event.getLandRecordId().getValue(), e);
        }
    }
}