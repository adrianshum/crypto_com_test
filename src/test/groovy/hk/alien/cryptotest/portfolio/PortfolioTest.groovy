package hk.alien.cryptotest.portfolio


import hk.alien.cryptotest.marketdata.MarketDataUpdate
import hk.alien.cryptotest.pricing.Pricing
import hk.alien.cryptotest.pricing.PricingInitialSnapshotEndEvent
import hk.alien.cryptotest.pricing.PricingUpdateEvent
import spock.lang.Specification
import java.time.Instant

class PortfolioTest extends Specification {

    def view = Mock(PortfolioView)

    def "update before initialized" (){
        given:
        def portfolio = new Portfolio()
        portfolio.setViews([ view ])
        portfolio.setEntries([
            entry("A", BigDecimal.valueOf(10)) ,
            entry("B", BigDecimal.valueOf(20)) ,
            entry("C", BigDecimal.valueOf(30)) ,

        ])



        when:
        portfolio.onPricingUpdate(new PricingUpdateEvent(
                new MarketDataUpdate("A", Instant.now(), BigDecimal.valueOf(10)),
                [
                        new Pricing("A", BigDecimal.valueOf(10)),
                        new Pricing("B", BigDecimal.valueOf(20)),
                ]))

        then:
        1 * view.onPricingUpdate( {
            it.marketDataUpdate.ticker == "A"
            it.pricings.size == 2
            it.pricings[0].ticker == "A"
            it.pricings[0].price == BigDecimal.valueOf(10)

        })

        then:
        0 * view.onPortfolioUpdate(_)

    }

    def "update after pricing snapshot end" (){
        given:
        def portfolio = new Portfolio()
        portfolio.setViews([ view ])
        portfolio.setEntries([
                entry("A", BigDecimal.valueOf(10)) ,
                entry("B", BigDecimal.valueOf(-20)) ,
                entry("C", BigDecimal.valueOf(30) ) ,

        ])



        when: "price update before snapshot end"
        portfolio.onPricingUpdate(new PricingUpdateEvent(
                new MarketDataUpdate("A", Instant.now(), BigDecimal.valueOf(10)),
                [
                        new Pricing("A", BigDecimal.valueOf(10)),
                        new Pricing("B", BigDecimal.valueOf(20)),
                ]))
        and: "snapshot end"
        portfolio.onPricingInitialSnapshotEnd(new PricingInitialSnapshotEndEvent())

        def navBeforeUpdate = portfolio.nav

        and: "realtime price update  A->50"
        portfolio.onPricingUpdate(new PricingUpdateEvent(
                new MarketDataUpdate("A", Instant.now(), BigDecimal.valueOf(50)),
                [
                        new Pricing("A", BigDecimal.valueOf(50)),
                ]))

        def navAfterUpdate = portfolio.nav


        then: "First price update before snapshot end"
        1 * view.onPricingUpdate( {
            it.marketDataUpdate.ticker == "A"
            && it.pricings.size == 2
            && it.pricings[0].ticker == "A"
            && it.pricings[0].price == BigDecimal.valueOf(10)

        })

        then: "portfolio update triggered by pricing snapshot end"
        1* view.onPortfolioUpdate( _ )

        then: "realtime price update "
        1* view.onPricingUpdate({
            it.marketDataUpdate.ticker == "A"
            && it.pricings.size == 1
            && it.pricings[0].ticker == "A"
            && it.pricings[0].price == BigDecimal.valueOf(50)

        })

        then: "portfolio update triggered by realtime price update"
        1* view.onPortfolioUpdate(_ )

        and: "nav before update"
        navBeforeUpdate == BigDecimal.valueOf(10 * 10 + -20 * 20 + 30 * 0)

        and: "nav after update"
        navAfterUpdate == BigDecimal.valueOf(10 * 50 + -20 * 20 + 30 * 0)

    }

    def PortfolioEntry entry(String ticker, BigDecimal qty) {
        return entry(ticker, qty, null)
    }

    def PortfolioEntry entry(String ticker, BigDecimal qty, BigDecimal price) {
        Pricing p = null
        if (price != null) {
            p = new Pricing(ticker, price, null)
        }

        PortfolioEntry entry = new PortfolioEntry(ticker, qty, p);
    }
}
