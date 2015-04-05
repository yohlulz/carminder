package to.uk.carminder.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;

import to.uk.carminder.app.data.EventSuggestionProvider;

public class Utility {
    private static final String LOG_TAG = Utility.class.getSimpleName();

    public static final String EMPTY_STRING = "";

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

    public static void clearSearchHistory(Context context) {
        new SearchRecentSuggestions(context, EventSuggestionProvider.AUTHORITY, EventSuggestionProvider.MODE).clearHistory();
        notifyUser(context, "Cleared search history");
    }
}
