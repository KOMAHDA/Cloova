package com.example.cloova;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CloovaDB.db";
    private static final int DATABASE_VERSION = 3;

    // Таблица пользователей
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_LOGIN = "login";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_GENDER = "gender";
    private static final String COLUMN_BIRTH_DATE = "birth_date";
    private static final String COLUMN_CITY = "city";
    private static final String COLUMN_AVATAR = "avatar";

    // Таблица выбранных цветов
    private static final String TABLE_USER_COLORS = "user_colors";
    private static final String COLUMN_COLOR_ID = "color_id";
    private static final String COLUMN_COLOR_NAME = "color_name";

    // Таблица выбранных стилей
    private static final String TABLE_USER_STYLES = "user_styles";
    private static final String COLUMN_STYLE_ID = "style_id";
    private static final String COLUMN_STYLE_NAME = "style_name";

    // Таблица гардероба
    private static final String TABLE_WARDROBE = "wardrobe";
    private static final String COLUMN_CLOTHING_ID = "clothing_id";
    private static final String COLUMN_CLOTHING_NAME = "clothing_name";

    // Таблица аксессуаров
    private static final String TABLE_ACCESSORIES = "accessories";
    private static final String COLUMN_ACCESSORY_ID = "accessory_id";
    private static final String COLUMN_ACCESSORY_NAME = "accessory_name";

    public static final String COLUMN_LANGUAGE = "language";


    public static final String SHARED_PREFS_NAME = "CloovaUserPrefs"; // Имя файла SharedPreferences
    public static final String PREF_KEY_LOGGED_IN_USER_ID = "logged_in_user_id"; // Ключ для хранения ID
    public static final long DEFAULT_USER_ID = -1L; // Значение по умолчанию (означает "не вошел")

    // --- Константа для Intent Extra ---
    public static final String EXTRA_USER_ID = "USER_ID";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблицы пользователей
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," // Проверьте это имя
                + COLUMN_LOGIN + " TEXT UNIQUE NOT NULL,"
                + COLUMN_PASSWORD + " TEXT NOT NULL,"
                + COLUMN_NAME + " TEXT NOT NULL,"
                + COLUMN_GENDER + " TEXT,"
                + COLUMN_BIRTH_DATE + " TEXT,"
                + COLUMN_CITY + " TEXT,"
                + COLUMN_LANGUAGE + " TEXT DEFAULT 'Русский',"
                + COLUMN_AVATAR + " INTEGER"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Создание таблицы цветов пользователя
        String CREATE_USER_COLORS_TABLE = "CREATE TABLE " + TABLE_USER_COLORS + "("
                + COLUMN_COLOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER,"
                + COLUMN_COLOR_NAME + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_USER_COLORS_TABLE);

        // Создание таблицы стилей пользователя
        String CREATE_USER_STYLES_TABLE = "CREATE TABLE " + TABLE_USER_STYLES + "("
                + COLUMN_STYLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER,"
                + COLUMN_STYLE_NAME + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_USER_STYLES_TABLE);

        // Создание таблицы гардероба пользователя
        String CREATE_WARDROBE_TABLE = "CREATE TABLE " + TABLE_WARDROBE + "("
                + COLUMN_CLOTHING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER,"
                + COLUMN_CLOTHING_NAME + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_WARDROBE_TABLE);

        // Создание таблицы аксессуаров пользователя
        String CREATE_ACCESSORIES_TABLE = "CREATE TABLE " + TABLE_ACCESSORIES + "("
                + COLUMN_ACCESSORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER,"
                + COLUMN_ACCESSORY_NAME + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_ACCESSORIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_COLORS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_STYLES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WARDROBE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCESSORIES);
        onCreate(db);
    }

    // Метод для добавления нового пользователя
    public long addUser(String login, String password, String name, String gender,
                        String birthDate, String city, String language, int avatarResId) {
        if (checkLoginExists(login)) {
            return -1;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        values.put(COLUMN_LOGIN, login);
        values.put(COLUMN_PASSWORD, hashedPassword);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_GENDER, gender);
        values.put(COLUMN_BIRTH_DATE, birthDate);
        values.put(COLUMN_CITY, city);
        values.put(COLUMN_LANGUAGE, language != null ? language : "Русский");
        values.put(COLUMN_AVATAR, avatarResId);

        // ВАЖНО: Используйте insertWithOnConflict или отдельную проверку
        long result = db.insert(TABLE_USERS, null, values);
        return result; // Должно возвращать -1 только при ошибке
    }

    // Метод для добавления цветов пользователя
    public void addUserColors(long userId, List<String> colors) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            for (String color : colors) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_USER_ID, userId); // Важно: правильный столбец
                values.put(COLUMN_COLOR_NAME, color);
                db.insert(TABLE_USER_COLORS, null, values);
            }
        } finally {
        }
    }

    // Метод для добавления стилей пользователя
    public void addUserStyles(long userId, List<String> styles) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            for (String style : styles) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_USER_ID, userId);
                values.put(COLUMN_STYLE_NAME, style);
                db.insert(TABLE_USER_STYLES, null, values);
            }
        }
        finally {
        }
    }

    // Метод для добавления одежды в гардероб
    public void addWardrobeItems(long userId, List<String> clothes) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            for (String item : clothes) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_USER_ID, userId);
                values.put(COLUMN_CLOTHING_NAME, item);
                db.insert(TABLE_WARDROBE, null, values);
            }
        }
        finally {
        }
    }

    // Метод для добавления аксессуаров
    public void addAccessories(long userId, List<String> accessories) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            for (String accessory : accessories) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_USER_ID, userId);
                values.put(COLUMN_ACCESSORY_NAME, accessory);
                db.insert(TABLE_ACCESSORIES, null, values);
            }
        }
        finally {
        }
    }

    // Проверка существования логина
    public boolean checkLoginExists(String login) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_USER_ID}, // Используем константу
                COLUMN_LOGIN + " = ?",
                new String[]{login},
                null, null, null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Проверка логина и пароля
    public boolean checkUserCredentials(String login, String enteredPassword) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String storedHash = null;

        try {
            // 1. Ищем пользователя по ЛОГИНУ и получаем его СОХРАНЕННЫЙ ХЭШ ПАРОЛЯ
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_PASSWORD}, // Получаем только колонку с хэшем пароля
                    COLUMN_LOGIN + "=?",           // Условие WHERE по логину
                    new String[]{login},           // Значение для логина
                    null, null, null);

            // 2. Если пользователь найден, извлекаем хэш
            if (cursor.moveToFirst()) {
                // Индекс 0, так как мы запросили только одну колонку (COLUMN_PASSWORD)
                storedHash = cursor.getString(0);
            }
        } catch (Exception e) {
            // Логирование ошибки
            android.util.Log.e("DB_HELPER", "Error checking user credentials", e);
            return false; // В случае ошибки - доступ запрещен
        } finally {
            if (cursor != null) {
                cursor.close(); // !!! ОБЯЗАТЕЛЬНО ЗАКРЫТЬ КУРСОР !!!
            }

        }


        // 3. Сравниваем введенный пароль с сохраненным хэшем
        if (storedHash != null) {
            // BCrypt.checkpw сама извлекает соль из storedHash и сравнивает
            try {
                return BCrypt.checkpw(enteredPassword, storedHash);
            } catch (IllegalArgumentException e) {
                // Это может случиться, если storedHash имеет неверный формат
                android.util.Log.e("DB_HELPER", "Error checking password - invalid hash format?", e);
                return false;
            }
        } else {
            // Пользователь с таким логином не найден
            return false;
        }
    }

    // Получение ID пользователя по логину
    public long getUserId(String login) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_LOGIN + "=?",
                new String[]{login},
                null, null, null);

        long userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getLong(0);
        }
        cursor.close();
        return userId;
    }

    // Получение списка цветов пользователя
    public List<String> getUserColors(long userId) {
        List<String> colors = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER_COLORS,
                new String[]{COLUMN_COLOR_NAME},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                colors.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return colors;
    }

    // Получение списка стилей пользователя
    public List<String> getUserStyles(long userId) {
        List<String> styles = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER_STYLES,
                new String[]{COLUMN_STYLE_NAME},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                styles.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return styles;
    }

    // Получение гардероба пользователя
    public List<String> getUserWardrobe(long userId) {
        List<String> wardrobe = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WARDROBE,
                new String[]{COLUMN_CLOTHING_NAME},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                wardrobe.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return wardrobe;
    }

    // Получение аксессуаров пользователя
    public List<String> getUserAccessories(long userId) {
        List<String> accessories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ACCESSORIES,
                new String[]{COLUMN_ACCESSORY_NAME},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                accessories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return accessories;
    }

    // В методе getUserInfo() DatabaseHelper исправьте:
    public User getUserInfo(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        User user = null;
        try {
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_NAME, COLUMN_GENDER, COLUMN_BIRTH_DATE, COLUMN_CITY, COLUMN_LANGUAGE, COLUMN_AVATAR, COLUMN_LOGIN}, // Добавлен язык и логин
                    COLUMN_USER_ID + "=?",
                    new String[]{String.valueOf(userId)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                user = new User(); // Убедитесь, что класс User существует
                user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                user.setGender(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)));
                user.setBirthDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTH_DATE)));
                user.setCity(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CITY)));
                user.setLanguage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LANGUAGE))); // Получаем язык
                user.setAvatarResId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AVATAR)));
                user.setLogin(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOGIN))); // Получаем логин
                // Добавьте поле и сеттер/геттер для language и login в класс User
            }
        } catch (Exception e) {
            android.util.Log.e("DB_HELPER", "Error getting user info", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return user;
    }

    public boolean updateUserLanguage(long userId, String language) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LANGUAGE, language);

        int rowsAffected = db.update(TABLE_USERS, values,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});

        return rowsAffected > 0;
    }

    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, user.getName());
        values.put(COLUMN_BIRTH_DATE, user.getBirthDate());
        values.put(COLUMN_LOGIN, user.getLogin());
        values.put(COLUMN_GENDER, user.getGender());
        values.put(COLUMN_CITY, user.getCity());
        values.put(COLUMN_LANGUAGE, user.getLanguage());
        values.put(COLUMN_AVATAR, user.getAvatarResId());

        int rowsAffected = db.update(
                TABLE_USERS,
                values,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(user.getUserId())}
        );

        return rowsAffected > 0;
    }

    // В DatabaseHelper.java
    public boolean deleteUser(long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. Удаляем связанные данные (цвета, стили и т.д.)
            db.delete(TABLE_USER_COLORS, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
            db.delete(TABLE_USER_STYLES, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
            db.delete(TABLE_WARDROBE, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
            db.delete(TABLE_ACCESSORIES, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});

            // 2. Удаляем самого пользователя
            int rowsAffected = db.delete(TABLE_USERS, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});

            db.setTransactionSuccessful();
            return rowsAffected > 0;
        } finally {
            db.endTransaction();
        }
    }
}