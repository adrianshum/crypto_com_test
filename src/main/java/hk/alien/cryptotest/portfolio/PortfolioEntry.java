package hk.alien.cryptotest.portfolio;

import hk.alien.cryptotest.instrument.Instrument;
import hk.alien.cryptotest.pricing.Pricing;

import java.math.BigDecimal;

public class PortfolioEntry {
    private String ticker;

    /**
     * Position on hand.  Positive for Long Position, and Negative for Short.
     */
    private BigDecimal position = BigDecimal.ZERO;

    /**
     * Latest Pricing
     */
    private Pricing pricing;

    public PortfolioEntry() {
    }

    public PortfolioEntry(String ticker, BigDecimal position) {
        this(ticker, position, null);
    }

    public PortfolioEntry(String ticker, BigDecimal position, Pricing pricing) {
        this.ticker = ticker;
        this.position = position;
        this.pricing = pricing;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public BigDecimal getPosition() {
        return position;
    }

    public void setPosition(BigDecimal position) {
        this.position = position;
    }

    public Pricing getPricing() {
        return pricing;
    }

    public void setPricing(Pricing pricing) {
        if (this.ticker.equals(pricing.getTicker())) {
            this.pricing = pricing;
        } else {
            // ignore, as it is incorrect pricing
        }
    }

    public BigDecimal getMarketValue() {
        if (this.pricing == null) {
            return BigDecimal.ZERO;
        }

        return this.pricing.getPrice().multiply(this.position);
    }
}
