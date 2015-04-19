package to.uk.carminder.app.data;

import android.content.SearchRecentSuggestionsProvider;

public class EventSuggestionProvider extends SearchRecentSuggestionsProvider {
    public static final String AUTHORITY = "uk.to.carminder.app.SearchProvider";
    public static final int MODE = DATABASE_MODE_QUERIES;

    public EventSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
