package to.uk.carminder.app.service;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import to.uk.carminder.app.Utility;

public class StatusEvent {
    private static final String LOG_TAG = StatusEvent.class.getSimpleName();
    static final ThreadLocal<DateFormat> DAY_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("dd");
        }
    };
    static final ThreadLocal<DateFormat> MONTH_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("MMM");
        }
    };
    static final ThreadLocal<DateFormat> MONTH_FORMAT_NUMBER = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("MM");
        }
    };
    static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("dd-MM-yyyy");
        }
    };

    static final String FIELD_PLATE = "plate";
    static final String FIELD_MTPL = "MTPL";
    static final String FIELD_END_DATE = "endDate";
    static final String FIELD_START_DATE = "startDate";

    private static final StatusEvent INVALID_EVENT = new StatusEvent(null, null, "No data received", "Server could be down, please try again later");

    private final Date startDate;
    private final Date expireDate;
    private final String name;
    private final String description;

    private StatusEvent(Date startDate, Date expireDate, String name, String description) {
        this.startDate = startDate;
        this.expireDate = expireDate;
        this.name = name;
        this.description = description;
    }

    public String getStartDay() {
        return (startDate != null) ? DAY_FORMAT.get().format(startDate) : Utility.EMPTY_STRING;
    }

    public String getStartMonth() {
        return (startDate != null) ? MONTH_FORMAT.get().format(startDate) : Utility.EMPTY_STRING;
    }

    public String getStartDate() {
        return (startDate != null) ? DATE_FORMAT.get().format(startDate) : Utility.EMPTY_STRING;
    }

    public String getExpireDay() {
        return (expireDate != null) ? DAY_FORMAT.get().format(expireDate) : Utility.EMPTY_STRING;
    }

    public String getExpireMonth() {
        return (expireDate != null) ? MONTH_FORMAT.get().format(expireDate) : Utility.EMPTY_STRING;
    }

    public String getExpireDate() {
        return (expireDate != null) ? DATE_FORMAT.get().format(expireDate) : Utility.EMPTY_STRING;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isValid() {
        return !Utility.isStringNullOrEmpty(getStartDate());
    }

    public void populateIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        intent.putExtra(FIELD_PLATE, getName());
        intent.putExtra(FIELD_MTPL, getDescription());
        intent.putExtra(FIELD_START_DATE, getStartDate());
        intent.putExtra(FIELD_END_DATE, getExpireDate());
    }

    public static StatusEvent fromJSON(String data) {
        if (Utility.isStringNullOrEmpty(data)) {
            return INVALID_EVENT;
        }

        try {
            final JSONObject event = new JSONObject(data);
            return new StatusEvent(Utility.parse(DATE_FORMAT.get(), event.getString(FIELD_START_DATE), null),
                                   Utility.parse(DATE_FORMAT.get(), event.getString(FIELD_END_DATE), null),
                                   event.getString(FIELD_PLATE),
                                   event.getString(FIELD_MTPL));

        } catch (JSONException ex) {
            Log.w(LOG_TAG, ex.getMessage(), ex);
        }

        return INVALID_EVENT;
    }

    public static StatusEvent fromIntent(Intent intent) {
        if (intent == null || Utility.isStringNullOrEmpty(intent.getStringExtra(FIELD_PLATE))) {
            return INVALID_EVENT;
        }

        return new StatusEvent(Utility.parse(DATE_FORMAT.get(), intent.getStringExtra(FIELD_START_DATE), null),
                               Utility.parse(DATE_FORMAT.get(), intent.getStringExtra(FIELD_END_DATE), null),
                               intent.getStringExtra(FIELD_PLATE),
                               intent.getStringExtra(FIELD_MTPL));
    }

    public static Bundle toBundle(Intent intent) {
        final StatusEvent event = fromIntent(intent);
        final Bundle result = new Bundle();
        if (event == null || event == INVALID_EVENT) {
            return result;
        }
        result.putString(FIELD_PLATE, event.getName());
        result.putString(FIELD_MTPL, event.getDescription());
        result.putString(FIELD_START_DATE, event.getStartDate());
        result.putString(FIELD_END_DATE, event.getExpireDate());

        return result;
    }
}
