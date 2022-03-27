package hk.alien.cryptotest.pricing;

import hk.alien.cryptotest.Constants;
import hk.alien.cryptotest.MathUtils;
import hk.alien.cryptotest.bizdate.BusinessDateCalendar;
import hk.alien.cryptotest.instrument.Instrument;
import hk.alien.cryptotest.instrument.InstrumentRepository;
import hk.alien.cryptotest.instrument.InstrumentType;
import hk.alien.cryptotest.instrument.OptionSide;
import hk.alien.cryptotest.interest.FixedInterestRateService;
import hk.alien.cryptotest.interest.InterestRateService;
import hk.alien.cryptotest.marketdata.MarketDataUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Pricing strategy for European Options
 */
public class EuropeanOptionPricingStrategy implements PricingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(EuropeanOptionPricingStrategy.class);
    private static final BigDecimal DEFAULT_INTEREST_RATE = new BigDecimal("0.02"); // 2% per annum by default
    private InstrumentRepository instrumentRepository;
    private BusinessDateCalendar businessDateCalendar;
    private InterestRateService interestRateService;
    private final double EULER_NUMBER = Math.exp(1.0);
    private final int DAYS_IN_YEAR = 365;

    public EuropeanOptionPricingStrategy(InstrumentRepository instrumentRepository, BusinessDateCalendar businessDateCalendar) {
        this(instrumentRepository, businessDateCalendar, new FixedInterestRateService(DEFAULT_INTEREST_RATE));
    }

    public EuropeanOptionPricingStrategy(InstrumentRepository instrumentRepository,
                                         BusinessDateCalendar businessDateCalendar,
                                         InterestRateService interestRateService) {
        this.instrumentRepository = instrumentRepository;
        this.businessDateCalendar = businessDateCalendar;
        this.interestRateService = interestRateService;
    }

    /**
     * European Option Pricing Formula
     * K - Strike price
     * t - Time to maturity (in year)
     * r - interest rate
     * S - Underlying Stock Price
     * z - stock volatility
     * N(x) - cumulative normal distribution function
     * ln(x) - natural log
     * e - euler constant
     *
     * d1 =  ( ln(S/K) + (r + z^2/2) * t ) / (z * t^(1/2))
     * d2 = d1 - z * (t^(1/2))
     *
     * Call price = S * N(d1) - K * e^(-rt) * N(d2)
     * Put Price  = K * e^(-rt) * N(-d2) -  S * N(-d1)
     *
     * @param instrument
     * @param marketDataUpdate
     * @param underlyingPricing
     * @return
     */
    @Override
    public BigDecimal calculatePrice(Instrument instrument, MarketDataUpdate marketDataUpdate, Pricing underlyingPricing) {
        if (instrument.getType() != InstrumentType.EUROPEAN_OPTION) {
            throw new RuntimeException("Unexpected instrument passed for calculation " + instrument.getTicker() + " type " + instrument.getType());
        }

        LocalDate today = this.businessDateCalendar.getBusinessDate();
        LocalDate expiry = instrument.getExpiryDate();
        if ( ! today.isBefore(expiry)) {
            logger.warn("Passed expiry date for instrument {}", instrument.getTicker());
            return BigDecimal.ZERO;
        }

        // !!! We should use BigDecimal arithmetics.  However, given we want a faster calculation and easier to read code,
        // and pricing calculation could tolerate slight rounding difference, I am using floating point arithmetics here.

        double k = instrument.getStrike().doubleValue();
        double t = DAYS.between(today, expiry) / (double) DAYS_IN_YEAR;
        double r = this.interestRateService.getRate().doubleValue();
        double s = underlyingPricing.getPrice().doubleValue();
        double z = instrumentRepository.findById(instrument.getUnderlying())
                                            .map(Instrument::getAnnualizedStandardDeviation)
                                            .map(BigDecimal::doubleValue)
                                            .orElse(0d);

        // d1 =  ( ln(S/K) + (r + z^2/2) * t ) / (z * t^(1/2))
        double d1 = ( Math.log(s / k) + (r + z * z / 2) * t )
                    / (z * Math.pow(t, 0.5) );

        // d2 = d1 - z * (t^(1/2))
        double d2 = d1 - z * Math.pow(t, 0.5);

        // Call price = S * N(d1) - K * e^(-rt) * N(d2)
        // Put Price  = K * e^(-rt) * N(-d2) -  S * N(-d1)

        logger.debug("underlying {} k {} S {} t {} z {} d1 {} d2 {} cndf(d1) {} cndf(-d1) {}  cndf(d2) {} cndf(-d2) {} ",
                instrument.getUnderlying(), k, s, t, z, d1, d2, MathUtils.cndf(d1), MathUtils.cndf(-d1), MathUtils.cndf(d2),MathUtils.cndf(-d2));
        if (instrument.getOptionSide() == OptionSide.CALL) {
            return new BigDecimal(s * MathUtils.cndf(d1)
                                    - k * Math.pow(EULER_NUMBER, -r * t) * MathUtils.cndf(d2))
                    .setScale(Constants.PRICING_DECIMAL, RoundingMode.HALF_UP);
        } else { // PUT
            return new BigDecimal(k * Math.pow(EULER_NUMBER, -r * t) * MathUtils.cndf(-d2)
                                    - s * MathUtils.cndf(-d1) )
                    .setScale(Constants.PRICING_DECIMAL, RoundingMode.HALF_UP);
        }

    }


}
