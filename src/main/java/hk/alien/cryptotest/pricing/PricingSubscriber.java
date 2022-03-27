package hk.alien.cryptotest.pricing;

public interface PricingSubscriber {
    public void onPricingInitialSnapshotEnd(PricingInitialSnapshotEndEvent event);
    public void onPricingUpdate(PricingUpdateEvent event);

}
