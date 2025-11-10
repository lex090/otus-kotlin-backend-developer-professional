-- liquibase formatted sql

-- changeset postgres-repo:3
-- Составной индекс для фильтрации по временным меткам
-- Ускоряет запросы на поиск активных/закрытых возможностей
CREATE INDEX idx_timestamps ON arbitrage_opportunities(start_timestamp, end_timestamp);

-- changeset postgres-repo:4
-- Составной индекс для комбинированных фильтров
-- Ускоряет запросы типа "токен + минимальный спред"
CREATE INDEX idx_token_spread ON arbitrage_opportunities(token_id, spread DESC);

-- changeset postgres-repo:5
-- Составной индекс для поиска по биржам и спреду
-- Ускоряет запросы типа "возможности для конкретной биржи с высоким спредом"
CREATE INDEX idx_buy_exchange_spread ON arbitrage_opportunities(buy_exchange_id, spread DESC);
CREATE INDEX idx_sell_exchange_spread ON arbitrage_opportunities(sell_exchange_id, spread DESC);
