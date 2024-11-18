package edublt.com.avaliacao2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TrailsDB";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_TRAILS = "trails";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_TRAIL_ID = "trail_id";

    private static final String DATABASE_CREATE = "create table " + TABLE_TRAILS + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_TRAIL_ID + " text not null, " +
            COLUMN_LATITUDE + " double not null, " +
            COLUMN_LONGITUDE + " double not null, " +
            COLUMN_TIMESTAMP + " integer not null);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRAILS);
        onCreate(db);
    }
}