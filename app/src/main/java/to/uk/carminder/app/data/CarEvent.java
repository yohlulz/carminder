package to.uk.carminder.app.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ovidiu on 3/21/15.
 */
public class CarEvent {
    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
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

    private final Date expireDate;
    private final String name;
    private final String description;

    public CarEvent(Date expireDate, String name, String description) {
        this.expireDate = expireDate;
        this.name = name;
        this.description = description;
    }

    public String getExpireDate() {
        return (expireDate != null) ? DATE_FORMAT.get().format(expireDate) : "";
    }

    public String getExpireMonth() {
        return (expireDate != null) ? MONTH_FORMAT.get().format(expireDate) : "";
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
