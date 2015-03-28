package to.uk.carminder.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
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

    public static boolean isNetworkConnected(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }

    public static void notifyUser(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static Date parse(DateFormat formatter, String date, Date defaultValue) {
        try {
            return formatter.parse(date);

        } catch (ParseException ex) {
            Log.w(LOG_TAG, ex.getMessage(), ex);
        }

        return defaultValue;
    }

    public static void clearSearchHistory(Context context) {
        new SearchRecentSuggestions(context, EventSuggestionProvider.AUTHORITY, EventSuggestionProvider.MODE).clearHistory();
        Toast.makeText(context, "Cleared search history", Toast.LENGTH_LONG).show();
    }
}
