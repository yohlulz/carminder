package to.uk.carminder.app.service;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class CheckStatusTask extends AsyncTask<String, Void, String> {
    private final String LOG_TAG = CheckStatusTask.class.getSimpleName();
    private static final String BASE_URL = "http://carreminder.uk.to/search.php?c=nr_inmatriculare";
    private static final String PARAM_NUMBER_PLATE = "v";
    private static final String PARAM_DAY = "zi";
    private static final String PARAM_MONTH = "luna";
    private static final String PARAM_YEAR = "an";

    private static final int INDEX_NUMBER_PLATE = 0;
    private static final int INDEX_DAY = 1;
    private static final int INDEX_MONTH = 2;
    private static final int INDEX_YEAR = 3;

    private static final int INDEX_FIELDS = 4;

    @Override
    protected String doInBackground(String... params) {
        final Uri statusUri = buildUriFromArguments(params);
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            final URL url = new URL(statusUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            final StringBuilder readLines = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                readLines.append(line);
            }
//                Toast.makeText(getActivity(), readLines.toString(), Toast.LENGTH_LONG).show();
            Log.i(LOG_TAG, readLines.toString());


            //TODO refactor this
            JSONObject status = new JSONObject(readLines.toString());
            Log.i(LOG_TAG, status.getString("plate"));
            Log.i(LOG_TAG, status.getString("MTPL"));
            Log.i(LOG_TAG, status.getString("endDate"));
            Log.i(LOG_TAG, status.getString("startDate"));

        } catch (Exception ex) {
            Log.w(LOG_TAG, ex.getMessage(), ex);
        }
        return null;
    }

    private Uri buildUriFromArguments(String... args) {
        final Calendar cal = Calendar.getInstance();
        final String[] fields = new String[] {"", cal.get(Calendar.DAY_OF_MONTH) + "", cal.get(Calendar.MONTH) + "", cal.get(Calendar.YEAR) + ""};
        for (int i = 0; i < Math.min(args.length, INDEX_FIELDS); i++) {
            fields[i] = args[i];
        }
        return Uri.parse(BASE_URL).buildUpon().appendQueryParameter(PARAM_NUMBER_PLATE, fields[INDEX_NUMBER_PLATE])
                .appendQueryParameter(PARAM_DAY, fields[INDEX_DAY])
                .appendQueryParameter(PARAM_MONTH, fields[INDEX_MONTH])
                .appendQueryParameter(PARAM_YEAR, fields[INDEX_YEAR])
                .build();
    }
}