package hk.alien.cryptotest.pricing

import hk.alien.cryptotest.bizdate.BusinessDateCalendar
import hk.alien.cryptotest.instrument.Instrument
import hk.alien.cryptotest.instrument.InstrumentRepository
import hk.alien.cryptotest.instrument.InstrumentType
import hk.alien.cryptotest.instrument.OptionSide
import hk.alien.cryptotest.interest.InterestRateService

import java.time.Instant

import static hk.alien.cryptotest.instrument.OptionSide.*
import hk.alien.cryptotest.marketdata.MarketDataUpdate
import spock.lang.Specification

import java.time.LocalDate

class EuropeanOptionPricingStrategyTest extends Specification {
    def instrumentRepository = Mock(InstrumentRepository)
    def businessDateCalendar = Mock(BusinessDateCalendar)
    def interestRateService = Mock(InterestRateService)
    def strategy = new EuropeanOptionPricingStrategy(instrumentRepository, businessDateCalendar, interestRateService)

    def "basic case"(   BigDecimal strike,
                        LocalDate expiryDate,
                        OptionSide optionSide,
                        BigDecimal stockPrice,
                        BigDecimal stockVolatility,
                        BigDecimal interestRate,
                        BigDecimal expectedPrice) {

        given:
        def option = createOption("OPTION", optionSide, strike, expiryDate, "STOCK")
        def stock  = createStock ("STOCK", stockVolatility)
        def businessDate = LocalDate.of(2022, 3, 31)

        // stubbing mocked dependencies
        instrumentRepository.findById("OPTION") >> Optional.of(option)
        instrumentRepository.findById("STOCK") >> Optional.of(stock)
        businessDateCalendar.getBusinessDate() >> businessDate
        interestRateService.getRate() >> interestRate

        when:
        def stockMarketDataUpdate = new MarketDataUpdate("STOCK", Instant.now(), stockPrice)
        def stockPricing = new Pricing("STOCK", stockPrice)
        def price = strategy.calculatePrice(option, stockMarketDataUpdate, stockPricing)

        then: "expected and calculated result should be within .01% difference"
        //(price - expectedPrice) / price < 0.0001
        price == expectedPrice


        /*
         * d1 =  ( ln(S/K) + (r + z^2/2) * t ) / (z * t^(1/2))
         * d2 = d1 - z * (t^(1/2))
         *
         * Call price = S * N(d1) - K * e^(-rt) * N(d2)
         * Put Price  = K * e^(-rt) * N(-d2) -  S * N(-d1)
         *
           CASE  1 & 2:
           t = 579days/365days = 1.586301369863014 years
           d1 = (-0.133531392624523 + 0.230013698630137) / 0.629742282577368
                = 0.153209191561249
           d2 = 0.153209191561249 - 0.629742282577368
                = -0.476533091016119

           cndf:
                d1 : 0.5608833622568554
                -d1: 0.4391166377431446
                d2 : 0.31684732535467197
                -d2: 0.683152674645328
           Call = 350 * 0.5608833622568554 - 400 * e^(- 0.02 * 1.586301369863014 ) * 0.31684732535467197
               = 196.30917678989939 - 400 * 0.96877196270011 * 0.31684732535467197
               = 73.528054685849034
           Put =  400 * e^(- 0.02 * 1.586301369863014 ) * 0.683152674645328 - 350 *  0.4391166377431446
              = 111.036839765893022

           CASE  3 & 4:
           t =  579days/365days
             = 1.586301369863014 years
           d1 = (0.117783035656383 + 0.230013698630137) / 0.629742282577368
                = 0.552284234215751
           d2 = 0.552284234215751 - 0.629742282577368
                = -0.077458048361617

           cndf:
            d1 : 0.7096232179836963
            -d1: 0.2903767820163037
            d2 : 0.46912951266031644
            -d2: 0.5308704873396836
           Call = 450 * 0.7096232179836963 - 400 * e^(- 0.02 * 1.586301369863014 ) * 0.46912951266031644
               = 137.538640596470991
           Put =  400 * e^(- 0.02 * 1.586301369863014 ) * 0.5308704873396836 - 450 *  0.2903767820163037 = 75.047425676515006

         */

        where:
        strike  | expiryDate         | optionSide   | stockPrice | stockVolatility | interestRate || expectedPrice
        400     | date('2023-10-31') | CALL         | 350        | 0.5             | 0.02         || 73.52805469
        400     | date('2023-10-31') | PUT          | 350        | 0.5             | 0.02         || 111.03683977
        400     | date('2023-10-31') | CALL         | 450        | 0.5             | 0.02         || 137.53864060
        400     | date('2023-10-31') | PUT          | 450        | 0.5             | 0.02         || 75.04742568
        400     | date('1999-01-01') | CALL         | 350        | 0.5             | 0.02         || 0
        400     | date('1999-01-01') | PUT          | 350        | 0.5             | 0.02         || 0

    }

    def Instrument createStock(String ticker, BigDecimal volatility) {
        Instrument i =  new Instrument(InstrumentType.STOCK, ticker);
        i.annualizedStandardDeviation = volatility;

        return i;
    }

    def Instrument createOption(String ticker, OptionSide optionSide, BigDecimal strike, LocalDate expiryDate, String underlying) {
        Instrument i = new Instrument(InstrumentType.EUROPEAN_OPTION, ticker);
        i.optionSide = optionSide
        i.strike = strike
        i.expiryDate = expiryDate
        i.underlying = underlying
        return i;
    }

    def LocalDate date(String d) {
        return LocalDate.parse(d)
    }
}
