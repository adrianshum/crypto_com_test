package hk.alien.cryptotest.pricing;

/**
 * Event to indicate end of initial snapshot of Pricing Updates.  Princing Update afterwards are
 * real-time change.
 */
public class PricingInitialSnapshotEndEvent extends PricingEvent {
    public PricingInitialSnapshotEndEvent() {
        super(PricingEvent.EventType.INITIAL_SNAPSHOT_END);
    }
}
