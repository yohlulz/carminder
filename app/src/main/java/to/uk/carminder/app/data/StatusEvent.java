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
import java.util.Date;

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
    public static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("dd-MM-yyyy");
        }
    };

    public static final String FIELD_NAME = EventContract.StatusEntry.COLUMN_CAR_NUMBER;
    public static final String FIELD_DESCRIPTION = EventContract.StatusEntry.COLUMN_DESCRIPTION;
    public static final String FIELD_END_DATE = EventContract.StatusEntry.COLUMN_END_DATE;
    public static final String FIELD_START_DATE = EventContract.StatusEntry.COLUMN_START_DATE;

    private static final String FIELD_JSON_PLATE = "plate";
    private static final String FIELD_JSON_MTPL = "MTPL";
    private static final String FIELD_JSON_END_DATE = "endDate";
    private static final String FIELD_JSON_START_DATE = "startDate";
    private static final StatusEvent INVALID_EVENT = new StatusEvent(null, null, "No data received", "Server could be down, please try again later");

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

    private StatusEvent(Long startDate, Long expireDate, String name, String description) {
        values = new ContentValues();
        values.put(FIELD_START_DATE, startDate);
        values.put(FIELD_END_DATE, expireDate);
        values.put(FIELD_NAME, name);
        values.put(FIELD_DESCRIPTION, description);
    }

    public String getAsString(String key) {
        return values.getAsString(key);
    }

    public void put(String key, String value) {
        values.put(key, value);
    }

    public boolean isValid() {
        return !Utility.isStringNullOrEmpty(getExpireDate());
    }

    public String getStartDay() {
        final Long startDate = values.getAsLong(FIELD_START_DATE);
        return (startDate != null) ? DAY_FORMAT.get().format(new Date(startDate)) : Utility.EMPTY_STRING;
    }

    public String getStartMonth() {
        final Long startDate = values.getAsLong(FIELD_START_DATE);
        return (startDate != null) ? MONTH_FORMAT.get().format(new Date(startDate)) : Utility.EMPTY_STRING;
    }

    public String getStartDate() {
        final Long startDate = values.getAsLong(FIELD_START_DATE);
        return (startDate != null) ? DATE_FORMAT.get().format(new Date(startDate)) : Utility.EMPTY_STRING;
    }

    public String getExpireDay() {
        final Long expireDate = values.getAsLong(FIELD_END_DATE);
        return (expireDate != null) ? DAY_FORMAT.get().format(new Date(expireDate)) : Utility.EMPTY_STRING;
    }

    public String getExpireMonth() {
        final Long expireDate = values.getAsLong(FIELD_END_DATE);
        return (expireDate != null) ? MONTH_FORMAT.get().format(new Date(expireDate)) : Utility.EMPTY_STRING;
    }

    public String getExpireDate() {
        final Long expireDate = values.getAsLong(FIELD_END_DATE);
        return (expireDate != null) ? DATE_FORMAT.get().format(new Date(expireDate)) : Utility.EMPTY_STRING;
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

    public static StatusEvent fromJSON(String data) {
        if (Utility.isStringNullOrEmpty(data)) {
            return INVALID_EVENT;
        }

        try {
            final JSONObject event = new JSONObject(data);
            return new StatusEvent(Utility.parse(DATE_FORMAT.get(), event.getString(FIELD_JSON_START_DATE), null),
                                   Utility.parse(DATE_FORMAT.get(), event.getString(FIELD_JSON_END_DATE), null),
                                   event.getString(FIELD_JSON_PLATE),
                                   event.getString(FIELD_JSON_MTPL));

        } catch (JSONException ex) {
            Log.w(LOG_TAG, ex.getMessage(), ex);
        }

        return INVALID_EVENT;
    }

    public static StatusEvent fromCursor(Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            final StatusEvent event = new StatusEvent(cursor.getLong(EventContract.StatusEntry.INDEX_COLUMN_START_DATE),
                                                      cursor.getLong(EventContract.StatusEntry.INDEX_COLUMN_END_DATE),
                                                      cursor.getString(EventContract.StatusEntry.INDEX_COLUMN_CAR_NUMBER),
                                                      cursor.getString(EventContract.StatusEntry.INDEX_COLUMN_DESCRIPTION));
            cursor.close();
            return event;
        }

        return null;
    }
}