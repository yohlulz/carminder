package to.uk.carminder.app.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import to.uk.carminder.app.Utility;

public class CheckStatusService extends IntentService {
    private static final String LOG_TAG = CheckStatusService.class.getSimpleName();
    public static final String ACTION_ON_DEMAND = "uk.to.carminder.app.DEMAND";
    public static final String ACTION_NOTIFICATION = "uk.to.carminder.app.NOTIFICATION";

    private static final String FIELD_MAIN_API_URL = "FIELD_API";
    private static final String FIELD_BACKUP_API_URL = "FIELD_BACKUP_API";
    private static final String FIELD_CAR_PLATE = "FIELD_PLATE";
    private static final String FIELD_DATE = "FIELD_DATE";
    private static final String FIELD_REPLY_SUBJECT = "FIELD_REPLY";
    public static final String FIELD_DATA = "FIELD_DATA";
    private static final String FIELD_TIMEOUT = "FIELD_TIMEOUT";

    private static final String WORKER_NAME = "CheckStatus Worker";
    private static final String MAIN_API_URL = "http://carminder.uk.to/index.php?c=nr_inmatriculare";
    private static final String BACKUP_API_URL = "http://carreminder.uk.to/index.php?c=nr_inmatriculare";
    private static final int DEFAULT_TIMEOUT_IN_MILLIS = 10 * 1000;

    public CheckStatusService() {
        super(WORKER_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        TaskBuilder.newInstance()
                   .mainURL(intent.getStringExtra(FIELD_MAIN_API_URL))
                   .backupURL(intent.getStringExtra(FIELD_BACKUP_API_URL))
                   .carPlate(intent.getStringExtra(FIELD_CAR_PLATE))
                   .date(intent.getLongExtra(FIELD_DATE, new Date().getTime()))
                   .replySubject(intent.getStringExtra(FIELD_REPLY_SUBJECT))
                   .timeout(intent.getIntExtra(FIELD_TIMEOUT, DEFAULT_TIMEOUT_IN_MILLIS))
                   .execute(this);
    }

    private static class TaskBuilder {
        private static final String PARAM_NUMBER_PLATE = "v";
        private static final String PARAM_DAY = "zi";
        private static final String PARAM_MONTH = "luna";
        private static final String PARAM_YEAR = "an";

        private final Calendar date = Calendar.getInstance();
        private String mainURL = MAIN_API_URL;
        private String backupURL = BACKUP_API_URL;
        private String carPlate;
        private String replySubject = ACTION_NOTIFICATION;
        private int timeout;

        private TaskBuilder() {
        }

        public TaskBuilder mainURL(String mainURL) {
            if (mainURL != null) {
                this.mainURL = mainURL;
            }
            return this;
        }

        public TaskBuilder backupURL(String backupURL) {
            if (backupURL != null) {
                this.backupURL = backupURL;
            }
            return this;
        }

        public TaskBuilder carPlate(String carPlate) {
            this.carPlate = carPlate;
            return this;
        }

        public TaskBuilder date(long dateInMillis) {
            if (dateInMillis > 0) {
                date.setTimeInMillis(dateInMillis);
            }
            return this;
        }

        public TaskBuilder replySubject(String replySubject) {
            if (!Utility.isStringNullOrEmpty(replySubject)) {
                this.replySubject = replySubject;
            }
            return this;
        }

        public TaskBuilder timeout(int timeout) {
            if (timeout > 0) {
                this.timeout = timeout;
            }
            return this;
        }

        public void execute(Context context) {
            String rawData = fetchData(mainURL);
            rawData = (rawData != null) ? rawData : fetchData(backupURL);

            if (!Utility.isStringNullOrEmpty(replySubject)) {
                final Intent reply = new Intent();
                reply.setAction(replySubject);
                reply.putExtra(FIELD_DATA, rawData);
                LocalBroadcastManager.getInstance(context).sendBroadcast(reply);

            } else {
                Log.w(LOG_TAG, "Skipping request due to empty reply subject.");
            }
        }

        private String fetchData(String baseUrl) {
            final Uri uri = buildUri(baseUrl);
            if (uri == null) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                final URL url = new URL(uri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(timeout / 2);
                urlConnection.setReadTimeout(timeout);
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                //read data
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                final StringBuilder readLines = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    readLines.append(line);
                }

                return readLines.toString();

            } catch (IOException ex) {
                Log.w(LOG_TAG, ex.getMessage(), ex);

            } finally { // close any open streams
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ex) {
                    Log.w(LOG_TAG, ex.getMessage(), ex);
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return null;
        }

        private Uri buildUri(String baseURL) {
            if (Utility.isStringNullOrEmpty(carPlate)) {
                Log.w(LOG_TAG, "Will not build URI for blank car plate");
                return null;
            }
            return Uri.parse(baseURL).buildUpon()
                      .appendQueryParameter(PARAM_NUMBER_PLATE, carPlate)
                      .appendQueryParameter(PARAM_DAY, String.valueOf(date.get(Calendar.DAY_OF_MONTH)))
                      .appendQueryParameter(PARAM_MONTH, String.valueOf(date.get(Calendar.MONTH)))
                      .appendQueryParameter(PARAM_YEAR, String.valueOf(date.get(Calendar.YEAR)))
                      .build();
        }

        public static TaskBuilder newInstance() {
            return new TaskBuilder();
        }
    }

    public static class IntentBuilder {

        private String carPlate;
        private String subject;
        private int timeout;
        private Date date;

        private IntentBuilder() {
        }

        public IntentBuilder carPlate(String carPlate) {
            this.carPlate = carPlate;
            return this;
        }

        public IntentBuilder replySubject(String subject) {
            this.subject = subject;
            return this;
        }

        public IntentBuilder executionTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public IntentBuilder verificationDate(Date date) {
            this.date = date;
            return this;
        }

        public Intent build(Context context) {
            final Intent intent = new Intent(context, CheckStatusService.class);
            if (date != null) {
                intent.putExtra(FIELD_DATE, date.getTime());
            }
            intent.putExtra(FIELD_CAR_PLATE, carPlate);
            intent.putExtra(FIELD_REPLY_SUBJECT, subject);
            intent.putExtra(FIELD_TIMEOUT, timeout);

            return intent;
        }

        public static IntentBuilder newInstance() {
            return new IntentBuilder();
        }
    }
}