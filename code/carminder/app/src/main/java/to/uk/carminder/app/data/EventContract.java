package to.uk.carminder.app.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class EventContract {
    public static final String CONTENT_AUTHORITY = "uk.to.carminder.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_STATUS = "status";
    public static final String GROUP_BY = "group";
    public static final String SELECT = "view";

    public static class StatusEntry implements BaseColumns {
        public static final String TABLE_NAME = "status";

        public static final String COLUMN_CAR_NUMBER = "car_number";
        public static final String COLUMN_EVENT_NAME = "event_name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_START_DATE = "start_date";
        public static final String COLUMN_END_DATE = "end_date";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_STATUS).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATUS;

        public static Uri buildStatusUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }


        public static Uri buildStatusByCarPlateUri(String carPlate) {
            return CONTENT_URI.buildUpon().appendPath(SELECT).appendPath(carPlate).build();
        }

        public static Uri buildGroupByUri(String columnName) {
            return CONTENT_URI.buildUpon().appendPath(GROUP_BY).appendPath(columnName).build();
        }

        public static String getCarPlateFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static long getIdFromUri(Uri uri) {
            return ContentUris.parseId(uri);
        }
    }
}
