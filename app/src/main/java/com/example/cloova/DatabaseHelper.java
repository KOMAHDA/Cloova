package com.example.cloova;

import com.example.cloova.model.ClothingItem;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log; // Убедимся, что импорт есть

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DB_HELPER"; // Тег для логов
    private static final String DATABASE_NAME = "CloovaDB.db";
    private static final int DATABASE_VERSION = 4; // <<<=== ВЕРСИЯ ИЗМЕНЕНА

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
    public static final String COLUMN_LANGUAGE = "language"; // Уже было

    // Таблица выбранных цветов пользователя
    private static final String TABLE_USER_COLORS = "user_colors";
    private static final String COLUMN_COLOR_ID = "color_id"; // PK этой таблицы
    // COLUMN_USER_ID используется как FK
    private static final String COLUMN_COLOR_NAME = "color_name";

    // Таблица выбранных стилей пользователя
    private static final String TABLE_USER_STYLES = "user_styles";
    private static final String COLUMN_STYLE_ID_USER = "user_style_id"; // Переименовал для ясности, PK
    // COLUMN_USER_ID используется как FK
    private static final String COLUMN_STYLE_NAME_USER = "style_name"; // Переименовал для ясности

    // Таблица гардероба пользователя
    private static final String TABLE_WARDROBE = "wardrobe";
    private static final String COLUMN_CLOTHING_ID_USER_TABLE = "clothing_id"; // Переименовал PK
    // COLUMN_USER_ID используется как FK
    private static final String COLUMN_CLOTHING_NAME_USER_TABLE = "clothing_name"; // Переименовал

    // Таблица аксессуаров пользователя
    private static final String TABLE_ACCESSORIES = "accessories";
    private static final String COLUMN_ACCESSORY_ID_USER_TABLE = "accessory_id"; // Переименовал PK
    // COLUMN_USER_ID используется как FK
    private static final String COLUMN_ACCESSORY_NAME_USER_TABLE = "accessory_name"; // Переименовал


    // --- НОВЫЕ КОНСТАНТЫ ДЛЯ ТАБЛИЦ ОДЕЖДЫ ---
    public static final String TABLE_CLOTHING_ITEMS = "clothing_items";
    public static final String COLUMN_CI_ID = "clothing_item_id"; // Изменено для уникальности
    public static final String COLUMN_CI_NAME = "name";
    public static final String COLUMN_CI_CATEGORY = "category";
    public static final String COLUMN_CI_IMAGE_RES = "image_resource_name";
    public static final String COLUMN_CI_GENDER_TARGET = "gender_target";
    public static final String COLUMN_CI_MIN_TEMP = "min_temp";
    public static final String COLUMN_CI_MAX_TEMP = "max_temp";
    public static final String COLUMN_CI_IS_WATERPROOF = "is_waterproof";
    public static final String COLUMN_CI_IS_WINDPROOF = "is_windproof";

    public static final String TABLE_STYLES_CATALOG = "styles_catalog";
    public static final String COLUMN_SC_ID = "style_catalog_id"; // Изменено для уникальности
    public static final String COLUMN_SC_NAME = "name";

    public static final String TABLE_WEATHER_CONDITIONS_CATALOG = "weather_conditions_catalog";
    public static final String COLUMN_WCC_ID = "condition_catalog_id"; // Изменено для уникальности
    public static final String COLUMN_WCC_NAME = "name";

    public static final String TABLE_CLOTHING_ITEM_STYLES = "clothing_item_styles";
    public static final String COLUMN_CIS_ID = "item_style_relation_id"; // Изменено для уникальности
    public static final String COLUMN_CIS_CLOTHING_ID_FK = "clothing_item_id_fk";
    public static final String COLUMN_CIS_STYLE_ID_FK = "style_catalog_id_fk";

    public static final String TABLE_CLOTHING_ITEM_CONDITIONS = "clothing_item_conditions";
    public static final String COLUMN_CIC_ID = "item_condition_relation_id"; // Изменено для уникальности
    public static final String COLUMN_CIC_CLOTHING_ID_FK = "clothing_item_id_fk";
    public static final String COLUMN_CIC_CONDITION_ID_FK = "condition_catalog_id_fk";
    // --- КОНЕЦ НОВЫХ КОНСТАНТ ---


    public static final String SHARED_PREFS_NAME = "CloovaUserPrefs";
    public static final String PREF_KEY_LOGGED_IN_USER_ID = "logged_in_user_id";
    public static final long DEFAULT_USER_ID = -1L;
    public static final String EXTRA_USER_ID = "USER_ID";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: Creating all tables for version " + DATABASE_VERSION);
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
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

        String CREATE_USER_COLORS_TABLE = "CREATE TABLE " + TABLE_USER_COLORS + "("
                + COLUMN_COLOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER,"
                + COLUMN_COLOR_NAME + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_USER_COLORS_TABLE);

        String CREATE_USER_STYLES_TABLE = "CREATE TABLE " + TABLE_USER_STYLES + "("
                + COLUMN_STYLE_ID_USER + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER,"
                + COLUMN_STYLE_NAME_USER + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_USER_STYLES_TABLE);

        String CREATE_WARDROBE_TABLE = "CREATE TABLE " + TABLE_WARDROBE + "("
                + COLUMN_CLOTHING_ID_USER_TABLE + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER,"
                + COLUMN_CLOTHING_NAME_USER_TABLE + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_WARDROBE_TABLE);

        String CREATE_ACCESSORIES_TABLE = "CREATE TABLE " + TABLE_ACCESSORIES + "("
                + COLUMN_ACCESSORY_ID_USER_TABLE + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER,"
                + COLUMN_ACCESSORY_NAME_USER_TABLE + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_ACCESSORIES_TABLE);

        // --- СОЗДАНИЕ НОВЫХ ТАБЛИЦ ДЛЯ ОДЕЖДЫ ---
        String CREATE_CLOTHING_ITEMS_TABLE = "CREATE TABLE " + TABLE_CLOTHING_ITEMS + "("
                + COLUMN_CI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CI_NAME + " TEXT NOT NULL,"
                + COLUMN_CI_CATEGORY + " TEXT NOT NULL,"
                + COLUMN_CI_IMAGE_RES + " TEXT,"
                + COLUMN_CI_GENDER_TARGET + " TEXT NOT NULL,"
                + COLUMN_CI_MIN_TEMP + " INTEGER,"
                + COLUMN_CI_MAX_TEMP + " INTEGER,"
                + COLUMN_CI_IS_WATERPROOF + " INTEGER DEFAULT 0,"
                + COLUMN_CI_IS_WINDPROOF + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_CLOTHING_ITEMS_TABLE);

        String CREATE_STYLES_CATALOG_TABLE = "CREATE TABLE " + TABLE_STYLES_CATALOG + "("
                + COLUMN_SC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SC_NAME + " TEXT NOT NULL UNIQUE"
                + ")";
        db.execSQL(CREATE_STYLES_CATALOG_TABLE);

        String CREATE_WEATHER_CONDITIONS_CATALOG_TABLE = "CREATE TABLE " + TABLE_WEATHER_CONDITIONS_CATALOG + "("
                + COLUMN_WCC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_WCC_NAME + " TEXT NOT NULL UNIQUE"
                + ")";
        db.execSQL(CREATE_WEATHER_CONDITIONS_CATALOG_TABLE);

        String CREATE_CLOTHING_ITEM_STYLES_TABLE = "CREATE TABLE " + TABLE_CLOTHING_ITEM_STYLES + "("
                + COLUMN_CIS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CIS_CLOTHING_ID_FK + " INTEGER NOT NULL,"
                + COLUMN_CIS_STYLE_ID_FK + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + COLUMN_CIS_CLOTHING_ID_FK + ") REFERENCES " + TABLE_CLOTHING_ITEMS + "(" + COLUMN_CI_ID + "),"
                + "FOREIGN KEY(" + COLUMN_CIS_STYLE_ID_FK + ") REFERENCES " + TABLE_STYLES_CATALOG + "(" + COLUMN_SC_ID + ")"
                + ")";
        db.execSQL(CREATE_CLOTHING_ITEM_STYLES_TABLE);

        String CREATE_CLOTHING_ITEM_CONDITIONS_TABLE = "CREATE TABLE " + TABLE_CLOTHING_ITEM_CONDITIONS + "("
                + COLUMN_CIC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CIC_CLOTHING_ID_FK + " INTEGER NOT NULL,"
                + COLUMN_CIC_CONDITION_ID_FK + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + COLUMN_CIC_CLOTHING_ID_FK + ") REFERENCES " + TABLE_CLOTHING_ITEMS + "(" + COLUMN_CI_ID + "),"
                + "FOREIGN KEY(" + COLUMN_CIC_CONDITION_ID_FK + ") REFERENCES " + TABLE_WEATHER_CONDITIONS_CATALOG + "(" + COLUMN_WCC_ID + ")"
                + ")";
        db.execSQL(CREATE_CLOTHING_ITEM_CONDITIONS_TABLE);

        prepopulateData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "onUpgrade: Upgrading database from version " + oldVersion + " to " + newVersion + ". Old data will be lost.");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLOTHING_ITEM_CONDITIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLOTHING_ITEM_STYLES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEATHER_CONDITIONS_CATALOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STYLES_CATALOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLOTHING_ITEMS);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCESSORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WARDROBE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_STYLES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_COLORS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private void prepopulateData(SQLiteDatabase db) {
        Log.d(TAG, "prepopulateData: Prepopulating catalogs...");
        addStyleCatalog(db, "Повседневный");
        addStyleCatalog(db, "Спортивный");
        addStyleCatalog(db, "Деловой");
        addStyleCatalog(db, "Классический");
        addStyleCatalog(db, "Вечерний");

        addWeatherConditionCatalog(db, "Солнечно");
        addWeatherConditionCatalog(db, "Облачно");
        addWeatherConditionCatalog(db, "Переменная облачность");
        addWeatherConditionCatalog(db, "Дождь");
        addWeatherConditionCatalog(db, "Небольшой дождь");
        addWeatherConditionCatalog(db, "Снег");
        addWeatherConditionCatalog(db, "Ветрено");
        addWeatherConditionCatalog(db, "Туман");
        Log.d(TAG, "prepopulateData: Finished prepopulating catalogs.");

        addClothingItemFull(db,"Футболка хлопковая", "Верх", "tshirt_cotton_white", "Унисекс",
                18, 30, 0, 0,
                List.of("Повседневный", "Спортивный"), // Стили
                List.of("Солнечно", "Переменная облачность", "Облачно") // Погодные условия
        );
        addClothingItemFull(db,"Джинсы синие", "Низ", "jeans_blue_classic", "Унисекс",
                5, 25, 0, 0,
                List.of("Повседневный", "Классический"),
                List.of("Солнечно", "Переменная облачность", "Облачно", "Ветрено")
        );
        addClothingItemFull(db,"Легкая куртка ветровка", "Верхняя одежда", "windbreaker_light_blue", "Унисекс",
                10, 20, 0, 1, // isWindproof = 1
                List.of("Повседневный", "Спортивный"),
                List.of("Переменная облачность", "Облачно", "Ветрено", "Небольшой дождь")
        );
    }

    private long addStyleCatalog(SQLiteDatabase db, String styleName) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SC_NAME, styleName);
        return db.insertWithOnConflict(TABLE_STYLES_CATALOG, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private long addWeatherConditionCatalog(SQLiteDatabase db, String conditionName) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_WCC_NAME, conditionName);
        return db.insertWithOnConflict(TABLE_WEATHER_CONDITIONS_CATALOG, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    // --- СУЩЕСТВУЮЩИЕ МЕТОДЫ (С УДАЛЕННЫМИ DB.CLOSE()) ---
    public long addUser(String login, String password, String name, String gender,
                        String birthDate, String city, String language, int avatarResId) {
        if (checkLoginExists(login)) {
            Log.w(TAG, "addUser: Login " + login + " already exists.");
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
        long result = -1;
        try {
            result = db.insertOrThrow(TABLE_USERS, null, values);
            Log.d(TAG, "addUser: User " + login + " added with ID: " + result);
        } catch (Exception e) {
            Log.e(TAG, "addUser: Error inserting user " + login, e);
        }
        return result;
    }

    public void addUserColors(long userId, List<String> colors) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            for (String color : colors) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_USER_ID, userId);
                values.put(COLUMN_COLOR_NAME, color);
                db.insert(TABLE_USER_COLORS, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error in addUserColors", e);
        } finally {
            db.endTransaction();
        }
    }

    public void addUserStyles(long userId, List<String> styles) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            for (String style : styles) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_USER_ID, userId);
                values.put(COLUMN_STYLE_NAME_USER, style); // Использовал переименованную константу
                db.insert(TABLE_USER_STYLES, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error in addUserStyles", e);
        } finally {
            db.endTransaction();
        }
    }

    public void addWardrobeItems(long userId, List<String> clothes) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            for (String item : clothes) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_USER_ID, userId);
                values.put(COLUMN_CLOTHING_NAME_USER_TABLE, item); // Использовал переименованную константу
                db.insert(TABLE_WARDROBE, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error in addWardrobeItems", e);
        } finally {
            db.endTransaction();
        }
    }

    public void addAccessories(long userId, List<String> accessories) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            for (String accessory : accessories) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_USER_ID, userId);
                values.put(COLUMN_ACCESSORY_NAME_USER_TABLE, accessory); // Использовал переименованную константу
                db.insert(TABLE_ACCESSORIES, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error in addAccessories", e);
        } finally {
            db.endTransaction();
        }
    }

    public boolean checkLoginExists(String login) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.query(
                    TABLE_USERS, new String[]{COLUMN_USER_ID}, COLUMN_LOGIN + " = ?",
                    new String[]{login}, null, null, null);
            exists = cursor.getCount() > 0;
        } catch (Exception e){
            Log.e(TAG, "Error in checkLoginExists", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return exists;
    }

    public boolean checkUserCredentials(String login, String enteredPassword) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String storedHash = null;
        boolean credentialsValid = false;
        try {
            cursor = db.query(TABLE_USERS, new String[]{COLUMN_PASSWORD},
                    COLUMN_LOGIN + "=?", new String[]{login}, null, null, null);
            if (cursor.moveToFirst()) {
                storedHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
            }
        } catch (Exception e){
            Log.e(TAG, "Error fetching stored hash for login: " + login, e);
        } finally {
            if (cursor != null) cursor.close();
        }
        if (storedHash != null) {
            try {
                credentialsValid = BCrypt.checkpw(enteredPassword, storedHash);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error checking password - invalid hash format for login: " + login, e);
            }
        }
        return credentialsValid;
    }

    public long getUserId(String login) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        long userId = -1;
        try {
            cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                    COLUMN_LOGIN + "=?", new String[]{login}, null, null, null);
            if (cursor.moveToFirst()) {
                userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getUserId for login: " + login, e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return userId;
    }

    public List<String> getUserColors(long userId) {
        List<String> colors = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USER_COLORS, new String[]{COLUMN_COLOR_NAME},
                    COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    colors.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR_NAME)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getUserColors for userId: " + userId, e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return colors;
    }

    public List<String> getUserStyles(long userId) {
        List<String> styles = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USER_STYLES, new String[]{COLUMN_STYLE_NAME_USER},
                    COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    styles.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STYLE_NAME_USER)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getUserStyles for userId: " + userId, e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return styles;
    }

    public List<String> getUserWardrobe(long userId) {
        List<String> wardrobe = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_WARDROBE, new String[]{COLUMN_CLOTHING_NAME_USER_TABLE},
                    COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    wardrobe.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLOTHING_NAME_USER_TABLE)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getUserWardrobe for userId: " + userId, e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return wardrobe;
    }

    public List<String> getUserAccessories(long userId) {
        List<String> accessories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_ACCESSORIES, new String[]{COLUMN_ACCESSORY_NAME_USER_TABLE},
                    COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    accessories.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACCESSORY_NAME_USER_TABLE)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getUserAccessories for userId: " + userId, e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return accessories;
    }

    public User getUserInfo(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        User user = null;
        try {
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_LOGIN, COLUMN_NAME, COLUMN_GENDER, COLUMN_BIRTH_DATE, COLUMN_CITY, COLUMN_LANGUAGE, COLUMN_AVATAR},
                    COLUMN_USER_ID + "=?",
                    new String[]{String.valueOf(userId)},
                    null, null, null);
            if (cursor.moveToFirst()) {
                user = new User();
                user.setUserId(userId);
                user.setLogin(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOGIN)));
                user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                user.setGender(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)));
                user.setBirthDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTH_DATE)));
                user.setCity(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CITY)));
                user.setLanguage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LANGUAGE)));
                user.setAvatarResId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AVATAR)));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user info for ID: " + userId, e);
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

    // --- НОВЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С ОДЕЖДОЙ ---
    public long addClothingItemFull(SQLiteDatabase db, String name, String category, String imageRes, String genderTarget,
                                    int minTemp, int maxTemp, int isWaterproof, int isWindproof,
                                    List<String> styleNames, List<String> conditionNames) {
        long clothingItemId = -1;
        ContentValues itemValues = new ContentValues();
        itemValues.put(COLUMN_CI_NAME, name);
        itemValues.put(COLUMN_CI_CATEGORY, category);
        itemValues.put(COLUMN_CI_IMAGE_RES, imageRes);
        itemValues.put(COLUMN_CI_GENDER_TARGET, genderTarget);
        itemValues.put(COLUMN_CI_MIN_TEMP, minTemp);
        itemValues.put(COLUMN_CI_MAX_TEMP, maxTemp);
        itemValues.put(COLUMN_CI_IS_WATERPROOF, isWaterproof);
        itemValues.put(COLUMN_CI_IS_WINDPROOF, isWindproof);
        try {
            db.beginTransaction();
            clothingItemId = db.insertOrThrow(TABLE_CLOTHING_ITEMS, null, itemValues);
            if (clothingItemId != -1) {
                if (styleNames != null) {
                    for (String styleName : styleNames) {
                        long styleId = getStyleIdByName(db, styleName);
                        if (styleId != -1) linkClothingToStyle(db, clothingItemId, styleId);
                        else Log.w(TAG, "Style not found in catalog for linking: " + styleName);
                    }
                }
                if (conditionNames != null) {
                    for (String conditionName : conditionNames) {
                        long conditionId = getWeatherConditionIdByName(db, conditionName);
                        if (conditionId != -1) linkClothingToWeatherCondition(db, clothingItemId, conditionId);
                        else Log.w(TAG, "Weather condition not found for linking: " + conditionName);
                    }
                }
                db.setTransactionSuccessful();
                Log.d(TAG, "Added clothing item '" + name + "' with ID: " + clothingItemId + " and linked its properties.");
            } else {
                Log.e(TAG, "Failed to add clothing item: " + name);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in addClothingItemFull for item: " + name, e);
            clothingItemId = -1;
        } finally {
            db.endTransaction();
        }
        return clothingItemId;
    }

    private void linkClothingToStyle(SQLiteDatabase db, long clothingId, long styleId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_CIS_CLOTHING_ID_FK, clothingId);
        values.put(COLUMN_CIS_STYLE_ID_FK, styleId);
        db.insert(TABLE_CLOTHING_ITEM_STYLES, null, values);
    }

    private void linkClothingToWeatherCondition(SQLiteDatabase db, long clothingId, long conditionId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_CIC_CLOTHING_ID_FK, clothingId);
        values.put(COLUMN_CIC_CONDITION_ID_FK, conditionId);
        db.insert(TABLE_CLOTHING_ITEM_CONDITIONS, null, values);
    }

    private long getStyleIdByName(SQLiteDatabase db, String styleName) {
        Cursor cursor = null;
        long styleId = -1;
        try {
            cursor = db.query(TABLE_STYLES_CATALOG, new String[]{COLUMN_SC_ID},
                    COLUMN_SC_NAME + "=?", new String[]{styleName}, null, null, null);
            if (cursor.moveToFirst()) {
                styleId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SC_ID));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getStyleIdByName for style: " + styleName, e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return styleId;
    }

    private long getWeatherConditionIdByName(SQLiteDatabase db, String conditionName) {
        Cursor cursor = null;
        long conditionId = -1;
        try {
            cursor = db.query(TABLE_WEATHER_CONDITIONS_CATALOG, new String[]{COLUMN_WCC_ID},
                    COLUMN_WCC_NAME + "=?", new String[]{conditionName}, null, null, null);
            if (cursor.moveToFirst()) {
                conditionId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_WCC_ID));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getWeatherConditionIdByName for condition: " + conditionName, e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return conditionId;
    }

    public List<ClothingItem> getSuggestedOutfit(int currentTemp, List<String> weatherConditionNames,
                                                 String userStyleName, String userGender) {
        List<ClothingItem> suggestedItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        long targetStyleId = getStyleIdByName(db, userStyleName); // Вызываем без передачи db
        List<Long> targetConditionIds = new ArrayList<>();
        if (weatherConditionNames != null) {
            for (String condName : weatherConditionNames) {
                long condId = getWeatherConditionIdByName(db, condName); // Вызываем без передачи db
                if (condId != -1) {
                    targetConditionIds.add(condId);
                }
            }
        }

        String query = "SELECT DISTINCT ci." + COLUMN_CI_ID + ", ci." + COLUMN_CI_NAME + ", ci." + COLUMN_CI_CATEGORY +
                ", ci." + COLUMN_CI_IMAGE_RES + ", ci." + COLUMN_CI_GENDER_TARGET + ", ci." + COLUMN_CI_MIN_TEMP +
                ", ci." + COLUMN_CI_MAX_TEMP + ", ci." + COLUMN_CI_IS_WATERPROOF + ", ci." + COLUMN_CI_IS_WINDPROOF +
                " FROM " + TABLE_CLOTHING_ITEMS + " ci" +
                " LEFT JOIN " + TABLE_CLOTHING_ITEM_STYLES + " cis ON ci." + COLUMN_CI_ID + " = cis." + COLUMN_CIS_CLOTHING_ID_FK +
                " LEFT JOIN " + TABLE_CLOTHING_ITEM_CONDITIONS + " cic ON ci." + COLUMN_CI_ID + " = cic." + COLUMN_CIC_CLOTHING_ID_FK +
                " WHERE (ci." + COLUMN_CI_GENDER_TARGET + " = ? OR ci." + COLUMN_CI_GENDER_TARGET + " = 'Унисекс')" +
                " AND ci." + COLUMN_CI_MIN_TEMP + " <= ?" +
                " AND ci." + COLUMN_CI_MAX_TEMP + " >= ?";

        List<String> selectionArgsList = new ArrayList<>();
        selectionArgsList.add(userGender);
        selectionArgsList.add(String.valueOf(currentTemp));
        selectionArgsList.add(String.valueOf(currentTemp));

        if (targetStyleId != -1) {
            query += " AND cis." + COLUMN_CIS_STYLE_ID_FK + " = ?";
            selectionArgsList.add(String.valueOf(targetStyleId));
        } else {
            // Если стиль пользователя не найден/не указан, можно не фильтровать по стилю
            // или добавить `AND 1=0` чтобы ничего не найти, если стиль обязателен.
            // Пока оставляем так, чтобы находились вещи любого стиля, если стиль пользователя не определен.
            Log.w(TAG, "User style not found or not specified, not filtering by style.");
        }

        if (!targetConditionIds.isEmpty()) {
            query += " AND cic." + COLUMN_CIC_CONDITION_ID_FK + " IN (" + makePlaceholders(targetConditionIds.size()) + ")";
            for (Long id : targetConditionIds) {
                selectionArgsList.add(String.valueOf(id));
            }
        } else {
            // Если погодные условия не указаны, можно также не фильтровать по ним
            // или, наоборот, считать, что вещь должна подходить "для любой погоды" (сложнее)
            Log.w(TAG, "Weather conditions not specified, not filtering by conditions explicitly.");
        }
        // Для логики выбора по одной вещи из категории, можно добавить ORDER BY ci.category и потом в Java обрабатывать
        query += " ORDER BY ci." + COLUMN_CI_CATEGORY;


        try {
            Log.d(TAG, "Executing outfit query: " + query);
            Log.d(TAG, "With args: " + selectionArgsList.toString());
            cursor = db.rawQuery(query, selectionArgsList.toArray(new String[0]));
            if (cursor.moveToFirst()) {
                do {
                    ClothingItem item = new ClothingItem();
                    item.setClothingId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CI_ID)));
                    item.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CI_NAME)));
                    item.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CI_CATEGORY)));
                    item.setImageResourceName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CI_IMAGE_RES)));
                    item.setGenderTarget(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CI_GENDER_TARGET)));
                    item.setMinTemp(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CI_MIN_TEMP)));
                    item.setMaxTemp(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CI_MAX_TEMP)));
                    item.setWaterproof(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CI_IS_WATERPROOF)) == 1);
                    item.setWindproof(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CI_IS_WINDPROOF)) == 1);
                    suggestedItems.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting suggested outfit", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d(TAG, "Suggested outfit items found (before category filtering): " + suggestedItems.size());
        return suggestedItems; // Пока возвращаем все подходящие, фильтрация по категориям будет позже
    }

    private String makePlaceholders(int len) {
        if (len < 1) {
            return "-1";
        }
        StringBuilder sb = new StringBuilder(len * 2 - 1);
        sb.append("?");
        for (int i = 1; i < len; i++) {
            sb.append(",?");
        }
        return sb.toString();
    }
}