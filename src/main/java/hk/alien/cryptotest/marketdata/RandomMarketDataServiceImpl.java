package hk.alien.cryptotest.marketdata;

import hk.alien.cryptotest.Constants;
import hk.alien.cryptotest.instrument.Instrument;
import hk.alien.cryptotest.instrument.InstrumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

import static hk.alien.cryptotest.ServiceState.RUNNING;

/**
 * Market Data Service which will schedule random update based on geometric brownian motion
 */
@Component
public class RandomMarketDataServiceImpl extends AbstractMarketDataChannel {
    private static final Logger logger = LoggerFactory.getLogger(RandomMarketDataServiceImpl.class);

    @Autowired
    private InstrumentRepository instrumentRepository;
    // Ticker vs Latest Market Data
    private Map<String, MarketDataUpdate> marketDataEntries = new ConcurrentHashMap<>();

    private ScheduledExecutorService scheduler;

    @Value("${market-data.sod-file:classpath:marketDataSod.csv}")
    private Resource sodFile;

    public void setInstrumentRepository(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    @PostConstruct
    public void initialize() throws IOException {
        // read SOD price from file
        try ( BufferedReader reader = new BufferedReader(new InputStreamReader(sodFile.getInputStream())) ) {
            reader.lines().forEach(l -> {
                String[] fields = l.split(",");
                if (fields.length >= 2) {
                    String ticker = fields[0];
                    BigDecimal price = new BigDecimal(fields[1]);
                    this.marketDataEntries.put(ticker, new MarketDataUpdate(ticker, Instant.now(), price));
                }
            });
        }

        this.state = RUNNING;


        scheduler = Executors.newScheduledThreadPool(1,
                r -> {
                    Thread t = Executors.defaultThreadFactory().newThread(r);
                    t.setDaemon(true);
                    return t;
                });

        scheduleNextUpdate();
    }

    public void setSubscribers(List<MarketDataSubscriber> subscribers) {
        this.subscribers.forEach(this::subscribe);
    }

    @Override
    protected Collection<MarketDataUpdate> getInitialSnapshot() {
        return this.marketDataEntries.values();
    }

    protected void onPriceUpdate() {
        Instant now = Instant.now();

        this.marketDataEntries.replaceAll((k,v) -> {
            return updateMarketData(now, v);
        });

        logger.info(">>> Updated price :  " + this.marketDataEntries);
        this.marketDataEntries.values().forEach(
                this::publishMarketDataUpdate
        );
        scheduleNextUpdate();
    }

    protected MarketDataUpdate updateMarketData(Instant time, MarketDataUpdate oldMarketData) {
        BigDecimal oldPrice = oldMarketData.getPrice();
        Instant oldTime = oldMarketData.getTime();
        long timeDiffInMs = Duration.between(oldTime, time).toMillis();

        Instrument i = instrumentRepository.findById(oldMarketData.getTicker()).orElse(null);
        if (i == null) {
            logger.warn("No instrument found for {}. Skip.", oldMarketData.getTicker());
            // No instrument. Skip update
            return oldMarketData;
        }

        BigDecimal expectedReturn = i.getExpectedReturn() == null ? BigDecimal.ZERO : i.getExpectedReturn();
        BigDecimal annualizedStandardDeviation = i.getAnnualizedStandardDeviation() == null
                                                    ? BigDecimal.ZERO
                                                    : i.getAnnualizedStandardDeviation();

        // S = ( u * (t/7257600) + z * r * sqrt(t / 7257600) ) * S + S
        // r : random .  Assumption here is made to make it from -100 to +100
        BigDecimal adjTimeDiff = BigDecimal.valueOf(timeDiffInMs)
                .divide(BigDecimal.valueOf(1000))
                .divide(BigDecimal.valueOf(7257600), 16, RoundingMode.HALF_EVEN);
        BigDecimal newPrice = expectedReturn.multiply(adjTimeDiff)
                                .add(annualizedStandardDeviation
                                    .multiply(BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(-100, 100)))
                                    .multiply(BigDecimal.valueOf(Math.sqrt(adjTimeDiff.doubleValue()))))
                            .multiply(oldPrice).add(oldPrice)
                .setScale(Constants.MARKET_PRICE_DECIMAL, RoundingMode.HALF_EVEN);

        return new MarketDataUpdate(oldMarketData.getTicker(), time, newPrice);

    }

    private void scheduleNextUpdate() {
        int delay = ThreadLocalRandom.current().nextInt(500, 2000);

        this.scheduler.schedule(() -> {
            try {
                this.onPriceUpdate();
            } catch (Exception e) {
                logger.error("Unexpected Exception Caught", e);
                throw new RuntimeException(e);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

}
