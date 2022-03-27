package hk.alien.cryptotest.marketdata;

import java.math.BigDecimal;
import java.time.Instant;

public class MarketDataInitialSnapshotEndEvent extends MarketDataEvent {
    public MarketDataInitialSnapshotEndEvent() {
        super(EventType.INITIAL_SNAPSHOT_END);
    }
}
