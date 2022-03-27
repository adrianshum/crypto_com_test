package hk.alien.cryptotest.instrument;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class InstrumentJdbcRepository implements InstrumentRepository {
    @Autowired
    private NamedParameterJdbcOperations jdbcTemplate;

    private RowMapper<Instrument> instrumentRowMapper = (row, rowNum)->{
        Instrument i = new Instrument();
        i.setTicker(row.getString("ticker"));
        i.setType(InstrumentType.valueOf(row.getString("instrument_type")));
        i.setExpectedReturn(row.getBigDecimal("expected_return"));
        i.setAnnualizedStandardDeviation(row.getBigDecimal("annual_standard_dev"));
        String optionSide = row.getString("option_side");
        i.setOptionSide(optionSide == null ? null : OptionSide.valueOf(optionSide));
        i.setStrike(row.getBigDecimal("strike"));
        Date expiryDate = row.getDate("expiry_date");
        i.setExpiryDate(expiryDate == null ? null : expiryDate.toLocalDate());
        i.setUnderlying(row.getString("underlying"));

        return i;
    };

    public void setJdbcTemplate(NamedParameterJdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Instrument> findById(String id) {
        try {
            return Optional.of(jdbcTemplate.queryForObject("select * from instrument where ticker = :id",
                    new MapSqlParameterSource().addValue("id", id),
                    instrumentRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Instrument> findAll() {
        return jdbcTemplate.query("select * from instrument", instrumentRowMapper);
    }

    @Override
    public List<Instrument> findByUnderlying(String id) {
        return jdbcTemplate.query("select * from instrument where underlying = :id",
                new MapSqlParameterSource().addValue("id", id),
                instrumentRowMapper);
    }
}
