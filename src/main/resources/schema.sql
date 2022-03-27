CREATE TABLE instrument
(
    ticker              VARCHAR(100) PRIMARY KEY,
    instrument_type     VARCHAR(20) NOT NULL,
    expected_return     NUMERIC(20, 8) NULL,
    annual_standard_dev NUMERIC(20, 8) NULL,
    option_side         VARCHAR(10) NULL,
    strike              NUMERIC(20, 8) NULL,
    expiry_date         DATE NULL,
    underlying          VARCHAR(100)
);
