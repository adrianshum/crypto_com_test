package hk.alien.cryptotest.portfolio;


import hk.alien.cryptotest.pricing.Pricing;
import hk.alien.cryptotest.pricing.PricingInitialSnapshotEndEvent;
import hk.alien.cryptotest.pricing.PricingSubscriber;
import hk.alien.cryptotest.pricing.PricingUpdateEvent;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entity for Portfolio.
 *
 * Portfolio-PortfolioView is a basic & typical Model-View design.
 */
public class Portfolio implements PricingSubscriber {

    protected Map<String, PortfolioEntry> entries = new LinkedHashMap<>();
    protected List<PortfolioView> views = new ArrayList<>();
    protected volatile boolean initialized = false;

    public void setEntries(Collection<PortfolioEntry> entries) {
        if (entries != null) {
            for (PortfolioEntry e: entries) {
                this.entries.put(e.getTicker(), e);
            }
        }
    }

    public Collection<PortfolioEntry> getEntries() {
        return this.entries.values();
    }


    public Map<String, PortfolioEntry> getEntriesMap() {
        return this.entries;
    }

    public void setViews(List<PortfolioView> views) {
        this.views = views;
    }

    public BigDecimal getNav() {
        BigDecimal nav = BigDecimal.ZERO;
        for (PortfolioEntry entry : this.entries.values()) {
            nav = nav.add(entry.getMarketValue());
        }
        return nav;
    }

    @Override
    public void onPricingInitialSnapshotEnd(PricingInitialSnapshotEndEvent event) {
        this.initialized =true;
        publishPortfolioToView();
    }

    @Override
    public void onPricingUpdate(PricingUpdateEvent event) {
        for (Pricing pricing : event.getPricings()) {
            entries.computeIfPresent(pricing.getTicker(), (k,entry) -> {
                entry.setPricing(pricing);
                return entry;
            });
        }


        this.views.forEach(v -> v.onPricingUpdate(event));

        publishPortfolioToView();
    }

    protected void publishPortfolioToView(){
        if (initialized) {
            this.views.forEach(v -> v.onPortfolioUpdate(this));
        }
    }
}
