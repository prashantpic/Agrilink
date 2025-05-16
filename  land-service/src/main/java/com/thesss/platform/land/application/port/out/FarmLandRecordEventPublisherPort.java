package com.thesss.platform.land.application.port.out;

import com.thesss.platform.land.domain.event.FarmLandRecordCreatedEvent;
import com.thesss.platform.land.domain.event.FarmLandRecordUpdatedEvent;

public interface FarmLandRecordEventPublisherPort { // REPO-LAND-SVC

    /**
     * Publishes a FarmLandRecordCreatedEvent to the message broker.
     *
     * @param event The event to publish.
     */
    void publishFarmLandRecordCreatedEvent(FarmLandRecordCreatedEvent event);

    /**
     * Publishes a FarmLandRecordUpdatedEvent to the message broker.
     *
     * @param event The event to publish.
     */
    void publishFarmLandRecordUpdatedEvent(FarmLandRecordUpdatedEvent event);
}