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
    private static final int TYPE_STATUS = 100;
    private static final int TYPE_STATUS_BY_CAR_NUMBER = 200;

    public static final String SELECTION_CAR_PLATE = EventContract.StatusEntry.COLUMN_CAR_NUMBER + " LIKE ?";

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
            case TYPE_STATUS:
                result = dbHelper.getReadableDatabase().query(EventContract.StatusEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case TYPE_STATUS_BY_CAR_NUMBER:
                result = getStatusEntryByCarNumber(uri, projection, sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    private Cursor getStatusEntryByCarNumber(Uri uri, String[] projection, String sortOrder) {
        final String carPlate = EventContract.StatusEntry.getCarPlateFromUri(uri);

        return dbHelper.getReadableDatabase().query(EventContract.StatusEntry.TABLE_NAME,
                                                    projection,
                                                    SELECTION_CAR_PLATE,
                                                    new String[] {carPlate},
                                                    null,
                                                    null,
                                                    sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case TYPE_STATUS:
                return EventContract.StatusEntry.CONTENT_TYPE;

            case TYPE_STATUS_BY_CAR_NUMBER:
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
            URI_MATCHER.addURI(EventContract.CONTENT_AUTHORITY, EventContract.PATH_STATUS, TYPE_STATUS);
            URI_MATCHER.addURI(EventContract.CONTENT_AUTHORITY, EventContract.PATH_STATUS + "/*", TYPE_STATUS_BY_CAR_NUMBER);
        }
    }
}
