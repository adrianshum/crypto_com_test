package hk.alien.cryptotest.pricing;

/**
 * Represent an event stream of Pricing updates.
 */
public interface PricingChannel {
    // NOTE: A very basic subscription mechanism. In real life, we may use distruptor/kafka/aeron etc
    public void subscribe(PricingSubscriber subscriber);
    public void unsubscribe(PricingSubscriber subscriber);
}
