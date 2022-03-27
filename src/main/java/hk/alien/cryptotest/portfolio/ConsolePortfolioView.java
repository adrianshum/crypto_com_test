package hk.alien.cryptotest.portfolio;

import hk.alien.cryptotest.marketdata.MarketDataUpdate;
import hk.alien.cryptotest.pricing.Pricing;
import hk.alien.cryptotest.pricing.PricingUpdateEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Portfolio View which print Portfolio and related Market Data Updates to standard out
 */
public class ConsolePortfolioView implements PortfolioView {

    // Market Updates pending display
    private List<MarketDataUpdate> marketDataUpdates = new ArrayList<>();


    @Override
    public void onPortfolioUpdate(Portfolio portfolio) {
        final String PORTFOLIO_HEADER = "symbol                         price             qty           value";
        final String PORTFOLIO_LINE_FORMAT = "%-20s %15.2f %15.2f %15.2f\n";
        final String PORTFOLIO_SUMMARY = "#Total portfolio                                    %15.2f\n";
        System.out.println("========================================");
        System.out.println("## Market Data Update");
        for (MarketDataUpdate marketData: marketDataUpdates) {
            System.out.println(marketData.getTicker() + " change to " + marketData.getPrice());
        }
        System.out.println();
        System.out.println("## Portfolio");
        System.out.println(PORTFOLIO_HEADER);
        for (PortfolioEntry entry : portfolio.getEntries()) {
            String symbol = entry.getTicker();
            BigDecimal price = entry.getPricing() == null ? BigDecimal.ZERO : entry.getPricing().getPrice();
            BigDecimal qty = entry.getPosition();
            BigDecimal value = entry.getMarketValue();
            System.out.printf(PORTFOLIO_LINE_FORMAT, symbol, price, qty, value);
        }
        System.out.println();
        System.out.printf(PORTFOLIO_SUMMARY, portfolio.getNav());
        System.out.println();


        this.marketDataUpdates.clear();
    }

    @Override
    public void onPricingUpdate(PricingUpdateEvent pricingUpdate) {
        if (pricingUpdate.getMarketDataUpdate() != null) {
            marketDataUpdates.add(pricingUpdate.getMarketDataUpdate());
        }
    }
}
