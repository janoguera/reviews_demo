CREATE TABLE account (
    id                BIGINT          PRIMARY KEY,
    external_id       VARCHAR(64),
    currency_code     VARCHAR(3)      NOT NULL,
    balance           NUMERIC(19,4)   NOT NULL,
    status            VARCHAR(20)     NOT NULL,
    creation_date     TIMESTAMP       NOT NULL,
    last_modified_date TIMESTAMP      NOT NULL
);

CREATE TABLE account_transaction (
    id                BIGINT          PRIMARY KEY,
    account_id        BIGINT          NOT NULL,
    type              VARCHAR(20)     NOT NULL,
    amount            NUMERIC(19,4)   NOT NULL,
    creation_date     TIMESTAMP       NOT NULL,
    last_modified_date TIMESTAMP      NOT NULL
);

CREATE TABLE currency (
    code        VARCHAR(3)   PRIMARY KEY,
    name        VARCHAR(64)  NOT NULL,
    minor_units INT          NOT NULL
);

INSERT INTO currency (code, name, minor_units) VALUES
    ('EUR', 'Euro',           2),
    ('USD', 'US Dollar',      2),
    ('JPY', 'Japanese Yen',   0),
    ('GBP', 'Pound Sterling', 2);
