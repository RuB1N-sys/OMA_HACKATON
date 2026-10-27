/**
 * OMA ACADEMY — Конфигурация клиента
 * Автоматически адаптируется под окружение:
 * - В Docker Nginx (порты 80, 3000 или тот же хост): относительный путь /api/ (без CORS)
 * - При локальной разработке с отдельного сервера (Live Server, python 8000): http://localhost:8080
 */
window.OMA_CONFIG = {
  API_URL: (
    window.location.port === '' ||
    window.location.port === '80' ||
    window.location.port === '3000' ||
    window.location.port === '8080'
  ) ? '' : 'http://localhost:8080'
};
