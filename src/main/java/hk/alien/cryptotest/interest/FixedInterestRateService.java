package hk.alien.cryptotest.interest;

import java.math.BigDecimal;

public class FixedInterestRateService implements InterestRateService {
    private BigDecimal rate;

    public FixedInterestRateService(BigDecimal rate) {
        this.rate = rate;
    }

    @Override
    public BigDecimal getRate() {
        return this.rate;
    }
}
