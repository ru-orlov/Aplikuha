package ru.ruorlov.aplikuha;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBHelper extends SQLiteOpenHelper {

    SQLiteDatabase db;


    private static final String DATABASE_NAME = "aplikuha.db";

    private static final int DATABASE_VERSION = 1;

    // запрос на создание базы данных
    private static final String CREATE_PLAN = "CREATE TABLE plan (_id integer primary key autoincrement, "
                                            + "event_id integer not null);";
    private static final String CREATE_PLAN_IND = "CREATE INDEX ix_plan_event_id ON plan (event_id);";

    private static final String CREATE_ACTIVE = "CREATE TABLE active (_id integer primary key autoincrement, "
                                              + "id_championship integer not null, "
                                              + "event_id integer not null);";
    private static final String CREATE_ACTIVE_IND = "CREATE INDEX ix_active_event_id ON active (event_id);";

    private static final String CREATE_ARCHIVE = "CREATE TABLE archive (_id integer primary key autoincrement, "
                                               + "event_id integer not null);";
    private static final String CREATE_ARCHIVE_IND = "CREATE INDEX ix_archive_event_id ON archive (event_id);";

    private static final String CREATE_OCCASIONS = "CREATE TABLE occasions (_id integer primary key autoincrement, "
                                                 + "occasion_id integer, "
                                                 + "t1 integer,"
                                                 + "t2 integer,"
                                                 + "t3 integer,"
                                                 + "status text," //'archive','active','plan'
                                                 + "name text,"
                                                 + "start integer,"
                                                 + "end integer,"
                                                 + "paused text,"
                                                 + "description text,"
                                                 + "duration_e integer,"
                                                 + "file text); ";
    private static final String CREATE_OCCASIONS_IND = "CREATE INDEX ix_occasions_occasion_id ON occasions (occasion_id);";

    private static final String CREATE_OCCASION_PLACES = "CREATE TABLE occasion_places (_id integer primary key autoincrement, "
                                                 + "occasion_id integer, "
                                                 + "place_id integer); ";
    private static final String CREATE_OCCASION_PLACES_IND = "CREATE INDEX ix_occasion_places_occasion_id ON occasion_places (occasion_id);";



    private static final String CREATE_PLACES = "CREATE TABLE places (_id integer primary key autoincrement, "
                                              + "occasion_id integer, "
                                              + "place_id integer,"
                                              + "name text,"
                                              + "start integer,"
                                              + "end text,"
                                              + "description text,"
                                              + "file text); ";
    private static final String CREATE_PLACES_IND = "CREATE INDEX ix_places_occasion_id ON places (occasion_id);"
                                                  + "CREATE INDEX ix_places_place_id ON places (place_id);";

    private static final String CREATE_PLACE_EVENTS = "CREATE TABLE place_events (_id integer primary key autoincrement, "
                                              + "place_id integer, "
                                              + "event_id integer); ";
    private static final String CREATE_PLACE_EVENTS_IND = "CREATE INDEX ix_place_events_place_id ON place_events (place_id);";

    private static final String CREATE_EVENTS = "CREATE TABLE events (_id integer primary key autoincrement, "
                                              + "event_id integer, "
                                              + "occasion_id integer, "
                                              + "place_id integer,"
                                              + "name text,"
                                              + "comments integer,"
                                              + "start integer,"
                                              + "end text,"
                                              + "likes_minus integer,"
                                              + "likes_plus integer,"
                                              + "description text,"
                                              + "duration integer,"
                                              + "position integer,"
                                              + "paused integer,"
                                              + "file text); ";
    private static final String CREATE_EVENTS_IND = "CREATE INDEX ix_events_occasion_id ON events (occasion_id);"
                                                  + "CREATE INDEX ix_events_place_id ON events (place_id);";

    private static final String CREATE_PARAMS = "CREATE TABLE params (_id integer primary key autoincrement, "
                                              + "param_type integer, " // 1 - occasion param, 2 - event param
                                              + "param_id integer,"
                                              + "param_name text,"
                                              + "param_value integer); ";
    private static final String CREATE_PARAMS_IND = "CREATE INDEX ix_params_param_id ON params (param_id);";

    private static final String CREATE_COMMENTS = "CREATE TABLE comments (_id integer primary key autoincrement, "
                                                + "event_type integer, " // 1-event coment, 2 - place comment, 3 - occasion comment
                                                + "event_id integer,"
                                                + "comment_id integer); ";
    private static final String CREATE_COMMENTS_IND = "CREATE INDEX ix_comments_comment_type ON comments (event_type);"
                                                    + "CREATE INDEX ix_comments_comment_id ON comments (comment_id);";

    private static final String CREATE_EVENT_COMMENTS = "CREATE TABLE event_comment (_id integer primary key autoincrement, "
                                              + "'comment_id' text not null,"
                                              + "'entered' integer,"  //время создания сообщения
                                              + "'event_id' integer," //Идентификатор события
                                              + "'message' text,"     //Текст сообщения
                                              + "'is_admin' text,"    //флаг админа
                                              + "'filename' text,"    //Идентификатор файла + extension
                                              + "'author_name' text," //Имя автора
                                              + "'author' text,"      //id автора
                                              + "'author_social' text); ";
    private static final String CREATE_EVENT_COMMENTS_IND = "CREATE INDEX ix_event_comment_event_id ON event_comment (event_id);"
            + "CREATE INDEX ix_comments_comment_id ON comments (comment_id);";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    // метод вызывается при создании базы данных
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_PLAN);
        System.out.println("plan created...");
        database.execSQL(CREATE_PLAN_IND);
        System.out.println("indexes for plan created...");

        database.execSQL(CREATE_ACTIVE);
        System.out.println("active created...");
        database.execSQL(CREATE_ACTIVE_IND);
        System.out.println("indexes for active created...");

        database.execSQL(CREATE_ARCHIVE);
        System.out.println("archive created...");
        database.execSQL(CREATE_ARCHIVE_IND);
        System.out.println("indexes for archive created...");

        database.execSQL(CREATE_OCCASIONS);
        System.out.println("occasions creates...");
        database.execSQL(CREATE_OCCASIONS_IND);
        System.out.println("indexes for occasions created...");

        database.execSQL(CREATE_OCCASION_PLACES);
        System.out.println("occasions_places creates...");
        database.execSQL(CREATE_OCCASION_PLACES_IND);
        System.out.println("indexes for occasions_places created...");

        database.execSQL(CREATE_PLACES);
        System.out.println("places created...");
        database.execSQL(CREATE_PLACES_IND);
        System.out.println("indexes for places created...");

        database.execSQL(CREATE_PLACE_EVENTS);
        System.out.println("place_events...");
        database.execSQL(CREATE_PLACE_EVENTS_IND);
        System.out.println("indexes for place_events created...");

        database.execSQL(CREATE_EVENTS);
        System.out.println("occasions events...");
        database.execSQL(CREATE_EVENTS_IND);
        System.out.println("indexes for events created...");

        database.execSQL(CREATE_PARAMS);
        System.out.println("occasions params...");
        database.execSQL(CREATE_PARAMS_IND);
        System.out.println("indexes for params created...");

        database.execSQL(CREATE_COMMENTS);
        System.out.println("comments...");
        database.execSQL(CREATE_COMMENTS_IND);
        System.out.println("indexes for comments created...");

        database.execSQL(CREATE_EVENT_COMMENTS);
        System.out.println("event_comments...");
        database.execSQL(CREATE_EVENT_COMMENTS_IND);
        System.out.println("indexes for event_comments created...");
    }


    public Cursor selectFromDB(String str_query){
        db = getWritableDatabase();
        Cursor cursor;
        cursor = db.rawQuery(str_query, null);
    return cursor;
    }



    // метод вызывается при обновлении базы данных, например, когда вы увеличиваете номер версии базы данных
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        Log.w(DBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS plan");
        database.execSQL("DROP TABLE IF EXISTS active");
        database.execSQL("DROP TABLE IF EXISTS archive");
        database.execSQL("DROP TABLE IF EXISTS occasions");
        database.execSQL("DROP TABLE IF EXISTS places");
        database.execSQL("DROP TABLE IF EXISTS place_events");
        database.execSQL("DROP TABLE IF EXISTS events");
        database.execSQL("DROP TABLE IF EXISTS params");
        database.execSQL("DROP TABLE IF EXISTS comments");
        database.execSQL("DROP TABLE IF EXISTS event_comments");
        onCreate(database);
    }
}
