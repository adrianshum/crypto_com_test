package hk.alien.cryptotest.portfolio;

import hk.alien.cryptotest.marketdata.MarketDataUpdateEvent;
import hk.alien.cryptotest.pricing.Pricing;
import hk.alien.cryptotest.pricing.PricingUpdateEvent;

public interface PortfolioView {
    void onPortfolioUpdate(Portfolio portfolio);

    void onPricingUpdate(PricingUpdateEvent pricingUpdate);
}
