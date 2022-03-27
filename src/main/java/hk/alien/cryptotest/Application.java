package hk.alien.cryptotest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import hk.alien.cryptotest.bizdate.BusinessDateCalendar;
import hk.alien.cryptotest.bizdate.SimpleBusinessDateCalendar;
import hk.alien.cryptotest.instrument.InstrumentRepository;
import hk.alien.cryptotest.instrument.InstrumentType;
import hk.alien.cryptotest.marketdata.MarketDataChannel;
import hk.alien.cryptotest.marketdata.MarketDataUpdate;
import hk.alien.cryptotest.portfolio.ConsolePortfolioView;
import hk.alien.cryptotest.portfolio.Portfolio;
import hk.alien.cryptotest.portfolio.PortfolioEntry;
import hk.alien.cryptotest.portfolio.PortfolioView;
import hk.alien.cryptotest.pricing.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    /*
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }

        };
    }
    */

    @Bean
    public PricingServiceImpl pricingService(
            @Autowired Map<InstrumentType, PricingStrategy> pricingStrategies,
            @Autowired InstrumentRepository instrumentRepository,
            @Autowired BusinessDateCalendar businessDateCalendar,
            @Autowired MarketDataChannel marketDataChannel) {
        PricingServiceImpl pricingService = new PricingServiceImpl();
        pricingService.setStrategies(pricingStrategies);
        pricingService.setInstrumentRepository(instrumentRepository);
        pricingService.setBusinessDateCalendar(businessDateCalendar);

        marketDataChannel.subscribe(pricingService);
        return pricingService;
    }

    @Bean
    public Map<InstrumentType, PricingStrategy> pricingStrategies(
            @Autowired InstrumentRepository instrumentRepository ,
            @Autowired BusinessDateCalendar businessDateCalendar) {
        Map<InstrumentType, PricingStrategy> strategyMap = new ConcurrentHashMap<>();
        strategyMap.put(InstrumentType.STOCK, new EquityPricingStrategy());
        strategyMap.put(InstrumentType.EUROPEAN_OPTION, new EuropeanOptionPricingStrategy(instrumentRepository, businessDateCalendar));
        return strategyMap;
    }

    @Bean
    public BusinessDateCalendar businessDateCalendar() {
        return new SimpleBusinessDateCalendar();
    }

    @Bean
    public DummyPricingSubscriber pricingSubscriber (@Autowired PricingChannel pricingChannel) {
        DummyPricingSubscriber subscriber = new DummyPricingSubscriber();
        pricingChannel.subscribe(subscriber);
        return subscriber;
    }

    @Bean
    public Portfolio portfolio(
            @Value("${portfolio.file:classpath:portfolio.csv}") Resource file,
            @Autowired PortfolioView view,
            @Autowired PricingChannel pricingChannel ) throws IOException {
        List<PortfolioEntry> entries = new ArrayList<>();
        try ( BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream())) ) {
            reader.lines().forEach(l -> {
                String[] fields = l.split(",");
                if (fields.length >= 2) {
                    String ticker = fields[0];
                    BigDecimal position = new BigDecimal(fields[1]);
                    entries.add(new PortfolioEntry(ticker, position));
                }
            });
        }
        Portfolio portfolio = new Portfolio();
        portfolio.setEntries(entries);
        portfolio.setViews(Arrays.asList(view));

        pricingChannel.subscribe(portfolio);

        return portfolio;
    }

    @Bean
    public PortfolioView portfolioView() {
        return new ConsolePortfolioView();
    }

}