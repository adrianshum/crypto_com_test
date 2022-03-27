package hk.alien.cryptotest.pricing;

import hk.alien.cryptotest.instrument.Instrument;

import java.math.BigDecimal;

/**
 * Immutable value object to represent pricing for an Instrument.
 *
 * A pricing could be sourced from external market data, or calculated within the system.
 */
public class Pricing {
    /**
     * ID of instrument
     */
    private String ticker;

    /**
     * Pricing calculated.
     */
    private BigDecimal price;

    /**
     * For derivatives, pricing is calculated based on an underlying pricing.
     * Null for non-derivatives
     */
    private Pricing underlyingPricing;

    public Pricing(String ticker, BigDecimal price, Pricing underlyingPricing) {
        this.ticker = ticker;
        this.price = price;
        this.underlyingPricing = underlyingPricing;
    }

    public Pricing(String ticker, BigDecimal price) {
        this(ticker, price, null);
    }

    public String getTicker() {
        return ticker;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Pricing getUnderlyingPricing() {
        return underlyingPricing;
    }

    @Override
    public String toString() {
        return "Pricing{" +
                "ticker='" + ticker + '\'' +
                ", price=" + price +
                ", underlyingPricing=" + (underlyingPricing == null ? null : underlyingPricing.getTicker()) +
                '}';
    }
}
