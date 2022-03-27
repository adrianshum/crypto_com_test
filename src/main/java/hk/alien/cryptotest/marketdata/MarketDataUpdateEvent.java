package hk.alien.cryptotest.marketdata;

import java.math.BigDecimal;
import java.time.Instant;

public class MarketDataUpdateEvent extends MarketDataEvent {

    protected MarketDataUpdate marketDataUpdate;
    public MarketDataUpdateEvent(MarketDataUpdate update) {
        super(EventType.UPDATE);
        this.marketDataUpdate = update;
    }

    public MarketDataUpdate getMarketDataUpdate() {
        return marketDataUpdate;
    }
}
