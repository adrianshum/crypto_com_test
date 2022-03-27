package hk.alien.cryptotest.pricing;

import hk.alien.cryptotest.instrument.Instrument;
import hk.alien.cryptotest.marketdata.MarketDataUpdate;

import java.math.BigDecimal;

/**
 * Strategy to calculate pricing.
 *
 * Based on current design, pricing of an instrument is calculated based on
 * - Instrument static data
 * - Related market data update
 * - Pricing of underlying instrument
 */
public interface PricingStrategy {
    BigDecimal calculatePrice(Instrument instrument, MarketDataUpdate marketDataUpdate, Pricing underlyingPricing);
}
