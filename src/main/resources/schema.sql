
CREATE TABLE IF NOT EXISTS property (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    size          INT NOT NULL,
    bedrooms      DOUBLE NOT NULL,
    wc            DOUBLE NOT NULL,
    country       VARCHAR(100),
    city          VARCHAR(100),
    address       TEXT,
    mortgage      DOUBLE NOT NULL,
    utilities     DOUBLE NOT NULL,
    cash_invested DOUBLE NOT NULL,
    pool          BOOLEAN DEFAULT FALSE,
    garden        BOOLEAN DEFAULT FALSE,
    parking       BOOLEAN DEFAULT FALSE
);