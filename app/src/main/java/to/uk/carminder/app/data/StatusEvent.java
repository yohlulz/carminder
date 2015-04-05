package to.uk.carminder.app.data;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import to.uk.carminder.app.Utility;
import to.uk.carminder.app.service.CheckStatusService;

public class StatusEvent implements Parcelable {
    private static final String LOG_TAG = StatusEvent.class.getSimpleName();
    public static final ThreadLocal<DateFormat> DAY_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("dd");
        }
    };
    public static final ThreadLocal<DateFormat> MONTH_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("MMM");
        }
    };
    public static final ThreadLocal<DateFormat> MONTH_FORMAT_NUMBER = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("MM");
        }
    };
    public static final ThreadLocal<DateFormat> YEAR_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy");
        }
    };
    public static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("dd-MM-yyyy");
        }
    };

    public static final String FIELD_ID = EventContract.StatusEntry._ID;
    public static final String FIELD_CAR_NUMBER = EventContract.StatusEntry.COLUMN_CAR_NUMBER;
    public static final String FIELD_NAME = EventContract.StatusEntry.COLUMN_EVENT_NAME;
    public static final String FIELD_DESCRIPTION = EventContract.StatusEntry.COLUMN_DESCRIPTION;
    public static final String FIELD_END_DATE = EventContract.StatusEntry.COLUMN_END_DATE;
    public static final String FIELD_START_DATE = EventContract.StatusEntry.COLUMN_START_DATE;

    public static final String[] COLUMNS_STATUS_ENTRY = {
            EventContract.StatusEntry._ID,
            EventContract.StatusEntry.COLUMN_CAR_NUMBER,
            EventContract.StatusEntry.COLUMN_EVENT_NAME,
            EventContract.StatusEntry.COLUMN_DESCRIPTION,
            EventContract.StatusEntry.COLUMN_START_DATE,
            EventContract.StatusEntry.COLUMN_END_DATE
    };
    public static final int INDEX_COLUMN_ID = 0;
    public static final int INDEX_COLUMN_CAR_NUMBER = 1;
    public static final int INDEX_COLUMN_EVENT_NAME = 2;
    public static final int INDEX_COLUMN_DESCRIPTION = 3;
    public static final int INDEX_COLUMN_START_DATE = 4;
    public static final int INDEX_COLUMN_END_DATE = 5;


    private static final String FIELD_JSON_PLATE = "plate";
    private static final String FIELD_JSON_MTPL = "MTPL";
    private static final String FIELD_JSON_END_DATE = "endDate";
    private static final String FIELD_JSON_START_DATE = "startDate";
    private static final StatusEvent INVALID_EVENT = new StatusEvent(null, null, null, "No data received", "Server could be down, please try again later");

    public static final Parcelable.Creator<StatusEvent> CREATOR = new Parcelable.Creator<StatusEvent>() {
        @Override
        public StatusEvent createFromParcel(Parcel source) {
            return new StatusEvent(ContentValues.CREATOR.createFromParcel(source));
        }

        @Override
        public StatusEvent[] newArray(int size) {
            return new StatusEvent[size];
        }
    };

    private final ContentValues values;

    public StatusEvent() {
        this(new ContentValues());
    }

    private StatusEvent(ContentValues values) {
        this.values = values;
    }

    private StatusEvent(String name, Long startDate, Long expireDate, String carNumber, String description) {
        values = new ContentValues();
        values.put(FIELD_NAME, name);
        values.put(FIELD_START_DATE, startDate);
        values.put(FIELD_END_DATE, expireDate);
        values.put(FIELD_DESCRIPTION, description);
        values.put(FIELD_CAR_NUMBER, carNumber);
    }

    public String getAsString(String key) {
        return values.getAsString(key);
    }

    public void put(String key, String value) {
        values.put(key, value);
    }

    public void put(String key, Integer value) {
        values.put(key, value);
    }

    public boolean isValid() {
        return !Utility.isStringNullOrEmpty(getExpireDate());
    }

    public String getStartDay() {
        return getFormated(DAY_FORMAT, FIELD_START_DATE);
    }

    public String getStartMonth() {
        return getFormated(MONTH_FORMAT, FIELD_START_DATE);
    }

    public String getStartDate() {
        return getFormated(DATE_FORMAT, FIELD_START_DATE);
    }

    public String getStartYear() {
        return getFormated(YEAR_FORMAT, FIELD_START_DATE);
    }

    public String getExpireDay() {
        return getFormated(DAY_FORMAT, FIELD_END_DATE);
    }

    public String getExpireMonth() {
        return getFormated(MONTH_FORMAT, FIELD_END_DATE);
    }

    public String getExpireDate() {
        return getFormated(DATE_FORMAT, FIELD_END_DATE);
    }

    public String getExpireYear() {
        return getFormated(YEAR_FORMAT, FIELD_END_DATE);
    }

    private String getFormated(ThreadLocal<DateFormat> df, String field) {
        final Long date = values.getAsLong(field);
        return (date != null) ? df.get().format(new Date(date)) : Utility.EMPTY_STRING;
    }

    public ContentValues getContentValues() {
        return values;
    }

    @Override
    public int describeContents() {
        return values.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        values.writeToParcel(dest, flags);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        if (that == this) {
            return true;
        }
        if (that instanceof StatusEvent) {
            return values.equals(((StatusEvent) that).values);
        }
        return false;
    }

    public static StatusEvent fromJSON(String data) {
        if (Utility.isStringNullOrEmpty(data)) {
            return INVALID_EVENT;
        }
        try {
            final JSONObject event = new JSONObject(data);
            return new StatusEvent(FIELD_JSON_MTPL,
                                   Utility.parse(DATE_FORMAT.get(), event.getString(FIELD_JSON_START_DATE), null),
                                   Utility.parse(DATE_FORMAT.get(), event.getString(FIELD_JSON_END_DATE), null),
                                   event.getString(FIELD_JSON_PLATE),
                                   event.getString(FIELD_JSON_MTPL));

        } catch (JSONException ex) {
            Log.w(LOG_TAG, ex.getMessage(), ex);
        }

        return INVALID_EVENT;
    }

    public static Set<StatusEvent> fromCursor(Cursor cursor) {
        if (cursor == null) {
            return Collections.emptySet();
        }
        final Set<StatusEvent> result = new HashSet<>();
        while (cursor.moveToNext()) {
            final StatusEvent event = new StatusEvent(cursor.getString(INDEX_COLUMN_EVENT_NAME),
                                                        cursor.getLong(INDEX_COLUMN_START_DATE),
                                                        cursor.getLong(INDEX_COLUMN_END_DATE),
                                                        cursor.getString(INDEX_COLUMN_CAR_NUMBER),
                                                        cursor.getString(INDEX_COLUMN_DESCRIPTION));
            event.put(FIELD_ID, cursor.getInt(INDEX_COLUMN_ID));
            result.add(event);
        }

        return result;
    }
}