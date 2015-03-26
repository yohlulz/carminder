package to.uk.carminder.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;

import java.util.logging.Logger;

public class Utility {
    private static final String LOG_TAG = Utility.class.getSimpleName();

    public static final String EMPTY_STRING = "";

    private Utility() {
    }

    public static boolean isStringNullOrEmpty(String value) {
        return (value == null) || value.trim().isEmpty();
    }
}
