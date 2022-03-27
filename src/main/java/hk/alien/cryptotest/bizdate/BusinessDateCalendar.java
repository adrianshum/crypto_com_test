package hk.alien.cryptotest.bizdate;

import java.time.LocalDate;

/**
 * Calendar for business date.  Provide way to retrieve current business date.
 */
public interface BusinessDateCalendar {
    /**
     * Current Business Date
     *
     * @return
     */
    LocalDate getBusinessDate();
}
