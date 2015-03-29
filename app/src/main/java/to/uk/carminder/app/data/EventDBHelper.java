package to.uk.carminder.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EventDBHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = EventDBHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "carminder.db";
    private static final String TEXT_NOT_NULL = "TEXT NOT NULL";
    private static final String INTEGER_NOT_NULL = "INTEGER NOT NULL";

    private static final int VERSION_INITIAL = 1;
    private static final int VERSION_CURRENT = VERSION_INITIAL;

    public EventDBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION_CURRENT);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format("CREATE TABLE %s (%s TEXT PRIMARY KEY, %s %s);",
                                    EventContract.CarEntry.TABLE_NAME,
                                    EventContract.CarEntry.COLUMN_PLATE,
                                    EventContract.CarEntry.COLUMN_DESCRIPTION, TEXT_NOT_NULL));

        db.execSQL(String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s %s, %s %s, %s %s, %s %s, FOREIGN KEY (%s) REFERENCES %s (%s), UNIQUE (%s, %s) ON CONFLICT REPLACE);",
                EventContract.EventEntry.TABLE_NAME,
                EventContract.EventEntry._ID,
                EventContract.EventEntry.COLUMN_NAME, TEXT_NOT_NULL,
                EventContract.EventEntry.COLUMN_DESCRIPTION, TEXT_NOT_NULL,
                EventContract.EventEntry.COLUMN_END_DATE, INTEGER_NOT_NULL,
                EventContract.EventEntry.COLUMN_CAR_NUMBER, TEXT_NOT_NULL,
  /* FK */      EventContract.EventEntry.COLUMN_CAR_NUMBER, EventContract.CarEntry.TABLE_NAME, EventContract.CarEntry.COLUMN_PLATE,
  /* UNIQUE */  EventContract.EventEntry.COLUMN_CAR_NUMBER, EventContract.EventEntry.COLUMN_NAME
                ));

        db.execSQL(String.format("CREATE TABLE %s (%s TEXT PRIMARY KEY, %s %s, %s %s, %s %s);",
                EventContract.StatusEntry.TABLE_NAME,
                EventContract.StatusEntry.COLUMN_CAR_NUMBER,
                EventContract.StatusEntry.COLUMN_DESCRIPTION, TEXT_NOT_NULL,
                EventContract.StatusEntry.COLUMN_START_DATE, INTEGER_NOT_NULL,
                EventContract.StatusEntry.COLUMN_END_DATE, INTEGER_NOT_NULL
                ));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(LOG_TAG, String.format("onUpgrade from version %d to version %d.", oldVersion, newVersion));
        dropTable(db, EventContract.EventEntry.TABLE_NAME);
        dropTable(db, EventContract.CarEntry.TABLE_NAME);
        dropTable(db, EventContract.StatusEntry.TABLE_NAME);
        onCreate(db);
    }

    private void dropTable(SQLiteDatabase db, String table) {
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", table));
    }
}
