INSERT INTO instrument(ticker, instrument_type, expected_return, annual_standard_dev, option_side, strike, expiry_date, underlying)
VALUES ('AAPL', 'STOCK', 10, 0.2, NULL, NULL, NULL, NULL),
       ('TELSA', 'STOCK', 10, 0.2, NULL, NULL, NULL, NULL),
       ('AAPL-OCT-2023-110-C', 'EUROPEAN_OPTION', 0.5, 0.5, 'CALL', 110, DATE '2023-10-31', 'AAPL'),
       ('AAPL-OCT-2023-110-P', 'EUROPEAN_OPTION', 0.5, 0.5, 'PUT',  110, DATE '2023-10-31', 'AAPL'),
       ('TELSA-NOV-2023-400-C', 'EUROPEAN_OPTION', 0.5, 0.5, 'CALL', 400, DATE '2023-11-30', 'TELSA'),
       ('TELSA-NOV-2023-400-P', 'EUROPEAN_OPTION', 0.5, 0.5, 'PUT',  400, DATE '2023-11-30', 'TELSA'),
       ('TELSA-DEC-2023-400-C', 'EUROPEAN_OPTION', 0.5, 0.5, 'CALL', 400, DATE '2023-12-31', 'TELSA'),
       ('TELSA-DEC-2023-400-P', 'EUROPEAN_OPTION', 0.5, 0.5, 'PUT',  400, DATE '2023-12-31', 'TELSA');

