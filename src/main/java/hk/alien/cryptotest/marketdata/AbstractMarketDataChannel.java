package hk.alien.cryptotest.marketdata;

import hk.alien.cryptotest.ServiceState;
import hk.alien.cryptotest.pricing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static hk.alien.cryptotest.ServiceState.RUNNING;

/**
 * Abstract class to help implement market data update publishing mechanism.
 *
 * Note: Original plan is to create RandomMarketDataService and a MockMarketDataService, for which the
 * latter is used in integration test to allow test case publishing required market data update. However
 * due to time limit I have not finished the integration test, hence the latter impl is not created yet.
 */
public abstract class AbstractMarketDataChannel implements MarketDataChannel {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMarketDataChannel.class);
    protected volatile ServiceState state = ServiceState.INITIALIZING;

    protected List<MarketDataSubscriber> subscribers=new ArrayList<>();

    @Override
    public void subscribe(MarketDataSubscriber subscriber) {
        // send initial snapshot to subscriber
        synchronized (subscribers) {
            this.subscribers.add(subscriber);
            for (MarketDataUpdate update: getInitialSnapshot()) {
                subscriber.onMarketDataUpdate(new MarketDataUpdateEvent(update));
            }
            if (this.state == RUNNING) {
                subscriber.onMarketDataInitialSnapshotEnd(new MarketDataInitialSnapshotEndEvent());
            }
        }
    }

    @Override
    public void unsubscribe(MarketDataSubscriber subscriber) {
        synchronized (subscribers) {
            this.subscribers.remove(subscriber);
        }
    }

    abstract protected Collection<MarketDataUpdate> getInitialSnapshot();

    // Publish market data update to subscribers
    protected void publishMarketDataUpdate(MarketDataUpdate marketDataUpdate) {
        synchronized (subscribers) {
            MarketDataUpdateEvent event = new MarketDataUpdateEvent(marketDataUpdate);
            for (MarketDataSubscriber s : this.subscribers) {
                s.onMarketDataUpdate(event);
            }
        }
    }


}
