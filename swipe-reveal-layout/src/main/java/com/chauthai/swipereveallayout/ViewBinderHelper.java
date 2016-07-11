/**
 The MIT License (MIT)

 Copyright (c) 2016 Chau Thai

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package com.chauthai.swipereveallayout;

import android.os.Bundle;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

    private Map<String, Integer> mapStates = Collections.synchronizedMap(new HashMap<String, Integer>());
    private Map<String, SwipeRevealLayout> mapLayouts = Collections.synchronizedMap(new HashMap<String, SwipeRevealLayout>());
    private Set<String> lockedSwipeSet = Collections.synchronizedSet(new HashSet<String>());

    private volatile boolean openOnlyOne = false;
    private final Object stateChangeLock = new Object();

    /**
     * Help to save and restore open/close state of the swipeLayout. Call this method
     * when you bind your view holder with the data object.
     *
     * @param swipeLayout swipeLayout of the current view.
     * @param id a string that uniquely defines the data object of the current view.
     */
    public void bind(final SwipeRevealLayout swipeLayout, final String id) {
        if (swipeLayout.shouldRequestLayout()) {
            swipeLayout.requestLayout();
        }

        mapLayouts.values().remove(swipeLayout);
        mapLayouts.put(id, swipeLayout);

        swipeLayout.abort();
        swipeLayout.setDragStateChangeListener(new SwipeRevealLayout.DragStateChangeListener() {
            @Override
            public void onDragStateChanged(int state) {
                mapStates.put(id, state);

                if (openOnlyOne) {
                    closeOthers(id, swipeLayout);
                }
            }
        });

        // first time binding.
        if (!mapStates.containsKey(id)) {
            mapStates.put(id, SwipeRevealLayout.STATE_CLOSE);
            swipeLayout.close(false);
        }

        // not the first time, then close or open depends on the current state.
        else {
            int state = mapStates.get(id);

            if (state == SwipeRevealLayout.STATE_CLOSE || state == SwipeRevealLayout.STATE_CLOSING ||
                    state == SwipeRevealLayout.STATE_DRAGGING) {
                swipeLayout.close(false);
            } else {
                swipeLayout.open(false);
            }
        }

        // set lock swipe
        swipeLayout.setLockDrag(lockedSwipeSet.contains(id));
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

    /**
     * Lock swipe for some layouts.
     * @param id a string that uniquely defines the data object.
     */
    public void lockSwipe(String... id) {
        setLockSwipe(true, id);
    }

    /**
     * Unlock swipe for some layouts.
     * @param id a string that uniquely defines the data object.
     */
    public void unlockSwipe(String... id) {
        setLockSwipe(false, id);
    }

    /**
     * @param openOnlyOne If set to true, then only one row can be opened at a time.
     */
    public void setOpenOnlyOne(boolean openOnlyOne) {
        this.openOnlyOne = openOnlyOne;
    }

    /**
     * Open a specific layout.
     * @param id unique id which identifies the data object which is bind to the layout.
     */
    public void openLayout(final String id) {
        synchronized (stateChangeLock) {
            mapStates.put(id, SwipeRevealLayout.STATE_OPEN);

            if (mapLayouts.containsKey(id)) {
                final SwipeRevealLayout layout = mapLayouts.get(id);
                layout.open(true);
            }
            else if (openOnlyOne) {
                closeOthers(id, mapLayouts.get(id));
            }
        }
    }

    /**
     * Close a specific layout.
     * @param id unique id which identifies the data object which is bind to the layout.
     */
    public void closeLayout(final String id) {
        synchronized (stateChangeLock) {
            mapStates.put(id, SwipeRevealLayout.STATE_CLOSE);

            if (mapLayouts.containsKey(id)) {
                final SwipeRevealLayout layout = mapLayouts.get(id);
                layout.close(true);
            }
        }
    }

    /**
     * Close others swipe layout.
     * @param id layout which bind with this data object id will be excluded.
     * @param swipeLayout will be excluded.
     */
    private void closeOthers(String id, SwipeRevealLayout swipeLayout) {
        synchronized (stateChangeLock) {
            // close other rows if openOnlyOne is true.
            if (getOpenCount() > 1) {
                for (Map.Entry<String, Integer> entry : mapStates.entrySet()) {
                    if (!entry.getKey().equals(id)) {
                        entry.setValue(SwipeRevealLayout.STATE_CLOSE);
                    }
                }

                for (SwipeRevealLayout layout : mapLayouts.values()) {
                    if (layout != swipeLayout) {
                        layout.close(true);
                    }
                }
            }
        }
    }

    private void setLockSwipe(boolean lock, String... id) {
        if (id == null || id.length == 0)
            return;

        if (lock)
            lockedSwipeSet.addAll(Arrays.asList(id));
        else
            lockedSwipeSet.removeAll(Arrays.asList(id));

        for (String s : id) {
            SwipeRevealLayout layout = mapLayouts.get(s);
            if (layout != null) {
                layout.setLockDrag(lock);
            }
        }
    }

    private int getOpenCount() {
        int total = 0;

        for (int state : mapStates.values()) {
            if (state == SwipeRevealLayout.STATE_OPEN || state == SwipeRevealLayout.STATE_OPENING) {
                total++;
            }
        }

        return total;
    }
}
