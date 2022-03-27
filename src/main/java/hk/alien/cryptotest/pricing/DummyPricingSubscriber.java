package hk.alien.cryptotest.pricing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyPricingSubscriber implements PricingSubscriber{
    private static final Logger logger = LoggerFactory.getLogger(DummyPricingSubscriber.class);
    @Override
    public void onPricingInitialSnapshotEnd(PricingInitialSnapshotEndEvent event) {
        logger.info("Pricing Snapshot End");
    }

    @Override
    public void onPricingUpdate(PricingUpdateEvent event) {
        logger.info("Pricing Update {} " + event);

    }
}
