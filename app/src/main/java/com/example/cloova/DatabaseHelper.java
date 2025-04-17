package com.example.cloova;
/*
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    // --- Настройки Базы Данных ---
    private static final String DATABASE_NAME = "CloovaApp.db"; // Имя файла БД
    private static final int DATABASE_VERSION = 1; // Версия (меняйте, если меняете структуру)

    // --- Настройки Таблицы Пользователей ---
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "_id"; // Обязательный стандартный первичный ключ (хз чё это значит)
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password"; // !!! ВАЖНО: Здесь надо будет хранить ХЭШ пользователя !!!

    // --- SQL для создания таблицы ---
    private static final String SQL_CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + // Авто-ID
                    COLUMN_USERNAME + " TEXT UNIQUE NOT NULL," +       // Логин (уникальный, не пустой)
                    COLUMN_PASSWORD + " TEXT NOT NULL);";              // Пароль (не пустой)

    // --- Конструктор ---
    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("DB_HELPER", "DatabaseHelper created. DB: " + DATABASE_NAME + " V: " + DATABASE_VERSION); //Ввыводит в логи некоторое сообщение
    }

    // --- Метод onCreate ---
    // Вызывается САМ, ТОЛЬКО когда файл DATABASE_NAME создается ВПЕРВЫЕ
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DB_HELPER", "onCreate called. Creating table: " + TABLE_USERS);
        db.execSQL(SQL_CREATE_USERS_TABLE); // Выполняем наш SQL для создания таблицы
        Log.d("DB_HELPER", "Table " + TABLE_USERS + " created.");
    }

    // --- Метод onUpgrade ---
    // Вызывается САМ, если DATABASE_VERSION в коде БОЛЬШЕ, чем версия в существующем файле БД
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("DB_HELPER", "onUpgrade called. Upgrading from V:" + oldVersion + " to V:" + newVersion);
        // Простейший вариант: удалить старую таблицу и создать новую
        // !!! ВНИМАНИЕ: ВСЕ ДАННЫЕ БУДУТ УДАЛЕНЫ ПРИ ОБНОВЛЕНИИ !!!
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db); // Создаем таблицу заново
    }

    // --- Метод для РЕГИСТРАЦИИ (добавления пользователя) ---
    public boolean addUser(String username, String password) {
        // !!! ПЕРЕД ЭТИМ ШАГОМ ПАРОЛЬ НУЖНО ЗАХЭШИРОВАТЬ !!!
        // String hashedPassword = hashFunction(password);

        SQLiteDatabase db = this.getWritableDatabase(); // Получаем БД для ЗАПИСИ
        ContentValues values = new ContentValues();     // Контейнер для данных
        values.put(COLUMN_USERNAME, username);          // Кладем логин
        values.put(COLUMN_PASSWORD, password);          // Кладем пароль (или его хэш)

        // Вставляем строку. Флаг CONFLICT_IGNORE не даст вставить, если username уже есть.
        long result = db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_IGNORE);

        if (result == -1) {
            Log.e("DB_HELPER", "Ошибка или такой логин уже занят: " + username);
            return false;
        } else {
            Log.d("DB_HELPER", "Данные успешно добавлены: " + username + " (ID: " + result + ")");
            return true;
        }
    }
    public boolean checkUser(String username, String enteredPassword) {
        // !!! ЗДЕСЬ НУЖНО СРАВНИВАТЬ ХЭШ ВВЕДЕННОГО ПАРОЛЯ С ХЭШЕМ В БАЗЕ !!!
        // 1. Получить хэш из БД по username
        // 2. Сравнить: checkHash(enteredPassword, hashFromDb)
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = {COLUMN_ID}; // Нам достаточно знать, есть ли такой ID
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?"; // Условие поиска
        String[] selectionArgs = {username, enteredPassword}; // Значения для условия (пока сравниваем пароли напрямую)

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            int count = cursor.getCount(); // Сколько строк найдено по условию?
            Log.d("DB_HELPER", "Checking user: " + username + ". Found rows: " + count);
            return count > 0; // Если больше 0, значит пользователь с таким логином и паролем есть
        } catch (Exception e) {
            Log.e("DB_HELPER", "Error checking user", e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close(); // !!! ОБЯЗАТЕЛЬНО ЗАКРЫВАТЬ КУРСОР !!!
                Log.d("DB_HELPER", "Cursor closed for checkUser.");
            }
        }
    }

}
*/


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CloovaDB.db";
    private static final int DATABASE_VERSION = 1;

    // Таблица пользователей
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CITY = "city";
    private static final String COLUMN_BIRTH_DATE = "birth_date";
    private static final String COLUMN_PROFILE_IMAGE = "profile_image";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_CITY + " TEXT,"
                + COLUMN_BIRTH_DATE + " TEXT,"
                + COLUMN_PROFILE_IMAGE + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public long addUser(String username, String password, String name,
                        String city, String birthDate, String profileImagePath) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Проверяем, существует ли пользователь
        if (checkUser(username, password)) {
            return -1; // Пользователь уже существует
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password); // В реальном приложении нужно хешировать!
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_CITY, city);
        values.put(COLUMN_BIRTH_DATE, birthDate);
        values.put(COLUMN_PROFILE_IMAGE, profileImagePath);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    public boolean checkUser(String login, String enteredPassword) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + " = ?",
                new String[]{login},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}