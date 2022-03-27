package hk.alien.cryptotest.instrument;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Instrument.
 */
// NOTE: It is much cleaner and easier by using Spring Data JPA.  However, to simplify dependencies used in
//       the demo, we are only using JDBC
public interface InstrumentRepository  {
    Optional<Instrument> findById(String id);

    List<Instrument> findAll();

    List<Instrument> findByUnderlying(String id);
}
