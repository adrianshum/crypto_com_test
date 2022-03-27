package hk.alien.cryptotest.bizdate;

import java.time.LocalDate;

public class SimpleBusinessDateCalendar implements BusinessDateCalendar {

    private LocalDate businessDate;

    public SimpleBusinessDateCalendar() {
        this(LocalDate.now());
    }

    public SimpleBusinessDateCalendar(LocalDate businessDate) {
        this.businessDate = businessDate;
    }

    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
    }

    @Override
    public LocalDate getBusinessDate() {
        return this.businessDate;
    }
}
