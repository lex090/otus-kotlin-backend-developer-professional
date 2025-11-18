# Импорт Saved Objects для Historical Analytics

## Описание

Файл `historical-analytics-saved-objects.ndjson` содержит экспортированные saved objects для анализа исторических данных в OpenSearch Dashboards.

## Содержимое экспорта

1. **Last 7 Days Logs** (search) - Поиск всех логов за последние 7 дней
2. **Error Logs History** (search) - Поиск логов с ошибками за последние 7 дней
3. **Daily Log Volume** (visualization) - Визуализация объема логов по дням (Bar Chart)
4. **Error Rate Trend** (visualization) - График тренда ошибок по часам (Line Chart)

## Импорт через UI

1. Открыть OpenSearch Dashboards: http://localhost:5601
2. Авторизоваться (admin/Admin123!)
3. Перейти в **Management** → **Dashboards Management** → **Saved Objects**
4. Нажать **Import**
5. Выбрать файл `historical-analytics-saved-objects.ndjson`
6. Нажать **Import**
7. При конфликтах выбрать действие (Overwrite/Skip)

## Импорт через API

```bash
curl -X POST "http://localhost:5601/api/saved_objects/_import" \
  -H 'osd-xsrf: true' \
  --form file=@historical-analytics-saved-objects.ndjson
```

## Использование после импорта

После импорта saved objects будут доступны:

- **Saved Searches**: **Discover** → кнопка **Open** → выбрать нужный поиск
- **Visualizations**: **Visualize** → **+** → выбрать существующую визуализацию
- Можно добавить визуализации в любой Dashboard через **Edit** → **Add** → **Add from library**

## Создание Dashboard с импортированными объектами

1. Перейти в **Dashboard** → **Create new dashboard**
2. Нажать **Add** → **Add from library**
3. Выбрать визуализации:
   - Daily Log Volume
   - Error Rate Trend
4. Настроить layout
5. Сохранить dashboard с именем "Historical Analytics Dashboard"

## Требования

- OpenSearch Dashboards 2.11.1+
- Index pattern `fluentbit-*` должен быть создан
- Данные в индексах `fluentbit-*` должны существовать
