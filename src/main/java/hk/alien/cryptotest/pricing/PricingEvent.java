package hk.alien.cryptotest.pricing;

import hk.alien.cryptotest.marketdata.MarketDataUpdateEvent;

import java.util.Collection;


public class PricingEvent {
    public enum EventType {
        INITIAL_SNAPSHOT_END,
        UPDATE
    }

    protected EventType eventType;

    protected PricingEvent(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }
}
