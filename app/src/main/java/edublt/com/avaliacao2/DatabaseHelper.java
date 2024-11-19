package edublt.com.avaliacao2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "TrailDatabase.db";
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_TRAILS = "trails";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TRAIL_ID = "trail_id"; // Nova coluna para identificar a trilha
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String CREATE_TABLE_TRAILS =
            "CREATE TABLE " + TABLE_TRAILS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TRAIL_ID + " TEXT NOT NULL, " +
                    COLUMN_LATITUDE + " REAL NOT NULL, " +
                    COLUMN_LONGITUDE + " REAL NOT NULL, " +
                    COLUMN_TIMESTAMP + " INTEGER NOT NULL)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TRAILS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_TRAILS + " ADD COLUMN " + COLUMN_TRAIL_ID + " TEXT NOT NULL DEFAULT ''");
        }
    }
}
