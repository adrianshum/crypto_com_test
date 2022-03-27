package hk.alien.cryptotest.interest;

import java.math.BigDecimal;

/**
 * Service to provide interest rate.
 *
 * Not enough information for business behind.  It is now providing a simple getRate() method
 */
public interface InterestRateService {
    BigDecimal getRate();
}
