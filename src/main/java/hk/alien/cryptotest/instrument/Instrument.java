package hk.alien.cryptotest.instrument;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Instrument {
    // Key of Instrument.  In real-life usually this is just an alternative identifier, and we should have
    // an unchanging ID.  Here we use ticker as ID to simplify the situation.
    protected String ticker;

    protected InstrumentType type;

    protected BigDecimal expectedReturn = BigDecimal.ZERO;
    protected BigDecimal annualizedStandardDeviation = BigDecimal.ZERO;


    protected OptionSide optionSide;     // Call/Put
    protected BigDecimal strike;
    protected LocalDate expiryDate;

    public Instrument() {
    }

    public Instrument(InstrumentType type, String ticker) {
        this.type = type;
        this.ticker = ticker;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public InstrumentType getType() {
        return type;
    }

    public void setType(InstrumentType type) {
        this.type = type;
    }

    public BigDecimal getExpectedReturn() {
        return expectedReturn;
    }

    public void setExpectedReturn(BigDecimal expectedReturn) {
        this.expectedReturn = expectedReturn;
    }

    public BigDecimal getAnnualizedStandardDeviation() {
        return annualizedStandardDeviation;
    }

    public void setAnnualizedStandardDeviation(BigDecimal annualizedStandardDeviation) {
        this.annualizedStandardDeviation = annualizedStandardDeviation;
    }

    public OptionSide getOptionSide() {
        return optionSide;
    }

    public void setOptionSide(OptionSide optionSide) {
        this.optionSide = optionSide;
    }

    public BigDecimal getStrike() {
        return strike;
    }

    public void setStrike(BigDecimal strike) {
        this.strike = strike;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getUnderlying() {
        return underlying;
    }

    public void setUnderlying(String underlying) {
        this.underlying = underlying;
    }

    protected String underlying;


}
