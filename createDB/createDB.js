const { Pool } = require('pg');

// Создание подключения к базе данных
const pool = new Pool({
    user: 'postgres',
    host: 'localhost',
    database: 'qiwi-proxy-emulator',
    password: 'psql',
    port: 5432, // Порт по умолчанию для PostgreSQL
});

// Функция для создания таблицы
async function createTable() {
    const client = await pool.connect();
    try {
        const createTableQuery = `
        CREATE TABLE IF NOT EXISTS cached_reqs (
            id SERIAL PRIMARY KEY,
            partner VARCHAR(255),
            method VARCHAR(255),
            url TEXT,
            body TEXT,
            createdAt TIMESTAMP,
            editedAt TIMESTAMP
        );

        CREATE TABLE IF NOT EXISTS cached_res (
            id SERIAL PRIMARY KEY,
            cached_reqs_id INTEGER UNIQUE,
            status_code INTEGER,
            body TEXT,
            createdAt TIMESTAMP,
            editedAt TIMESTAMP
        );
    `;
        await client.query(createTableQuery);
        console.log('Таблица cached_reqs успешно создана');
    } catch (error) {
        console.error('Ошибка при создании таблицы:', error);
    } finally {
        client.release();
    }
}

// Вызов функции для создания таблицы
createTable();