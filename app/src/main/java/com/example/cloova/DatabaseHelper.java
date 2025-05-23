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
        addStyleCatalog(db, "Классический");
        addStyleCatalog(db, "Богемный");

        addWeatherConditionCatalog(db, "Солнечно");
        addWeatherConditionCatalog(db, "Облачно");
        addWeatherConditionCatalog(db, "Переменная облачность");
        addWeatherConditionCatalog(db, "Дождь");
        addWeatherConditionCatalog(db, "Небольшой дождь");
        addWeatherConditionCatalog(db, "Снег");
        addWeatherConditionCatalog(db, "Ветрено");
        addWeatherConditionCatalog(db, "Туман");
        Log.d(TAG, "prepopulateData: Finished prepopulating catalogs.");

        // В DatabaseHelper.java, внутри prepopulateData(SQLiteDatabase db)

        // Каталоги стилей и погодных условий (оставляем ваши)
        addStyleCatalog(db, "Повседневный");
        addStyleCatalog(db, "Спортивный");
        addStyleCatalog(db, "Классический");
        addStyleCatalog(db, "Богемный");

        addWeatherConditionCatalog(db, "Солнечно");
        addWeatherConditionCatalog(db, "Облачно");
        addWeatherConditionCatalog(db, "Переменная облачность");
        addWeatherConditionCatalog(db, "Дождь");
        addWeatherConditionCatalog(db, "Небольшой дождь");
        addWeatherConditionCatalog(db, "Снег");
        addWeatherConditionCatalog(db, "Ветрено");
        addWeatherConditionCatalog(db, "Туман");
        Log.d(TAG, "prepopulateData: Finished prepopulating catalogs.");

        // --- Одежда ---

        // --- Верх ---
        addClothingItemFull(db, "Футболка хлопковая", "Верх", "tshirt_cotton_default", "Унисекс",
                18, 30, 0, 0,
                List.of("Повседневный", "Спортивный"), // Покрывает 2 стиля
                List.of("Солнечно", "Переменная облачность", "Облачно", "Туман") // Широкий спектр теплой погоды
        );
        addClothingItemFull(db, "Кофта", "Верх", "sweatshirt_fleece_default", "Унисекс",
                8, 20, 0, 1, // Ветрозащитная
                List.of("Повседневный", "Спортивный"), // Покрывает 2 стиля
                List.of("Облачно", "Переменная облачность", "Ветрено", "Туман") // Прохладная, ветреная
        );
        addClothingItemFull(db, "Рубашка классическая", "Верх", "shirt_classic_default", "Мужской", // Только Мужской
                15, 25, 0, 0,
                List.of("Классический", "Богемный", "Повседневный"), // Покрывает 3 стиля
                List.of("Солнечно", "Облачно", "Переменная облачность", "Туман")
        );
        addClothingItemFull(db, "Пиджак", "Верх", "shirt_classic_default", "Унисекс", // Только Мужской
                5, 15, 0, 0,
                List.of("Классический", "Богемный", "Повседневный"), // Покрывает 3 стиля
                List.of("Солнечно", "Облачно", "Переменная облачность", "Туман", "Небольшой дождь")
        );
        addClothingItemFull(db, "Блузка элегантная", "Верх", "shirt_classic_default", "Женский",
                15, 25, 0, 0,
                List.of("Классический", "Богемный", "Повседневный"),
                List.of("Солнечно", "Облачно", "Переменная облачность", "Туман")
        );
        addClothingItemFull(db, "Рубашка фланелевая", "Верх", "shirt_flannel_default", "Унисекс",
                8, 18, 0, 0, // Уменьшил верхнюю границу температуры, чтобы не сильно пересекалась с флиской
                List.of("Повседневный"), // Только повседневный
                List.of("Облачно", "Переменная облачность", "Ветрено", "Туман") // Убрал "Прохладно"
        );
        addClothingItemFull(db, "Свитер теплый", "Верх", "sweater_warm_default", "Унисекс",
                -5, 10, 0, 0,
                List.of("Повседневный", "Классический", "Богемный"), // Спортивный может быть флиска + верхняя
                List.of("Облачно", "Переменная облачность", "Ветрено", "Снег", "Туман")
        );


        // --- Низ ---
        addClothingItemFull(db, "Шорты спортивные", "Низ", "shorts_sport_default", "Унисекс",
                22, 35, 0, 0,
                List.of("Спортивный", "Повседневный"),
                List.of("Солнечно", "Переменная облачность")
        );
        addClothingItemFull(db, "Шорты джинсовые", "Низ", "shorts_denim_default", "Унисекс",
                20, 30, 0, 0,
                List.of("Повседневный"),
                List.of("Солнечно", "Переменная облачность", "Облачно")
        );
        addClothingItemFull(db, "Брюки летние", "Низ", "pants_summer_classic", "Унисекс",
                18, 28, 0, 0,
                List.of("Классический", "Повседневный"),
                List.of("Солнечно", "Переменная облачность", "Облачно", "Небольшой дождь")
        );
        addClothingItemFull(db, "Штаны спортивные", "Низ", "pants_sport_default", "Унисекс",
                5, 20, 0, 1,
                List.of("Спортивный", "Повседневный"),
                List.of("Облачно", "Переменная облачность", "Ветрено", "Небольшой дождь") // Убрал "Прохладно"
        );
        addClothingItemFull(db, "Брюки классические", "Низ", "pants_classic_default", "Мужской", // Только Мужской
                10, 22, 0, 0,
                List.of("Классический", "Богемный"),
                List.of("Солнечно", "Облачно", "Переменная облачность", "Ветрено", "Небольшой дождь") // Добавил Ветрено
        );
        addClothingItemFull(db, "Джинсы", "Низ", "jeans_default", "Унисекс", // Переименовал из "Джинсы" для ясности, скорректировал температуру
                0, 20, 0, 1,
                List.of("Повседневный", "Классический", "Богемный"), // Джинсы универсальны
                List.of("Облачно", "Переменная облачность", "Ветрено", "Небольшой дождь", "Дождь", "Туман")
        );
        addClothingItemFull(db, "Джинсы утепленные", "Низ", "jeans_insulated_default", "Унисекс",
                -15, 5, 0, 1, // Скорректировал температуру
                List.of("Повседневный"),
                List.of("Облачно", "Снег", "Ветрено") // Убрал дождь для утепленных, если они не водоотталкивающие
        );
        // !!! НУЖЕН "Низ" для "Спортивный", "Классический", "Богемный" для < 0°C
        addClothingItemFull(db, "Штаны утепленные Спортивные", "Низ", "pants_winter_sport", "Унисекс",
                -20, 0, 1, 1,
                List.of("Спортивный"),
                List.of("Снег", "Ветрено")
        );
        addClothingItemFull(db, "Брюки утеплённые", "Низ", "pants_wool_classic", "Унисекс",
                -20, 0, 0, 0,
                List.of("Классический", "Богемный"),
                List.of("Облачно", "Снег", "Ветрено")
        );

        addClothingItemFull(db, "Платье летнее легкое", "Платья/Юбки", "dress_summer_default", "Женский",
                20, 35, 0, 0,
                List.of("Повседневный", "Богемный"), // Заменил "Вечерний" на "Богемный" для примера
                List.of("Солнечно", "Переменная облачность")
        );
        addClothingItemFull(db, "Юбка джинсовая", "Платья/Юбки", "skirt_denim_default", "Женский",
                18, 30, 0, 0,
                List.of("Повседневный"),
                List.of("Солнечно", "Переменная облачность", "Облачно")
        );
        addClothingItemFull(db, "Платье трикотажное", "Платья/Юбки", "dress_knit_default", "Женский",
                10, 20, 0, 0,
                List.of("Повседневный", "Классический", "Богемный"), // Расширил стили
                List.of("Облачно", "Переменная облачность", "Ветрено", "Туман") // Убрал "Прохладно"
        );


        // --- Верхняя одежда ---
        addClothingItemFull(db, "Куртка джинсовая", "Верхняя одежда", "jacket_denim_default", "Унисекс",
                12, 22, 0, 0, // Немного поднял min_temp
                List.of("Повседневный", "Богемный"), // Добавил Богемный
                List.of("Солнечно", "Переменная облачность", "Облачно", "Ветрено", "Туман", "Дождь")
        );
        addClothingItemFull(db, "Куртка кожаная", "Верхняя одежда", "jacket_leather_default", "Унисекс",
                8, 18, 0, 1,
                List.of("Повседневный", "Классический", "Богемный"), // Добавил Богемный
                List.of("Облачно", "Переменная облачность", "Ветрено", "Туман", "Небольшой дождь", "Дождь") // Убрал "Прохладно"
        );
        addClothingItemFull(db, "Пуховик легкий", "Верхняя одежда", "puffer_jacket_light_default", "Унисекс",
                -10, 10, 1, 1, // Скорректировал max_temp
                List.of("Повседневный", "Спортивный"),
                List.of("Облачно", "Переменная облачность", "Ветрено", "Небольшой дождь", "Дождь", "Снег") // Добавил Снег
        );
        addClothingItemFull(db, "Зимняя куртка (парка)", "Верхняя одежда", "parka_winter_default", "Унисекс",
                -25, 0, 1, 1, // Скорректировал max_temp
                List.of("Повседневный", "Спортивный"), // Парка может быть и спортивной
                List.of("Облачно", "Снег", "Ветрено") // Убрал "Холодно", "Мороз"
        );
        // !!! НУЖНА Верхняя одежда для "Классический", "Богемный" на холод и очень холод.
        addClothingItemFull(db, "Пальто классическое", "Верхняя одежда", "coat_classic_default", "Унисекс",
                -10, 8, 0, 1, // Шерстяное пальто может быть ветрозащитным
                List.of("Классический", "Богемный"),
                List.of("Облачно", "Переменная облачность", "Ветрено", "Снег", "Туман")
        );
        addClothingItemFull(db, "Пальто утеплённое", "Верхняя одежда", "coat_classic_default", "Унисекс",
                -20, -5, 0, 1, // Шерстяное пальто может быть ветрозащитным
                List.of("Классический", "Богемный", "Повседневный"),
                List.of("Облачно", "Переменная облачность", "Ветрено", "Снег", "Туман")
        );


        addClothingItemFull(db, "Кроссовки летние", "Обувь", "sneakers_summer_default", "Унисекс",
                15, 30, 0, 0,
                List.of("Повседневный", "Спортивный", "Богемный"), // Добавил Богемный
                List.of("Солнечно", "Переменная облачность", "Облачно") // Убрал дождь для летних кроссовок
        );
        addClothingItemFull(db, "Кроссовки демисезонные", "Обувь", "sneakers_demi_default", "Унисекс",
                5, 18, 1, 0, // Сделал водонепроницаемыми
                List.of("Повседневный", "Спортивный"),
                List.of("Облачно", "Переменная облачность", "Небольшой дождь", "Дождь", "Ветрено", "Туман") // Убрал "Прохладно"
        );
        addClothingItemFull(db, "Ботинки зимние утепленные", "Обувь", "boots_winter_insulated_default", "Унисекс",
                -25, 5, 1, 1,
                List.of("Повседневный", "Спортивный", "Классический", "Богемный"), // Могут быть и для активного отдыха
                List.of("Снег", "Ветрено") // Убрал "Холодно", "Мороз"
        );
        addClothingItemFull(db, "Туфли классические", "Обувь", "shoes_classic_default", "Унисекс",
                10, 25, 0, 0,
                List.of("Классический", "Богемный"),
                List.of("Солнечно", "Облачно", "Переменная облачность", "Туман") // Не для дождя
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
        SQLiteDatabase db = this.getReadableDatabase(); // Получаем один раз
        Cursor cursor = null;

        long targetStyleId = getStyleIdByName(db, userStyleName);
        List<Long> targetConditionIds = new ArrayList<>();
        if (weatherConditionNames != null) {
            for (String condName : weatherConditionNames) {
                long condId = getWeatherConditionIdByName(db, condName);
                if (condId != -1) {
                    targetConditionIds.add(condId);
                }
            }
        }

        // Строим базовую часть запроса
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT DISTINCT ci.* FROM ").append(TABLE_CLOTHING_ITEMS).append(" ci");

        List<String> selectionArgsList = new ArrayList<>();

        // Присоединяем таблицу стилей, если стиль указан и найден
        if (targetStyleId != -1) {
            queryBuilder.append(" JOIN ").append(TABLE_CLOTHING_ITEM_STYLES).append(" cis ON ci.")
                    .append(COLUMN_CI_ID).append(" = cis.").append(COLUMN_CIS_CLOTHING_ID_FK);
        }
        // Присоединяем таблицу погодных условий, если они указаны
        if (!targetConditionIds.isEmpty()) {
            queryBuilder.append(" JOIN ").append(TABLE_CLOTHING_ITEM_CONDITIONS).append(" cic ON ci.")
                    .append(COLUMN_CI_ID).append(" = cic.").append(COLUMN_CIC_CLOTHING_ID_FK);
        }

        queryBuilder.append(" WHERE (ci.").append(COLUMN_CI_GENDER_TARGET).append(" = ? OR ci.")
                .append(COLUMN_CI_GENDER_TARGET).append(" = 'Унисекс')");
        selectionArgsList.add(userGender);

        queryBuilder.append(" AND ci.").append(COLUMN_CI_MIN_TEMP).append(" <= ?");
        selectionArgsList.add(String.valueOf(currentTemp));

        queryBuilder.append(" AND ci.").append(COLUMN_CI_MAX_TEMP).append(" >= ?");
        selectionArgsList.add(String.valueOf(currentTemp));

        if (targetStyleId != -1) {
            queryBuilder.append(" AND cis.").append(COLUMN_CIS_STYLE_ID_FK).append(" = ?");
            selectionArgsList.add(String.valueOf(targetStyleId));
        }

        if (!targetConditionIds.isEmpty()) {
            queryBuilder.append(" AND cic.").append(COLUMN_CIC_CONDITION_ID_FK).append(" IN (")
                    .append(makePlaceholders(targetConditionIds.size())).append(")");
            for (Long id : targetConditionIds) {
                selectionArgsList.add(String.valueOf(id));
            }
        }

        String finalQuery = queryBuilder.toString();

        try {
            Log.d(TAG, "Executing outfit query: " + finalQuery);
            Log.d(TAG, "With args: " + selectionArgsList.toString());
            cursor = db.rawQuery(finalQuery, selectionArgsList.toArray(new String[0]));
            if (cursor.moveToFirst()) {
                do {
                    ClothingItem item = new ClothingItem();
                    item.setClothingId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CI_ID)));
                    item.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CI_NAME))); // Используем константу из CI
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
            // НЕ ЗАКРЫВАЕМ db
        }
        Log.d(TAG, "Suggested outfit items found (from DB): " + suggestedItems.size());
        return suggestedItems;
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