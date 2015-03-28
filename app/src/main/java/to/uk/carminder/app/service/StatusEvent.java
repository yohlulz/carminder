package to.uk.carminder.app.service;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import to.uk.carminder.app.Utility;

public class StatusEvent {
    private static final String LOG_TAG = StatusEvent.class.getSimpleName();
    private static final ThreadLocal<DateFormat> DAY_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("dd");
        }
    };
    private static final ThreadLocal<DateFormat> MONTH_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("MMM");
        }
    };
    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("dd-MM-yyyy");
        }
    };

    private static final String FIELD_PLATE = "plate";
    private static final String FIELD_MTPL = "MTPL";
    private static final String FIELD_END_DATE = "endDate";
    private static final String FIELD_START_DATE = "startDate";

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

    public String getExpireDay() {
        return (expireDate != null) ? DAY_FORMAT.get().format(expireDate) : Utility.EMPTY_STRING;
    }

    public String getExpireMonth() {
        return (expireDate != null) ? MONTH_FORMAT.get().format(expireDate) : Utility.EMPTY_STRING;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static Collection<StatusEvent> fromJSON(String data) {
        if (Utility.isStringNullOrEmpty(data)) {
            return Collections.emptyList();
        }

        try {
            final JSONObject event = new JSONObject(data);
            final Collection<StatusEvent> events = new ArrayList<>();
            events.add(new StatusEvent(Utility.parse(DATE_FORMAT.get(), event.getString(FIELD_START_DATE), null),
                                       Utility.parse(DATE_FORMAT.get(), event.getString(FIELD_END_DATE), null),
                                       event.getString(FIELD_PLATE),
                                       event.getString(FIELD_MTPL)));
            return events;

        } catch (JSONException ex) {
            Log.w(LOG_TAG, ex.getMessage(), ex);
        }

        return Collections.emptyList();
    }
}
