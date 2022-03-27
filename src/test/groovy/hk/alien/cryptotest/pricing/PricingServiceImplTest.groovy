package hk.alien.cryptotest.pricing

import hk.alien.cryptotest.bizdate.BusinessDateCalendar
import hk.alien.cryptotest.instrument.Instrument
import hk.alien.cryptotest.instrument.InstrumentRepository
import hk.alien.cryptotest.marketdata.MarketDataInitialSnapshotEndEvent

import java.time.Instant

import hk.alien.cryptotest.instrument.InstrumentType
import hk.alien.cryptotest.marketdata.MarketDataUpdate
import hk.alien.cryptotest.marketdata.MarketDataUpdateEvent
import spock.lang.Specification

import java.time.LocalDate

class PricingServiceImplTest extends Specification {
    def InstrumentRepository instrumentRepository = Mock(InstrumentRepository)
    def PricingSubscriber subscriber = Mock(PricingSubscriber)
    def pricingService = new PricingServiceImpl()
    def stockPricingStrategy = Mock(PricingStrategy)
    def optionPricingStrategy = Mock(PricingStrategy)
    def businessDateCalendar = Mock(BusinessDateCalendar)

    def setup() {
        businessDateCalendar.getBusinessDate() >> LocalDate.of(2000, 1, 1)
        pricingService.businessDateCalendar = businessDateCalendar

        pricingService.strategies = [ (InstrumentType.STOCK) : stockPricingStrategy,
                                      (InstrumentType.EUROPEAN_OPTION) : optionPricingStrategy]

        pricingService.instrumentRepository = instrumentRepository
    }

    def "market data update triggers pricing update of all related instruments"() {
        given:
        defaultInstrumentRepository()
        pricingService.subscribe(subscriber)

        when:
        pricingService.onMarketDataInitialSnapshotEnd(new MarketDataInitialSnapshotEndEvent())
        pricingService.onMarketDataUpdate(new MarketDataUpdateEvent(new MarketDataUpdate("stock", Instant.now(), BigDecimal.valueOf(10))))

        then:
        1 * subscriber.onPricingInitialSnapshotEnd( _ )

        then: "Strategy invoked"
        1 * stockPricingStrategy.calculatePrice(_, _, _)
        2 * optionPricingStrategy.calculatePrice(_, _, _)

        then: "subscriber updated with all 3 pricing updates in one event"
        1 * subscriber.onPricingUpdate( {
            it.pricings.size == 3
        })
    }

    def "market data update for unknown instrument will NOT triggers pricing update"() {
        given:
        defaultInstrumentRepository()
        pricingService.subscribe(subscriber)

        when:
        pricingService.onMarketDataInitialSnapshotEnd(new MarketDataInitialSnapshotEndEvent())
        pricingService.onMarketDataUpdate(new MarketDataUpdateEvent(new MarketDataUpdate("unknown", Instant.now(), BigDecimal.valueOf(10))))

        then:
        1 * subscriber.onPricingInitialSnapshotEnd( _ )

        then: "No Strategy invoked"
        0 * stockPricingStrategy.calculatePrice(_, _, _)
        0 * optionPricingStrategy.calculatePrice(_, _, _)

        then: "No subscriber updated"
        0 * subscriber.onPricingUpdate( _ )
    }


    def defaultInstrumentRepository() {
        def stock = new Instrument(InstrumentType.STOCK, "stock")

        def opt1 = new Instrument(InstrumentType.EUROPEAN_OPTION, "opt1")
        opt1.underlying = "STOCK"

        def opt2 = new Instrument(InstrumentType.EUROPEAN_OPTION, "opt2")
        opt2.underlying = "STOCK"

        instrumentRepository.findById("stock") >> Optional.of(stock)
        instrumentRepository.findById("opt1") >> Optional.of(opt1)
        instrumentRepository.findById("opt2") >> Optional.of(opt2)
        instrumentRepository.findById(_) >> Optional.empty()
        instrumentRepository.findByUnderlying("stock") >> [opt1, opt2]
    }


}
