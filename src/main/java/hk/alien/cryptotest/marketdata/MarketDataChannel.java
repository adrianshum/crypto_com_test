package hk.alien.cryptotest.marketdata;

import hk.alien.cryptotest.pricing.PricingSubscriber;

public interface MarketDataChannel {
    // NOTE: A very basic subscription mechanism. In real life, we may use better event distrubution library
    public void subscribe(MarketDataSubscriber subscriber);
    public void unsubscribe(MarketDataSubscriber subscriber);
}
