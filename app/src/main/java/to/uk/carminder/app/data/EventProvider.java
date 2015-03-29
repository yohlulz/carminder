package to.uk.carminder.app.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class EventProvider extends ContentProvider {
    private static final int TYPE_CAR = 100;
    private static final int TYPE_EVENT = 200;
    private static final int TYPE_STATUS = 300;

    private static final UriMatcher URI_MATCHER = UriMatcherHolder.URI_MATCHER;

    private SQLiteOpenHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new EventDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final Cursor result;
        switch (URI_MATCHER.match(uri)) {
            case TYPE_CAR:
                result = dbHelper.getReadableDatabase().query(EventContract.CarEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case TYPE_EVENT:
                result = dbHelper.getReadableDatabase().query(EventContract.EventEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case TYPE_STATUS:
                result = dbHelper.getReadableDatabase().query(EventContract.StatusEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case TYPE_EVENT:
                return EventContract.EventEntry.CONTENT_TYPE;

            case TYPE_CAR:
                return EventContract.CarEntry.CONTENT_TYPE;

            case TYPE_STATUS:
                return EventContract.StatusEntry.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final Uri result;

        switch (URI_MATCHER.match(uri)) {
            case TYPE_CAR:
                long carRowId = db.insertOrThrow(EventContract.CarEntry.TABLE_NAME, null, values);
                if (carRowId > 0) {
                    result = EventContract.CarEntry.buildCarUri(carRowId);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;

            case TYPE_EVENT:
                long eventRowId = db.insert(EventContract.EventEntry.TABLE_NAME, null, values);
                if (eventRowId > 0) {
                    result = EventContract.EventEntry.buildEventUri(eventRowId);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;

            case TYPE_STATUS:
                long statusRowId = db.insert(EventContract.StatusEntry.TABLE_NAME, null, values);
                if (statusRowId > 0) {
                    result = EventContract.StatusEntry.buildStatusUri(statusRowId);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deletedRows = 0;
        if (selection == null) {
            selection = "1";
        }
        switch (URI_MATCHER.match(uri)) {
            case TYPE_CAR:
                deletedRows = db.delete(EventContract.CarEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case TYPE_EVENT:
                deletedRows = db.delete(EventContract.EventEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case TYPE_STATUS:
                deletedRows = db.delete(EventContract.StatusEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }

        if (deletedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deletedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int updatedRows = 0;
        switch (URI_MATCHER.match(uri)) {
            case TYPE_CAR:
                updatedRows = db.update(EventContract.CarEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case TYPE_EVENT:
                updatedRows = db.update(EventContract.EventEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case TYPE_STATUS:
                updatedRows = db.update(EventContract.StatusEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (updatedRows != 0 || selection == null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updatedRows;
    }

    private static class UriMatcherHolder {
        private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        static {
            URI_MATCHER.addURI(EventContract.CONTENT_AUTHORITY, EventContract.PATH_CAR, TYPE_CAR);

            URI_MATCHER.addURI(EventContract.CONTENT_AUTHORITY, EventContract.PATH_EVENT, TYPE_EVENT);

            URI_MATCHER.addURI(EventContract.CONTENT_AUTHORITY, EventContract.PATH_STATUS, TYPE_STATUS);
        }
    }
}
