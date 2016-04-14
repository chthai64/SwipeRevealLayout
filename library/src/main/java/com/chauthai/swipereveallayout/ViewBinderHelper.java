package com.chauthai.swipereveallayout;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ViewBinderHelper provides a quick and easy solution to restore the open/close state
 * of the items in RecyclerView, ListView, GridView or any view that requires its child view
 * to bind the view to a data object.
 *
 * <p>When you bind you data object to a view, use {@link #bind(SwipeRevealLayout, String)} to
 * save and restore the open/close state of the view.</p>
 *
 * <p>Optionally, if you also want to save and restore the open/close state when the device's
 * orientation is changed, call {@link #saveStates(Bundle)} in {@link android.app.Activity#onSaveInstanceState(Bundle)}
 * and {@link #restoreStates(Bundle)} in {@link android.app.Activity#onRestoreInstanceState(Bundle)}</p>
 */
public class ViewBinderHelper {
    private static final String BUNDLE_MAP_KEY = "ViewBinderHelper_Bundle_Map_Key";
    private HashMap<String, Integer> mapStates = new HashMap<>();

    /**
     * Help to save and restore open/close state of the swipeLayout. Call this method
     * when you bind your view holder with the data object.
     *
     * @param swipeLayout swipeLayout of the current view.
     * @param id a string that uniquely defines the data object of the current view.
     */
    public void bind(final SwipeRevealLayout swipeLayout, final String id) {
        swipeLayout.abort();

        swipeLayout.setDragStateChangeListener(new SwipeRevealLayout.DragStateChangeListener() {
            @Override
            public void onDragStateChanged(int state) {
                mapStates.put(id, state);
            }
        });

        if (!mapStates.containsKey(id)) {
            mapStates.put(id, SwipeRevealLayout.STATE_CLOSE);
            swipeLayout.close(false);
        } else {
            int state = mapStates.get(id);

            if (state == SwipeRevealLayout.STATE_CLOSE || state == SwipeRevealLayout.STATE_CLOSING ||
                    state == SwipeRevealLayout.STATE_DRAGGING) {
                swipeLayout.close(false);
            } else {
                swipeLayout.open(false);
            }
        }
    }

    /**
     * Only if you need to restore open/close state when the orientation is changed.
     * Call this method in {@link android.app.Activity#onSaveInstanceState(Bundle)}
     */
    public void saveStates(Bundle outState) {
        if (outState == null)
            return;

        Bundle statesBundle = new Bundle();
        for (Map.Entry<String, Integer> entry : mapStates.entrySet()) {
            statesBundle.putInt(entry.getKey(), entry.getValue());
        }

        outState.putBundle(BUNDLE_MAP_KEY, statesBundle);
    }


    /**
     * Only if you need to restore open/close state when the orientation is changed.
     * Call this method in {@link android.app.Activity#onRestoreInstanceState(Bundle)}
     */
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public void restoreStates(Bundle inState) {
        if (inState == null)
            return;

        if (inState.containsKey(BUNDLE_MAP_KEY)) {
            HashMap<String, Integer> restoredMap = new HashMap<>();

            Bundle statesBundle = inState.getBundle(BUNDLE_MAP_KEY);
            Set<String> keySet = statesBundle.keySet();

            if (keySet != null) {
                for (String key : keySet) {
                    restoredMap.put(key, statesBundle.getInt(key));
                }
            }

            mapStates = restoredMap;
        }
    }
}
