package to.uk.carminder.app.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class EventContract {
    public static final String CONTENT_AUTHORITY = "uk.to.carminder.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_EVENT = "event";
    public static final String PATH_CAR = "car";

    public static class EventEntry implements BaseColumns {
        public static final String TABLE_NAME = "event";

        public static final String COLUMN_CAR_NUMBER = "car_number";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_START_DATE = "start_date";
        public static final String COLUMN_END_DATE = "end_date";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_REQUESTED = "requested";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENT).build();

    }

    public static class CarEntry implements BaseColumns {
        public static final String TABLE_NAME = "car";

        public static final String COLUMN_PLATE = "plate";
        public static final String COLUMN_DESCRIPTION = "description";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CAR).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CAR;
    }

}
