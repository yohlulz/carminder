package to.uk.carminder.app.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import to.uk.carminder.app.Utility;

public class CarEvent {
    private static final String LOG_TAG = CarEvent.class.getSimpleName();
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

    private final Car car;
    private final Date expireDate;
    private final String name;
    private final String description;

    private CarEvent(Car car,Date expireDate, String name, String description) {
        this.car = car;
        this.expireDate = expireDate;
        this.name = name;
        this.description = description;
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

    public static Collection<CarEvent> fromJSON(String data) {
        if (Utility.isStringNullOrEmpty(data)) {
            return Collections.emptyList();
        }

        try {
            final JSONObject event = new JSONObject(data);
            final Car car = new Car(event.getString(FIELD_PLATE));
            final Collection<CarEvent> events = new ArrayList<>();
            events.add(new CarEvent(car, DATE_FORMAT.get().parse(event.getString(FIELD_START_DATE)), FIELD_MTPL, event.getString(FIELD_MTPL)));
            events.add(new CarEvent(car, DATE_FORMAT.get().parse(event.getString(FIELD_END_DATE)), FIELD_MTPL, event.getString(FIELD_MTPL)));
            return events;

        } catch (JSONException | ParseException ex) {
            Log.w(LOG_TAG, ex.getMessage(), ex);
        }

        return Collections.emptyList();
    }
}
