package com.example.cloova;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

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
                        String birthDate, String city, int avatarResId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_LOGIN, login);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_GENDER, gender);
        values.put(COLUMN_BIRTH_DATE, birthDate);
        values.put(COLUMN_CITY, city);
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
            db.close();
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
            db.close();
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
            db.close();
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
            db.close();
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
        db.close();
        return exists;
    }

    // Проверка логина и пароля
    public boolean checkUserCredentials(String login, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_LOGIN + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{login, password},
                null, null, null);

        boolean valid = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return valid;
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
        db.close();
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
        db.close();
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
        db.close();
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
        db.close();
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
        db.close();
        return accessories;
    }

    // В методе getUserInfo() DatabaseHelper исправьте:
    public User getUserInfo(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_NAME, COLUMN_GENDER, COLUMN_BIRTH_DATE, COLUMN_CITY, COLUMN_AVATAR},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setName(cursor.getString(0)); // name
            user.setGender(cursor.getString(1)); // gender
            user.setBirthDate(cursor.getString(2)); // birth date
            user.setCity(cursor.getString(3)); // city
            user.setAvatarResId(cursor.getInt(4)); // avatar
        }
        cursor.close();
        db.close();
        return user;
    }
}