package to.uk.carminder.app.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import to.uk.carminder.app.Utility;

public class EventsContainer implements Parcelable {
    private final Bundle values = new Bundle();
    private final ConcurrentMap<StatusEvent, EventState> eventToState = new ConcurrentHashMap<>();
    private final AtomicBoolean addedToBundle = new AtomicBoolean();

    /**
     *
     * @return true is not value found for that key
     */
    public boolean add(StatusEvent event, EventState state) {
        if (EventState.UNCHANGED == state && eventToState.containsKey(event)) {
            return false;
        }
        final EventState oldState = eventToState.put(event, state);
        if (oldState == null || oldState != state) {
            addedToBundle.set(false);
        }

        return oldState == null;
    }

    public void update(EventsContainer container) {
        if (container == null) {
            return;
        }
        clear();
        eventToState.putAll(container.eventToState);
        values.putAll(container.values);
        addedToBundle.set(container.addedToBundle.get());
    }

    public void clear() {
        addedToBundle.set(false);
        eventToState.clear();
        values.clear();
    }

    /**
     * @return true if element removed.
     */
    public boolean remove(StatusEvent event, boolean considerState) {
        if (!considerState) {
            return eventToState.remove(event) != null;
        }
        final EventState state = eventToState.get(event);
        if (state == null) {
            return false;
        }
        if (state == EventState.ADDED) {
            eventToState.remove(event);
            addedToBundle.set(false);
            return true;

        } else {
            add(event, EventState.DELETED);
        }

        return false;
    }

    public List<StatusEvent> getByState(EventState state) {
        ensureValues();
        return values.getParcelableArrayList(String.valueOf(state));
    }

    public Set<StatusEvent> getAllEvents() {
        return new HashSet<>(eventToState.keySet());
    }

    private void ensureValues() {
        if (addedToBundle.compareAndSet(false, true)) {
            values.clear();
            for (Map.Entry<StatusEvent, EventState> entry : eventToState.entrySet()) {
                final String state = entry.getValue().toString();
                ArrayList<StatusEvent> events = values.getParcelableArrayList(state);
                if (events == null) {
                    events = new ArrayList<>();
                    values.putParcelableArrayList(state, events);
                }
                events.add(entry.getKey());
            }
        }
    }

    @Override
    public int describeContents() {
        ensureValues();
        return values.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ensureValues();
        values.writeToParcel(dest, flags);
    }

    public static final Parcelable.Creator<EventsContainer> CREATOR = new Creator<EventsContainer>() {
        @Override
        public EventsContainer createFromParcel(Parcel source) {
            return fromBundle(Bundle.CREATOR.createFromParcel(source));
        }

        @Override
        public EventsContainer[] newArray(int size) {
            return new EventsContainer[size];
        }
    };

    private static EventsContainer fromBundle(Bundle bundle) {
        final EventsContainer container = new EventsContainer();
        for (EventState state : EventState.values()) {
            final List<StatusEvent> events = bundle.getParcelableArrayList(state.toString());
            if (!Utility.isCollectionNullOrEmpty(events)) {
                for (StatusEvent event : events) {
                    container.eventToState.put(event, state);
                }
            }
        }
        container.values.putAll(bundle);
        container.addedToBundle.set(true);

        return container;
    }

    public static enum EventState {
        ADDED, MODIFIED, DELETED, UNCHANGED;
    }
}
