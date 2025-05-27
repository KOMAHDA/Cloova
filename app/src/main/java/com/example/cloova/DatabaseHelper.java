package com.example.cloova;

import com.example.cloova.model.ClothingItem;
import com.example.cloova.model.SavedOutfit;
import com.example.cloova.model.PlannedOutfit;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import org.mindrot.jbcrypt.BCrypt;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DB_HELPER"; // Тег для логов
    private static final String DATABASE_NAME = "CloovaDB.db";
    private static final int DATABASE_VERSION = 9; // ВЕРСИЯ

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
    private static final String COLUMN_STYLE_ID_USER = "user_style_id";
    // COLUMN_USER_ID используется как FK
    private static final String COLUMN_STYLE_NAME_USER = "style_name";

    // Таблица гардероба пользователя
    private static final String TABLE_WARDROBE = "wardrobe";
    private static final String COLUMN_CLOTHING_ID_USER_TABLE = "clothing_id";
    // COLUMN_USER_ID используется как FK
    private static final String COLUMN_CLOTHING_NAME_USER_TABLE = "clothing_name";

    // Таблица аксессуаров пользователя
    private static final String TABLE_ACCESSORIES = "accessories";
    private static final String COLUMN_ACCESSORY_ID_USER_TABLE = "accessory_id";
    // COLUMN_USER_ID используется как FK
    private static final String COLUMN_ACCESSORY_NAME_USER_TABLE = "accessory_name";


    // --- НОВЫЕ КОНСТАНТЫ ДЛЯ ТАБЛИЦ ОДЕЖДЫ ---
    public static final String TABLE_CLOTHING_ITEMS = "clothing_items";
    public static final String COLUMN_CI_ID = "clothing_item_id";
    public static final String COLUMN_CI_NAME = "name";
    public static final String COLUMN_CI_CATEGORY = "category";
    public static final String COLUMN_CI_IMAGE_RES = "image_resource_name";
    public static final String COLUMN_CI_GENDER_TARGET = "gender_target";
    public static final String COLUMN_CI_MIN_TEMP = "min_temp";
    public static final String COLUMN_CI_MAX_TEMP = "max_temp";
    public static final String COLUMN_CI_IS_WATERPROOF = "is_waterproof";
    public static final String COLUMN_CI_IS_WINDPROOF = "is_windproof";

    public static final String TABLE_STYLES_CATALOG = "styles_catalog";
    public static final String COLUMN_SC_ID = "style_catalog_id";
    public static final String COLUMN_SC_NAME = "name";

    public static final String TABLE_WEATHER_CONDITIONS_CATALOG = "weather_conditions_catalog";
    public static final String COLUMN_WCC_ID = "condition_catalog_id";
    public static final String COLUMN_WCC_NAME = "name";

    public static final String TABLE_CLOTHING_ITEM_STYLES = "clothing_item_styles";
    public static final String COLUMN_CIS_ID = "item_style_relation_id";
    public static final String COLUMN_CIS_CLOTHING_ID_FK = "clothing_item_id_fk";
    public static final String COLUMN_CIS_STYLE_ID_FK = "style_catalog_id_fk";

    public static final String TABLE_CLOTHING_ITEM_CONDITIONS = "clothing_item_conditions";
    public static final String COLUMN_CIC_ID = "item_condition_relation_id";
    public static final String COLUMN_CIC_CLOTHING_ID_FK = "clothing_item_id_fk";
    public static final String COLUMN_CIC_CONDITION_ID_FK = "condition_catalog_id_fk";

    public static final String TABLE_SAVED_OUTFITS = "saved_outfits";
    public static final String COLUMN_SO_ID = "outfit_id"; // Primary Key
    public static final String COLUMN_SO_USER_ID = "user_id"; // Foreign Key к users
    public static final String COLUMN_SO_DATE_SAVED = "date_saved"; // Дата сохранения
    public static final String COLUMN_SO_WEATHER_DESC = "weather_description"; // Описание погоды (напр. "Солнечно")
    public static final String COLUMN_SO_TEMPERATURE = "temperature"; // Температура, для которой сохранен образ
    public static final String COLUMN_SO_STYLE = "style_name"; // Стиль, для которого сохранен образ

    // Таблица элементов сохраненного образа (связь Many-to-Many)
    public static final String TABLE_SAVED_OUTFIT_ITEMS = "saved_outfit_items";
    public static final String COLUMN_SOI_ID = "saved_item_id"; // Primary Key
    public static final String COLUMN_SOI_OUTFIT_ID = "outfit_id_fk"; // Foreign Key к saved_outfits
    public static final String COLUMN_SOI_CLOTHING_ID = "clothing_item_id_fk"; // Foreign Key к clothing_items
    public static final String COLUMN_SOI_CATEGORY = "clothing_category";


    // Таблица запланированных образов
    public static final String TABLE_PLANNED_OUTFITS = "planned_outfits";
    public static final String COLUMN_PO_ID = "planned_outfit_id"; // Primary Key
    public static final String COLUMN_PO_USER_ID = "user_id"; // Foreign Key к users
    public static final String COLUMN_PO_PLAN_DATE = "plan_date"; // Дата, на которую запланирован образ (YYYY-MM-DD)
    public static final String COLUMN_PO_DATE_CREATED = "date_created"; // Дата создания записи (YYYY-MM-DD HH:MM:SS)
    public static final String COLUMN_PO_WEATHER_DESC = "weather_description"; // Описание погоды (напр. "Солнечно")
    public static final String COLUMN_PO_TEMPERATURE = "temperature"; // Температура, для которой запланирован образ
    public static final String COLUMN_PO_STYLE = "style_name"; // Стиль, для которого запланирован образ

    // Таблица элементов запланированного образа (связь Many-to-Many)
    public static final String TABLE_PLANNED_OUTFIT_ITEMS = "planned_outfit_items";
    public static final String COLUMN_POI_ID = "planned_item_id"; // Primary Key
    public static final String COLUMN_POI_OUTFIT_ID = "planned_outfit_id_fk"; // Foreign Key к planned_outfits
    public static final String COLUMN_POI_CLOTHING_ID = "clothing_item_id_fk"; // Foreign Key к clothing_items
    public static final String COLUMN_POI_CATEGORY = "clothing_category"; // Категория одежды (Верх, Низ и т.д.)


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

        // СОЗДАНИЕ НОВЫХ ТАБЛИЦ ДЛЯ ОДЕЖДЫ
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

        String CREATE_SAVED_OUTFITS_TABLE = "CREATE TABLE " + TABLE_SAVED_OUTFITS + "("
                + COLUMN_SO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SO_USER_ID + " INTEGER NOT NULL,"
                + COLUMN_SO_DATE_SAVED + " TEXT NOT NULL," // Формат "YYYY-MM-DD HH:MM:SS"
                + COLUMN_SO_WEATHER_DESC + " TEXT,"
                + COLUMN_SO_TEMPERATURE + " REAL," // Можно хранить как double
                + COLUMN_SO_STYLE + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_SO_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_SAVED_OUTFITS_TABLE);
        Log.d(TAG, "onCreate: TABLE_SAVED_OUTFITS created.");

        String CREATE_SAVED_OUTFIT_ITEMS_TABLE = "CREATE TABLE " + TABLE_SAVED_OUTFIT_ITEMS + "("
                + COLUMN_SOI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SOI_OUTFIT_ID + " INTEGER NOT NULL,"
                + COLUMN_SOI_CLOTHING_ID + " INTEGER NOT NULL,"
                + COLUMN_SOI_CATEGORY + " TEXT NOT NULL," // Важно для сборки полного образа
                + "FOREIGN KEY(" + COLUMN_SOI_OUTFIT_ID + ") REFERENCES " + TABLE_SAVED_OUTFITS + "(" + COLUMN_SO_ID + ") ON DELETE CASCADE," // При удалении образа, удалять и его элементы
                + "FOREIGN KEY(" + COLUMN_SOI_CLOTHING_ID + ") REFERENCES " + TABLE_CLOTHING_ITEMS + "(" + COLUMN_CI_ID + ")"
                + ")";
        db.execSQL(CREATE_SAVED_OUTFIT_ITEMS_TABLE);
        Log.d(TAG, "onCreate: TABLE_SAVED_OUTFIT_ITEMS created.");

        String CREATE_PLANNED_OUTFITS_TABLE = "CREATE TABLE " + TABLE_PLANNED_OUTFITS + "("
                + COLUMN_PO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PO_USER_ID + " INTEGER NOT NULL,"
                + COLUMN_PO_PLAN_DATE + " TEXT NOT NULL," // Формат "YYYY-MM-DD"
                + COLUMN_PO_DATE_CREATED + " TEXT NOT NULL," // Формат "YYYY-MM-DD HH:MM:SS"
                + COLUMN_PO_WEATHER_DESC + " TEXT,"
                + COLUMN_PO_TEMPERATURE + " REAL,"
                + COLUMN_PO_STYLE + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_PO_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_PLANNED_OUTFITS_TABLE);
        Log.d(TAG, "onCreate: TABLE_PLANNED_OUTFITS created.");

        String CREATE_PLANNED_OUTFIT_ITEMS_TABLE = "CREATE TABLE " + TABLE_PLANNED_OUTFIT_ITEMS + "("
                + COLUMN_POI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_POI_OUTFIT_ID + " INTEGER NOT NULL,"
                + COLUMN_POI_CLOTHING_ID + " INTEGER NOT NULL,"
                + COLUMN_POI_CATEGORY + " TEXT NOT NULL,"
                + "FOREIGN KEY(" + COLUMN_POI_OUTFIT_ID + ") REFERENCES " + TABLE_PLANNED_OUTFITS + "(" + COLUMN_PO_ID + ") ON DELETE CASCADE," // При удалении образа, удалять и его элементы
                + "FOREIGN KEY(" + COLUMN_POI_CLOTHING_ID + ") REFERENCES " + TABLE_CLOTHING_ITEMS + "(" + COLUMN_CI_ID + ")"
                + ")";
        db.execSQL(CREATE_PLANNED_OUTFIT_ITEMS_TABLE);
        Log.d(TAG, "onCreate: TABLE_PLANNED_OUTFIT_ITEMS created.");

        prepopulateData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "onUpgrade: Upgrading database from version " + oldVersion + " to " + newVersion + ". Old data will be lost.");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLANNED_OUTFIT_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLANNED_OUTFITS);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED_OUTFIT_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED_OUTFITS);

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


        // Каталоги стилей и погодных условий
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


        // Верх
        addClothingItemFullInternal(db, "Футболка хлопковая", "Верх", "tshirtblue", "Унисекс",
                18, 30, 0, 0,
                List.of("Повседневный", "Спортивный"), // Покрывает 2 стиля
                List.of("Солнечно", "Переменная облачность", "Облачно", "Туман") // Широкий спектр теплой погоды
        );
        addClothingItemFullInternal(db,"Кофта", "Верх", "cardiganblue", "Унисекс",
                8, 15, 0, 1, // Ветрозащитная
                List.of("Повседневный", "Спортивный"), // Покрывает 2 стиля
                List.of("Облачно", "Переменная облачность", "Ветрено", "Туман") // Прохладная, ветреная
        );
        addClothingItemFullInternal(db,"Рубашка классическая", "Верх", "shirtwhite", "Мужской", // Только Мужской
                15, 25, 0, 0,
                List.of("Классический", "Богемный", "Повседневный"), // Покрывает 3 стиля
                List.of("Солнечно", "Облачно", "Переменная облачность", "Туман")
        );
        addClothingItemFullInternal(db,"Блузка элегантная", "Верх", "shirtwhite", "Женский",
                15, 25, 0, 0,
                List.of("Классический", "Богемный", "Повседневный"),
                List.of("Солнечно", "Облачно", "Переменная облачность", "Туман")
        );
        addClothingItemFullInternal(db,"Рубашка фланелевая", "Верх", "shirtblack", "Унисекс",
                8, 18, 0, 0, // Уменьшил верхнюю границу температуры, чтобы не сильно пересекалась с флиской
                List.of("Повседневный"), // Только повседневный
                List.of("Облачно", "Переменная облачность", "Ветрено", "Туман") // Убрал "Прохладно"
        );
        addClothingItemFullInternal(db,"Свитер теплый", "Верх", "cardigangrey", "Унисекс",
                -5, 10, 0, 0,
                List.of("Повседневный", "Классический", "Богемный"), // Спортивный может быть флиска + верхняя
                List.of("Облачно", "Переменная облачность", "Ветрено", "Снег", "Туман")
        );


        // Низ
        addClothingItemFullInternal(db,"Шорты спортивные", "Низ", "shortsblack", "Унисекс",
                22, 35, 0, 0,
                List.of("Спортивный", "Повседневный"),
                List.of("Солнечно", "Переменная облачность")
        );
        addClothingItemFullInternal(db,"Шорты джинсовые", "Низ", "shortsblue", "Унисекс",
                20, 30, 0, 0,
                List.of("Повседневный"),
                List.of("Солнечно", "Переменная облачность", "Облачно")
        );
        addClothingItemFullInternal(db,"Брюки летние", "Низ", "pantsfamiliarblack", "Унисекс",
                18, 28, 0, 0,
                List.of("Классический", "Повседневный"),
                List.of("Солнечно", "Переменная облачность", "Облачно", "Небольшой дождь")
        );
        addClothingItemFullInternal(db,"Штаны спортивные", "Низ", "pantsgray", "Унисекс",
                5, 20, 0, 1,
                List.of("Спортивный", "Повседневный"),
                List.of("Облачно", "Переменная облачность", "Ветрено", "Небольшой дождь") // Убрал "Прохладно"
        );
        addClothingItemFullInternal(db,"Брюки классические", "Низ", "pantsblack", "Мужской", // Только Мужской
                10, 22, 0, 0,
                List.of("Классический", "Богемный"),
                List.of("Солнечно", "Облачно", "Переменная облачность", "Ветрено", "Небольшой дождь") // Добавил Ветрено
        );
        addClothingItemFullInternal(db,"Джинсы", "Низ", "pantsfamiliarblue", "Унисекс", // Переименовал из "Джинсы" для ясности, скорректировал температуру
                0, 20, 0, 1,
                List.of("Повседневный", "Богемный"), // Джинсы универсальны
                List.of("Облачно", "Переменная облачность", "Ветрено", "Небольшой дождь", "Дождь", "Туман")
        );
        addClothingItemFullInternal(db,"Джинсы утепленные", "Низ", "pantsfamiliarblue", "Унисекс",
                -15, 5, 0, 1, // Скорректировал температуру
                List.of("Повседневный"),
                List.of("Облачно", "Снег", "Ветрено") // Убрал дождь для утепленных, если они не водоотталкивающие
        );
        addClothingItemFullInternal(db,"Штаны утепленные Спортивные", "Низ", "pantsgray", "Унисекс",
                -20, 0, 1, 1,
                List.of("Спортивный"),
                List.of("Снег", "Ветрено")
        );
        addClothingItemFullInternal(db,"Брюки утеплённые", "Низ", "pantsblack", "Унисекс",
                -20, 0, 0, 0,
                List.of("Классический", "Богемный"),
                List.of("Облачно", "Снег", "Ветрено")
        );
        addClothingItemFullInternal(db,"Юбка джинсовая", "Низ", "skirtblue", "Женский",
                18, 30, 0, 0,
                List.of("Повседневный"),
                List.of("Солнечно", "Переменная облачность", "Облачно")
        );
        addClothingItemFullInternal(db,"Юбка летняя", "Низ", "skirtwhite", "Женский",
                18, 30, 0, 0,
                List.of("Повседневный"),
                List.of("Солнечно", "Переменная облачность", "Облачно")
        );


        addClothingItemFullInternal(db,"Платье летнее легкое", "Платья/Юбки", "dresswhite", "Женский",
                20, 35, 0, 0,
                List.of("Повседневный", "Богемный"), // Заменил "Вечерний" на "Богемный" для примера
                List.of("Солнечно", "Переменная облачность")
        );
        addClothingItemFullInternal(db,"Платье трикотажное", "Платья/Юбки", "dressblack", "Женский",
                10, 23, 0, 0,
                List.of("Повседневный", "Классический", "Богемный"), // Расширил стили
                List.of("Облачно", "Переменная облачность", "Ветрено", "Туман") // Убрал "Прохладно"
        );


        // Верхняя одежда
        addClothingItemFullInternal(db,"Пиджак", "Верхняя одежда", "jacketclassicblack", "Унисекс", // Только Мужской
                5, 15, 0, 0,
                List.of("Классический", "Богемный", "Повседневный"), // Покрывает 3 стиля
                List.of("Солнечно", "Облачно", "Переменная облачность", "Туман", "Небольшой дождь")
        );
        addClothingItemFullInternal(db,"Куртка джинсовая", "Верхняя одежда", "jacketblue", "Унисекс",
                12, 15, 0, 0, // Немного поднял min_temp
                List.of("Повседневный", "Богемный"), // Добавил Богемный
                List.of("Солнечно", "Переменная облачность", "Облачно", "Ветрено", "Туман", "Дождь")
        );
        addClothingItemFullInternal(db,"Куртка кожаная", "Верхняя одежда", "jacketblack", "Унисекс",
                8, 15, 0, 1,
                List.of("Повседневный", "Классический", "Богемный"), // Добавил Богемный
                List.of("Облачно", "Переменная облачность", "Ветрено", "Туман", "Небольшой дождь", "Дождь") // Убрал "Прохладно"
        );
        addClothingItemFullInternal(db,"Пуховик легкий", "Верхняя одежда", "jacketwarmgrey", "Унисекс",
                -10, 10, 1, 1, // Скорректировал max_temp
                List.of("Повседневный", "Спортивный"),
                List.of("Облачно", "Переменная облачность", "Ветрено", "Небольшой дождь", "Дождь", "Снег") // Добавил Снег
        );
        addClothingItemFullInternal(db,"Зимняя куртка", "Верхняя одежда", "jacketwarmblue", "Унисекс",
                -25, 0, 1, 1, // Скорректировал max_temp
                List.of("Повседневный", "Спортивный"), // Парка может быть и спортивной
                List.of("Облачно", "Снег", "Ветрено") // Убрал "Холодно", "Мороз"
        );
        // НУЖНА Верхняя одежда для "Классический", "Богемный" на холод и очень холод.
        addClothingItemFullInternal(db,"Пальто классическое", "Верхняя одежда", "jacketclassicgrey", "Унисекс",
                -10, 8, 0, 1, // Шерстяное пальто может быть ветрозащитным
                List.of("Классический", "Богемный"),
                List.of("Облачно", "Переменная облачность", "Ветрено", "Снег", "Туман")
        );
        addClothingItemFullInternal(db,"Пальто утеплённое", "Верхняя одежда", "jacketclassicgrey", "Унисекс",
                -20, -5, 0, 1, // Шерстяное пальто может быть ветрозащитным
                List.of("Классический", "Богемный", "Повседневный"),
                List.of("Облачно", "Переменная облачность", "Ветрено", "Снег", "Туман")
        );


        addClothingItemFullInternal(db,"Кроссовки летние", "Обувь", "sneakerswhite", "Унисекс",
                15, 30, 0, 0,
                List.of("Повседневный", "Спортивный"), // Добавил Богемный
                List.of("Солнечно", "Переменная облачность", "Облачно") // Убрал дождь для летних кроссовок
        );
        addClothingItemFullInternal(db,"Кроссовки демисезонные", "Обувь", "sneakersblack", "Унисекс",
                5, 18, 1, 0, // Сделал водонепроницаемыми
                List.of("Повседневный", "Спортивный"),
                List.of("Облачно", "Переменная облачность", "Небольшой дождь", "Дождь", "Ветрено", "Туман") // Убрал "Прохладно"
        );
        addClothingItemFullInternal(db,"Ботинки зимние утепленные", "Обувь", "bootsgray", "Унисекс",
                -25, 5, 1, 1,
                List.of("Повседневный", "Спортивный", "Классический", "Богемный"), // Могут быть и для активного отдыха
                List.of("Снег", "Ветрено") // Убрал "Холодно", "Мороз"
        );
        addClothingItemFullInternal(db,"Туфли классические", "Обувь", "shoesblack", "Унисекс",
                10, 25, 0, 0,
                List.of("Классический", "Богемный"),
                List.of("Солнечно", "Облачно", "Переменная облачность", "Туман") // Не для дождя
        );
        addClothingItemFullInternal(db,"Туфли женские", "Обувь", "shoesbrown", "Женский",
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
                values.put(COLUMN_STYLE_NAME_USER, style);
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
                values.put(COLUMN_CLOTHING_NAME_USER_TABLE, item);
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
                values.put(COLUMN_ACCESSORY_NAME_USER_TABLE, accessory);
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
        Log.d("DB_DEBUG", "Updating user ID: " + user.getUserId() +
                ", New city: " + user.getCity());

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, user.getName());
        values.put(COLUMN_BIRTH_DATE, user.getBirthDate());
        values.put(COLUMN_LOGIN, user.getLogin());
        values.put(COLUMN_GENDER, user.getGender());
        values.put(COLUMN_LANGUAGE, user.getLanguage());
        values.put(COLUMN_AVATAR, user.getAvatarResId());
        values.put(COLUMN_CITY, user.getCity());

        int rowsAffected = db.update(
                TABLE_USERS,
                values,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(user.getUserId())}
        );

        Log.d("DB_DEBUG", "Rows affected: " + rowsAffected);
        return rowsAffected > 0;
    }

    // В DatabaseHelper.java
    public boolean deleteUser(long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Удаляем связанные данные (цвета, стили и т.д.)
            db.delete(TABLE_USER_COLORS, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
            db.delete(TABLE_USER_STYLES, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
            db.delete(TABLE_WARDROBE, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
            db.delete(TABLE_ACCESSORIES, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});

            // Удаляем самого пользователя
            int rowsAffected = db.delete(TABLE_USERS, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});

            db.setTransactionSuccessful();
            return rowsAffected > 0;
        } finally {
            db.endTransaction();
        }
    }

    private long addClothingItemFullInternal(SQLiteDatabase db, String name, String category, String imageRes, String genderTarget,
                                             int minTemp, int maxTemp, int isWaterproof, int isWindproof,
                                             List<String> styleNames, List<String> conditionNames) {
        Log.d(TAG, "addClothingItemFullInternal for: " + name);
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
            clothingItemId = db.insertOrThrow(TABLE_CLOTHING_ITEMS, null, itemValues); // Используем переданный db
            if (clothingItemId != -1) {
                if (styleNames != null) {
                    for (String styleName : styleNames) {
                        long styleId = getStyleIdByNameInternal(db, styleName); // Передаем db
                        if (styleId != -1) linkClothingToStyle(db, clothingItemId, styleId); // Передаем db
                        else Log.w(TAG, "Style not found in catalog for linking: " + styleName);
                    }
                }
                if (conditionNames != null) {
                    for (String conditionName : conditionNames) {
                        long conditionId = getWeatherConditionIdByNameInternal(db, conditionName); // Передаем db
                        if (conditionId != -1) linkClothingToWeatherCondition(db, clothingItemId, conditionId); // Передаем db
                        else Log.w(TAG, "Weather condition not found for linking: " + conditionName);
                    }
                }
                Log.d(TAG, "Added clothing item (from prepop) '" + name + "' ID: " + clothingItemId);
            } else {
                Log.e(TAG, "Failed to add clothing item (from prepop): " + name);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in addClothingItemFullInternal for item: " + name, e);
            clothingItemId = -1;
        }
        return clothingItemId;
    }

    public long addClothingItemFull(String name, String category, String imageRes, String genderTarget,
                                    int minTemp, int maxTemp, int isWaterproof, int isWindproof,
                                    List<String> styleNames, List<String> conditionNames) {
        SQLiteDatabase db = this.getWritableDatabase();
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
                        long styleId = getStyleIdByNameInternal(db, styleName); // Вызываем public версию без db
                        if (styleId != -1) linkClothingToStyle(db, clothingItemId, styleId); // link... принимает db
                        else Log.w(TAG, "Style not found in catalog for linking: " + styleName);
                    }
                }
                if (conditionNames != null) {
                    for (String conditionName : conditionNames) {
                        long conditionId = getWeatherConditionIdByNameInternal(db, conditionName); // Вызываем public версию без db
                        if (conditionId != -1) linkClothingToWeatherCondition(db, clothingItemId, conditionId); // link... принимает db
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

    private long getStyleIdByNameInternal(SQLiteDatabase db, String styleName) {
        Cursor cursor = null;
        long styleId = -1;
        try {
            cursor = db.query(TABLE_STYLES_CATALOG, new String[]{COLUMN_SC_ID},
                    COLUMN_SC_NAME + "=?", new String[]{styleName}, null, null, null);
            if (cursor.moveToFirst()) {
                styleId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SC_ID));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getStyleIdByNameInternal for style: " + styleName, e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return styleId;
    }

    private long getWeatherConditionIdByNameInternal(SQLiteDatabase db, String conditionName) {
        Cursor cursor = null;
        long conditionId = -1;
        try {
            cursor = db.query(TABLE_WEATHER_CONDITIONS_CATALOG, new String[]{COLUMN_WCC_ID},
                    COLUMN_WCC_NAME + "=?", new String[]{conditionName}, null, null, null);
            if (cursor.moveToFirst()) {
                conditionId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_WCC_ID));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getWeatherConditionIdByNameInternal for condition: " + conditionName, e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return conditionId;
    }

    public long getStyleIdByName(String styleName) {
        SQLiteDatabase db = this.getReadableDatabase();
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

    public long getWeatherConditionIdByName(String conditionName) {
        SQLiteDatabase db = this.getReadableDatabase();
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


    public List<ClothingItem> getSuggestedOutfit(int currentTemp, String userGender) {
        List<ClothingItem> allPossibleItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        // Основной фильтр: пол и примерный температурный диапазон
        // Мы немного расширяем температурный диапазон, чтобы было больше кандидатов
        int tempLowerBound = currentTemp - 7; // Например, на 7 градусов ниже
        int tempUpperBound = currentTemp + 7; // Например, на 7 градусов выше

        String query = "SELECT DISTINCT ci.* FROM " + TABLE_CLOTHING_ITEMS + " ci" +
                " WHERE (ci." + COLUMN_CI_GENDER_TARGET + " = ? OR ci." + COLUMN_CI_GENDER_TARGET + " = 'Унисекс')" +
                " AND ci." + COLUMN_CI_MIN_TEMP + " <= ?" +   // Предмет должен начинать подходить при температуре не выше верхней границы нашего расширенного диапазона
                " AND ci." + COLUMN_CI_MAX_TEMP + " >= ?";   // Предмет должен заканчивать подходить при температуре не ниже нижней границы нашего расширенного диапазона

        List<String> selectionArgsList = new ArrayList<>();
        selectionArgsList.add(userGender);
        selectionArgsList.add(String.valueOf(tempUpperBound));
        selectionArgsList.add(String.valueOf(tempLowerBound));

        query += " ORDER BY ci." + COLUMN_CI_CATEGORY; // Сортировка для удобства

        try {
            Log.d(TAG, "Executing WIDER outfit query: " + query);
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
                    allPossibleItems.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting ALL possible outfit items", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d(TAG, "All possible outfit items found (from DB): " + allPossibleItems.size());
        return allPossibleItems;
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

    public List<Long> getStylesForClothingItem(long clothingItemId) {
        List<Long> styleIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(); // Можно получить db здесь
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_CLOTHING_ITEM_STYLES,
                    new String[]{COLUMN_CIS_STYLE_ID_FK},
                    COLUMN_CIS_CLOTHING_ID_FK + "=?",
                    new String[]{String.valueOf(clothingItemId)},
                    null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    styleIds.add(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CIS_STYLE_ID_FK)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting styles for clothing item: " + clothingItemId, e);
        } finally {
            if (cursor != null) cursor.close();
            // Не закрываем db, если он получен через this.getReadableDatabase()
        }
        return styleIds;
    }

    // Получить ID погодных условий, к которым привязан предмет одежды
    public List<Long> getConditionsForClothingItem(long clothingItemId) {
        List<Long> conditionIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(); // Можно получить db здесь
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_CLOTHING_ITEM_CONDITIONS,
                    new String[]{COLUMN_CIC_CONDITION_ID_FK},
                    COLUMN_CIC_CLOTHING_ID_FK + "=?",
                    new String[]{String.valueOf(clothingItemId)},
                    null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    conditionIds.add(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CIC_CONDITION_ID_FK)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting conditions for clothing item: " + clothingItemId, e);
        } finally {
            if (cursor != null) cursor.close();
            // Не закрываем db
        }
        return conditionIds;
    }

    public long saveOutfit(long userId, String weatherDesc, double temperature, String styleName, Map<String, ClothingItem> outfitItemsMap) {
        SQLiteDatabase db = this.getWritableDatabase();
        long outfitId = -1;
        db.beginTransaction();
        try {
            ContentValues outfitValues = new ContentValues();
            outfitValues.put(COLUMN_SO_USER_ID, userId);
            outfitValues.put(COLUMN_SO_DATE_SAVED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            outfitValues.put(COLUMN_SO_WEATHER_DESC, weatherDesc);
            outfitValues.put(COLUMN_SO_TEMPERATURE, temperature);
            outfitValues.put(COLUMN_SO_STYLE, styleName);

            outfitId = db.insertOrThrow(TABLE_SAVED_OUTFITS, null, outfitValues);
            if (outfitId == -1) {
                Log.e(TAG, "Failed to insert into " + TABLE_SAVED_OUTFITS);
                return -1;
            }

            // Сохраняем элементы образа
            for (Map.Entry<String, ClothingItem> entry : outfitItemsMap.entrySet()) {
                String category = entry.getKey();
                ClothingItem item = entry.getValue();
                if (item != null) { // Сохраняем только те элементы, которые были фактически выбраны
                    ContentValues itemValues = new ContentValues();
                    itemValues.put(COLUMN_SOI_OUTFIT_ID, outfitId);
                    itemValues.put(COLUMN_SOI_CLOTHING_ID, item.getClothingId());
                    itemValues.put(COLUMN_SOI_CATEGORY, category);
                    long result = db.insert(TABLE_SAVED_OUTFIT_ITEMS, null, itemValues);
                    if (result == -1) {
                        Log.e(TAG, "Failed to insert item " + item.getName() + " for outfit " + outfitId);
                        // Возможно, нужно выбросить исключение или пометить транзакцию как неуспешную
                    }
                }
            }
            db.setTransactionSuccessful();
            Log.d(TAG, "Outfit saved successfully with ID: " + outfitId);
        } catch (Exception e) {
            Log.e(TAG, "Error saving outfit: " + e.getMessage(), e);
            outfitId = -1; // Устанавливаем -1 в случае ошибки
        } finally {
            db.endTransaction();
        }
        return outfitId;
    }

    public List<SavedOutfit> getSavedOutfits(long userId) {
        List<SavedOutfit> savedOutfits = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor outfitCursor = null;

        try {
            outfitCursor = db.query(
                    TABLE_SAVED_OUTFITS,
                    new String[]{COLUMN_SO_ID, COLUMN_SO_DATE_SAVED, COLUMN_SO_WEATHER_DESC, COLUMN_SO_TEMPERATURE, COLUMN_SO_STYLE},
                    COLUMN_SO_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, COLUMN_SO_DATE_SAVED + " DESC" // Сортировка по дате, от новых к старым
            );

            if (outfitCursor.moveToFirst()) {
                do {
                    long outfitId = outfitCursor.getLong(outfitCursor.getColumnIndexOrThrow(COLUMN_SO_ID));
                    String dateSaved = outfitCursor.getString(outfitCursor.getColumnIndexOrThrow(COLUMN_SO_DATE_SAVED));
                    String weatherDesc = outfitCursor.getString(outfitCursor.getColumnIndexOrThrow(COLUMN_SO_WEATHER_DESC));
                    double temperature = outfitCursor.getDouble(outfitCursor.getColumnIndexOrThrow(COLUMN_SO_TEMPERATURE));
                    String style = outfitCursor.getString(outfitCursor.getColumnIndexOrThrow(COLUMN_SO_STYLE));

                    // Получаем все элементы для текущего образа
                    Map<String, ClothingItem> outfitItemsMap = new LinkedHashMap<>(); // LinkedHashMap для сохранения порядка категорий
                    Cursor itemsCursor = null;
                    try {
                        // Используем JOIN для получения полной информации об одежде сразу
                        String query = "SELECT ci.*, soi." + COLUMN_SOI_CATEGORY +
                                " FROM " + TABLE_SAVED_OUTFIT_ITEMS + " soi" +
                                " JOIN " + TABLE_CLOTHING_ITEMS + " ci ON soi." + COLUMN_SOI_CLOTHING_ID + " = ci." + COLUMN_CI_ID +
                                " WHERE soi." + COLUMN_SOI_OUTFIT_ID + " = ?" +
                                " ORDER BY CASE soi." + COLUMN_SOI_CATEGORY + // Определяем порядок слоев для отображения
                                " WHEN 'головной убор' THEN 1" +
                                " WHEN 'верхняя одежда' THEN 2" +
                                " WHEN 'верх' THEN 3" +
                                " WHEN 'платья/юбки' THEN 4" + // Если платье/юбка, то после верха
                                " WHEN 'низ' THEN 5" +
                                " WHEN 'обувь' THEN 6" +
                                " ELSE 7 END"; // Для других категорий

                        itemsCursor = db.rawQuery(query, new String[]{String.valueOf(outfitId)});

                        if (itemsCursor.moveToFirst()) {
                            do {
                                ClothingItem item = new ClothingItem();
                                item.setClothingId(itemsCursor.getLong(itemsCursor.getColumnIndexOrThrow(COLUMN_CI_ID)));
                                item.setName(itemsCursor.getString(itemsCursor.getColumnIndexOrThrow(COLUMN_CI_NAME)));
                                item.setCategory(itemsCursor.getString(itemsCursor.getColumnIndexOrThrow(COLUMN_CI_CATEGORY)));
                                item.setImageResourceName(itemsCursor.getString(itemsCursor.getColumnIndexOrThrow(COLUMN_CI_IMAGE_RES)));
                                item.setGenderTarget(itemsCursor.getString(itemsCursor.getColumnIndexOrThrow(COLUMN_CI_GENDER_TARGET)));
                                item.setMinTemp(itemsCursor.getInt(itemsCursor.getColumnIndexOrThrow(COLUMN_CI_MIN_TEMP)));
                                item.setMaxTemp(itemsCursor.getInt(itemsCursor.getColumnIndexOrThrow(COLUMN_CI_MAX_TEMP)));
                                item.setWaterproof(itemsCursor.getInt(itemsCursor.getColumnIndexOrThrow(COLUMN_CI_IS_WATERPROOF)) == 1);
                                item.setWindproof(itemsCursor.getInt(itemsCursor.getColumnIndexOrThrow(COLUMN_CI_IS_WINDPROOF)) == 1);

                                String category = itemsCursor.getString(itemsCursor.getColumnIndexOrThrow(COLUMN_SOI_CATEGORY));
                                outfitItemsMap.put(category, item);

                            } while (itemsCursor.moveToNext());
                        }
                    } finally {
                        if (itemsCursor != null) itemsCursor.close();
                    }

                    // Создаем объект SavedOutfit и добавляем в список
                    SavedOutfit savedOutfit = new SavedOutfit(outfitId, userId, dateSaved, weatherDesc, temperature, style, outfitItemsMap);
                    savedOutfits.add(savedOutfit);

                } while (outfitCursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting saved outfits for userId: " + userId, e);
        } finally {
            if (outfitCursor != null) outfitCursor.close();
        }
        return savedOutfits;
    }

    // Метод для удаления сохраненного образа
    public boolean deleteSavedOutfit(long outfitId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;
        db.beginTransaction();
        try {
            // Благодаря ON DELETE CASCADE в TABLE_SAVED_OUTFIT_ITEMS, все связанные элементы будут удалены автоматически.
            rowsAffected = db.delete(TABLE_SAVED_OUTFITS, COLUMN_SO_ID + " = ?",
                    new String[]{String.valueOf(outfitId)});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error deleting saved outfit with ID: " + outfitId, e);
        } finally {
            db.endTransaction();
        }
        return rowsAffected > 0;
    }

    public long savePlannedOutfit(long userId, String planDate, String weatherDesc, double temperature, String styleName, Map<String, ClothingItem> outfitItemsMap) {
        SQLiteDatabase db = this.getWritableDatabase();
        long outfitId = -1;
        db.beginTransaction();
        try {
            ContentValues outfitValues = new ContentValues();
            outfitValues.put(COLUMN_PO_USER_ID, userId);
            outfitValues.put(COLUMN_PO_PLAN_DATE, planDate); // Дата планирования
            outfitValues.put(COLUMN_PO_DATE_CREATED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())); // Дата создания записи
            outfitValues.put(COLUMN_PO_WEATHER_DESC, weatherDesc);
            outfitValues.put(COLUMN_PO_TEMPERATURE, temperature);
            outfitValues.put(COLUMN_PO_STYLE, styleName);

            outfitId = db.insertOrThrow(TABLE_PLANNED_OUTFITS, null, outfitValues);
            if (outfitId == -1) {
                Log.e(TAG, "Failed to insert into " + TABLE_PLANNED_OUTFITS);
                return -1;
            }

            // Сохраняем элементы образа
            for (Map.Entry<String, ClothingItem> entry : outfitItemsMap.entrySet()) {
                String category = entry.getKey();
                ClothingItem item = entry.getValue();
                if (item != null) { // Сохраняем только те элементы, которые были фактически выбраны
                    ContentValues itemValues = new ContentValues();
                    itemValues.put(COLUMN_POI_OUTFIT_ID, outfitId);
                    itemValues.put(COLUMN_POI_CLOTHING_ID, item.getClothingId());
                    itemValues.put(COLUMN_POI_CATEGORY, category);
                    long result = db.insert(TABLE_PLANNED_OUTFIT_ITEMS, null, itemValues);
                    if (result == -1) {
                        Log.e(TAG, "Failed to insert item " + item.getName() + " for planned outfit " + outfitId);
                    }
                }
            }
            db.setTransactionSuccessful();
            Log.d(TAG, "Planned outfit saved successfully with ID: " + outfitId);
        } catch (Exception e) {
            Log.e(TAG, "Error saving planned outfit: " + e.getMessage(), e);
            outfitId = -1;
        } finally {
            db.endTransaction();
        }
        return outfitId;
    }

    // Метод для получения всех запланированных образов для пользователя
    public List<PlannedOutfit> getPlannedOutfits(long userId) {
        List<PlannedOutfit> plannedOutfits = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor outfitCursor = null;

        try {
            outfitCursor = db.query(
                    TABLE_PLANNED_OUTFITS,
                    new String[]{COLUMN_PO_ID, COLUMN_PO_PLAN_DATE, COLUMN_PO_DATE_CREATED, COLUMN_PO_WEATHER_DESC, COLUMN_PO_TEMPERATURE, COLUMN_PO_STYLE},
                    COLUMN_PO_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, COLUMN_PO_PLAN_DATE + " ASC" // Сортировка по дате планирования, от старых к новым
            );

            if (outfitCursor.moveToFirst()) {
                do {
                    long outfitId = outfitCursor.getLong(outfitCursor.getColumnIndexOrThrow(COLUMN_PO_ID));
                    String planDate = outfitCursor.getString(outfitCursor.getColumnIndexOrThrow(COLUMN_PO_PLAN_DATE));
                    String dateCreated = outfitCursor.getString(outfitCursor.getColumnIndexOrThrow(COLUMN_PO_DATE_CREATED));
                    String weatherDesc = outfitCursor.getString(outfitCursor.getColumnIndexOrThrow(COLUMN_PO_WEATHER_DESC));
                    double temperature = outfitCursor.getDouble(outfitCursor.getColumnIndexOrThrow(COLUMN_PO_TEMPERATURE));
                    String style = outfitCursor.getString(outfitCursor.getColumnIndexOrThrow(COLUMN_PO_STYLE));

                    // Получаем все элементы для текущего образа
                    Map<String, ClothingItem> outfitItemsMap = new LinkedHashMap<>();
                    Cursor itemsCursor = null;
                    try {
                        String query = "SELECT ci.*, poi." + COLUMN_POI_CATEGORY +
                                " FROM " + TABLE_PLANNED_OUTFIT_ITEMS + " poi" +
                                " JOIN " + TABLE_CLOTHING_ITEMS + " ci ON poi." + COLUMN_POI_CLOTHING_ID + " = ci." + COLUMN_CI_ID +
                                " WHERE poi." + COLUMN_POI_OUTFIT_ID + " = ?" +
                                " ORDER BY CASE poi." + COLUMN_POI_CATEGORY + // Определяем порядок слоев для отображения
                                " WHEN 'головной убор' THEN 1" +
                                " WHEN 'верхняя одежда' THEN 2" +
                                " WHEN 'верх' THEN 3" +
                                " WHEN 'платья/юбки' THEN 4" +
                                " WHEN 'низ' THEN 5" +
                                " WHEN 'обувь' THEN 6" +
                                " ELSE 7 END"; // Для других категорий

                        itemsCursor = db.rawQuery(query, new String[]{String.valueOf(outfitId)});

                        if (itemsCursor.moveToFirst()) {
                            do {
                                ClothingItem item = new ClothingItem();
                                item.setClothingId(itemsCursor.getLong(itemsCursor.getColumnIndexOrThrow(COLUMN_CI_ID)));
                                item.setName(itemsCursor.getString(itemsCursor.getColumnIndexOrThrow(COLUMN_CI_NAME)));
                                item.setCategory(itemsCursor.getString(itemsCursor.getColumnIndexOrThrow(COLUMN_CI_CATEGORY)));
                                item.setImageResourceName(itemsCursor.getString(itemsCursor.getColumnIndexOrThrow(COLUMN_CI_IMAGE_RES)));
                                item.setGenderTarget(itemsCursor.getString(itemsCursor.getColumnIndexOrThrow(COLUMN_CI_GENDER_TARGET)));
                                item.setMinTemp(itemsCursor.getInt(itemsCursor.getColumnIndexOrThrow(COLUMN_CI_MIN_TEMP)));
                                item.setMaxTemp(itemsCursor.getInt(itemsCursor.getColumnIndexOrThrow(COLUMN_CI_MAX_TEMP)));
                                item.setWaterproof(itemsCursor.getInt(itemsCursor.getColumnIndexOrThrow(COLUMN_CI_IS_WATERPROOF)) == 1);
                                item.setWindproof(itemsCursor.getInt(itemsCursor.getColumnIndexOrThrow(COLUMN_CI_IS_WINDPROOF)) == 1);

                                String category = itemsCursor.getString(itemsCursor.getColumnIndexOrThrow(COLUMN_POI_CATEGORY));
                                outfitItemsMap.put(category, item);

                            } while (itemsCursor.moveToNext());
                        }
                    } finally {
                        if (itemsCursor != null) itemsCursor.close();
                    }

                    PlannedOutfit plannedOutfit = new PlannedOutfit(outfitId, userId, planDate, dateCreated, weatherDesc, temperature, style, outfitItemsMap);
                    plannedOutfits.add(plannedOutfit);

                } while (outfitCursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting planned outfits for userId: " + userId, e);
        } finally {
            if (outfitCursor != null) outfitCursor.close();
        }
        return plannedOutfits;
    }

    // Метод для удаления запланированного образа
    public boolean deletePlannedOutfit(long outfitId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;
        db.beginTransaction();
        try {
            rowsAffected = db.delete(TABLE_PLANNED_OUTFITS, COLUMN_PO_ID + " = ?",
                    new String[]{String.valueOf(outfitId)});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error deleting planned outfit with ID: " + outfitId, e);
        } finally {
            db.endTransaction();
        }
        return rowsAffected > 0;
    }

}