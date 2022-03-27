package hk.alien.cryptotest.pricing;

import hk.alien.cryptotest.instrument.Instrument;
import hk.alien.cryptotest.marketdata.MarketDataUpdate;

import java.math.BigDecimal;

/**
 * Simple pricing strategy for equities (e.g. Stock).  The pricing is simply the last price in market update.
 */
public class EquityPricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal calculatePrice(Instrument instrument, MarketDataUpdate marketDataUpdate, Pricing underlyingPricing) {
        return marketDataUpdate.getPrice();
    }
}
