package hk.alien.cryptotest.marketdata;

public interface MarketDataSubscriber {
    void onMarketDataInitialSnapshotEnd(MarketDataInitialSnapshotEndEvent event);
    void onMarketDataUpdate(MarketDataUpdateEvent event);
}
