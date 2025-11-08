-- liquibase formatted sql

-- changeset postgres-repo:1
CREATE TABLE arbitrage_opportunities (
    id VARCHAR(36) PRIMARY KEY,
    token_id VARCHAR(255) NOT NULL,
    buy_exchange_id VARCHAR(255) NOT NULL,
    sell_exchange_id VARCHAR(255) NOT NULL,
    buy_price_raw NUMERIC(30, 18) NOT NULL,
    sell_price_raw NUMERIC(30, 18) NOT NULL,
    spread DOUBLE PRECISION NOT NULL,
    start_timestamp BIGINT NOT NULL,
    end_timestamp BIGINT,
    lock_version BIGINT NOT NULL DEFAULT 0
);

-- changeset postgres-repo:2
CREATE INDEX idx_token_id ON arbitrage_opportunities(token_id);
CREATE INDEX idx_buy_exchange_id ON arbitrage_opportunities(buy_exchange_id);
CREATE INDEX idx_sell_exchange_id ON arbitrage_opportunities(sell_exchange_id);
CREATE INDEX idx_spread ON arbitrage_opportunities(spread);
