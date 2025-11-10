-- liquibase formatted sql

-- changeset postgres-repo:3
-- comment: Замена optimistic locking с integer версии на UUID токен
-- Это изменение улучшает безопасность и уменьшает вероятность коллизий при concurrent updates

-- Добавляем новую колонку lock_token
ALTER TABLE arbitrage_opportunities
    ADD COLUMN lock_token VARCHAR(36);

-- changeset postgres-repo:4
-- comment: Генерируем UUID для существующих записей и устанавливаем NOT NULL
-- Используем gen_random_uuid() если доступно (PostgreSQL 13+), иначе uuid-ossp extension

-- Обновляем существующие записи новыми UUID
UPDATE arbitrage_opportunities
SET lock_token = gen_random_uuid()::text
WHERE lock_token IS NULL;

-- changeset postgres-repo:5
-- comment: Делаем lock_token обязательным полем

ALTER TABLE arbitrage_opportunities
    ALTER COLUMN lock_token SET NOT NULL;

-- changeset postgres-repo:6
-- comment: Удаляем старую колонку lock_version

ALTER TABLE arbitrage_opportunities
    DROP COLUMN lock_version;
