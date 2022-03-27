package hk.alien.cryptotest.marketdata;

import java.math.BigDecimal;
import java.time.Instant;

public class MarketDataUpdate {
    private String ticker;
    private Instant time;
    private BigDecimal price;

    public MarketDataUpdate(String ticker, Instant time, BigDecimal price) {
        this.ticker = ticker;
        this.time = time;
        this.price = price;
    }

    public String getTicker() {
        return ticker;
    }

    public Instant getTime() {
        return time;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "MarketDataUpdate{" +
                "ticker='" + ticker + '\'' +
                ", time=" + time +
                ", price=" + price +
                '}';
    }
}
