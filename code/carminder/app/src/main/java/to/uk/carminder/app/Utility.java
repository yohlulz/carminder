package to.uk.carminder.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import to.uk.carminder.app.data.EventSuggestionProvider;

public class Utility {
    private static final String LOG_TAG = Utility.class.getSimpleName();

    public static final String EMPTY_STRING = "";
    public static final String FIELD_DATA = "FIELD_DATA";

    private Utility() {
    }

    public static boolean isStringNullOrEmpty(String value) {
        return (value == null) || value.trim().isEmpty();
    }

    public static boolean isCollectionNullOrEmpty(Collection<?> values) {
        return (values == null) || values.isEmpty();
    }

    public static boolean isNetworkConnected(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }

    public static void notifyUser(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static Long parse(DateFormat formatter, String date, Long defaultValue) {
        try {
            return formatter.parse(date).getTime();

        } catch (ParseException ex) {
            Log.w(LOG_TAG, ex.getMessage(), ex);
        }

        return defaultValue;
    }

    public static long parse(int year, int month, int day) {
        final Calendar cal = getCurrentDate();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);

        return cal.getTimeInMillis();
    }

    public static Calendar getCurrentDate() {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }
}