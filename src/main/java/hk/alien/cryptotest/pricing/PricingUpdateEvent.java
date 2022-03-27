package hk.alien.cryptotest.pricing;

import hk.alien.cryptotest.marketdata.MarketDataUpdate;

import java.util.Collection;

/**
 * An event to represent updates of pricings.
 *
 * Related updates of pricings (e.g. updates caused by market data update of same underlying) are grouped in one event.
 */
public class PricingUpdateEvent {
    /**
     * Pricing update may be triggered by a market data update. In such case, marketDataUpdate will be populated.
     */
    private MarketDataUpdate marketDataUpdate;

    private Collection<Pricing> pricings;

    public PricingUpdateEvent(MarketDataUpdate marketDataUpdate, Collection<Pricing> pricings) {
        this.marketDataUpdate = marketDataUpdate;
        this.pricings = pricings;
    }

    public MarketDataUpdate getMarketDataUpdate() {
        return marketDataUpdate;
    }

    public Collection<Pricing> getPricings() {
        return pricings;
    }

    @Override
    public String toString() {
        return "PricingUpdateEvent{" +
                "marketDataUpdate=" + marketDataUpdate +
                ", pricings=" + pricings +
                '}';
    }
}
