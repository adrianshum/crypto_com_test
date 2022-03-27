package hk.alien.cryptotest.pricing;

import hk.alien.cryptotest.ServiceState;
import hk.alien.cryptotest.bizdate.BusinessDateCalendar;
import hk.alien.cryptotest.instrument.Instrument;
import hk.alien.cryptotest.instrument.InstrumentRepository;
import hk.alien.cryptotest.instrument.InstrumentType;
import hk.alien.cryptotest.marketdata.MarketDataInitialSnapshotEndEvent;
import hk.alien.cryptotest.marketdata.MarketDataSubscriber;
import hk.alien.cryptotest.marketdata.MarketDataUpdate;
import hk.alien.cryptotest.marketdata.MarketDataUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pricing Service which subscribe market data update, and evaluate pricing
 * for supported instruments, using pricing strategy configured based on instrument type.
 *
 * Any pricing update will be pushed to subscribers of Pricing Channel.
 */
public class PricingServiceImpl implements PricingChannel, MarketDataSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(PricingServiceImpl.class);
    private volatile ServiceState state = ServiceState.INITIALIZING;

    private InstrumentRepository instrumentRepository;

    private BusinessDateCalendar businessDateCalendar;

    private List<PricingSubscriber> subscribers = new ArrayList<>();

    private Map<InstrumentType, PricingStrategy> strategies= new HashMap<>();

    private Map<String, Pricing> pricings = new ConcurrentHashMap<>();    // Instrument ID vs Pricing


    public void setSubscribers(List<PricingSubscriber> subscribers) {
        subscribers.forEach(this::subscribe);
    }

    public void setInstrumentRepository(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    public void setBusinessDateCalendar(BusinessDateCalendar businessDateCalendar) {
        this.businessDateCalendar = businessDateCalendar;
    }

    public void setStrategies(Map<InstrumentType, PricingStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public void subscribe(PricingSubscriber subscriber) {
        // send initial snapshot to subscriber
        synchronized (subscribers) {
            this.subscribers.add(subscriber);
            subscriber.onPricingUpdate(new PricingUpdateEvent(null, this.pricings.values()));

            if (this.state == ServiceState.RUNNING) {
                subscriber.onPricingInitialSnapshotEnd(new PricingInitialSnapshotEndEvent());
            }
        }
    }

    @Override
    public void unsubscribe(PricingSubscriber subscriber) {
        this.subscribers.remove(subscriber);
    }

    @Override
    public void onMarketDataInitialSnapshotEnd(MarketDataInitialSnapshotEndEvent event) {
        logger.info("onMarketDataInitialSnapshotEnd");
        PricingInitialSnapshotEndEvent snapshotEnd = new PricingInitialSnapshotEndEvent();
        this.subscribers.forEach(s -> s.onPricingInitialSnapshotEnd(snapshotEnd));
        state=ServiceState.RUNNING;
    }

    @Override
    public void onMarketDataUpdate(MarketDataUpdateEvent marketDataUpdateEvent) {
        MarketDataUpdate marketDataUpdate = marketDataUpdateEvent.getMarketDataUpdate();
        logger.info("onMarketDataUpdate {} {}", marketDataUpdate.getTicker(), marketDataUpdate.getPrice());
        Collection<Pricing> updatedPricings = updatePricings(marketDataUpdate);
        logger.debug("onMarketDataUpdate: updated pricings {}", updatedPricings.size() );
        if (this.state == ServiceState.RUNNING
                && updatedPricings != null
                && ! updatedPricings.isEmpty()) {
            publishPricingUpdateEvent(marketDataUpdate, updatedPricings);
        }
    }

    private void publishPricingUpdateEvent(MarketDataUpdate marketDataUpdate, Collection<Pricing> updatedPricings) {
        logger.debug("publish pricing");
        synchronized (subscribers) {
            PricingUpdateEvent event = new PricingUpdateEvent(marketDataUpdate, updatedPricings);
            for (PricingSubscriber s : this.subscribers) {
                s.onPricingUpdate(event);
            }
        }
    }

    private Collection<Pricing> updatePricings(MarketDataUpdate marketDataUpdate) {
        Optional<Instrument> i = this.instrumentRepository.findById(marketDataUpdate.getTicker());
        if ( ! i.isPresent()) {
            logger.warn("Instrument {} not found. Skip pricing calculation", marketDataUpdate.getTicker());
            return Collections.EMPTY_LIST;
        }

        Instrument instrument = i.orElse(null);

        if (instrument.getType() != InstrumentType.STOCK) {
            logger.warn("Currently only support updating pricing based on stock market data update, but {} of type {} received",
                    instrument.getTicker(), instrument.getType());
            return Collections.EMPTY_LIST;
        }


        List<Pricing> updatedPricings = new ArrayList<>();

        // updating pricing of the stock itself
        Pricing stockPricing = computePricingForInstrument( instrument, marketDataUpdate, null);
        updatedPricings.add(stockPricing);

        // update pricing of derivatives
        List<Instrument> derivatives = instrumentRepository.findByUnderlying(instrument.getTicker());
        for (Instrument d: derivatives) {
            updatedPricings.add(computePricingForInstrument(d, marketDataUpdate, stockPricing));
        }
        return updatedPricings;
    }

    private Pricing computePricingForInstrument(Instrument d, MarketDataUpdate marketDataUpdate, Pricing underlyingPricing) {
        return this.pricings.compute(d.getTicker(),
                (k, pricing) -> {
                    logger.debug("Updating Pricing of {}", k);
                    BigDecimal price = this.strategies.get(d.getType())
                            .calculatePrice(d, marketDataUpdate, underlyingPricing);
                    logger.debug("Updating Pricing of {} - {} ", k, price);
                    return new Pricing(d.getTicker(), price);
                });
    }
}
