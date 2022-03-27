package hk.alien.cryptotest.marketdata;


public class MarketDataEvent {
    public enum EventType {
        /**
         * Indicates initial snapshots have been published.  Subsequent updates are realtime updates.
         */
        INITIAL_SNAPSHOT_END,

        /**
         * Updates of market data.
         */
        UPDATE,
    }

    protected EventType eventType;

    protected MarketDataEvent(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }
}
