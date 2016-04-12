package com.chauthai.swipereveallayout;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chau Thai on 4/11/16.
 */
public class ViewBinderHelper {
    private final Map<String, Integer> mapStates = new HashMap<>();

    public void bind(final SwipeRevealLayout paneLayout, final String id) {
        paneLayout.abort();

        paneLayout.setDragStateChangeListener(new SwipeRevealLayout.DragStateChangeListener() {
            @Override
            public void onDragStateChanged(int state) {
                mapStates.put(id, state);
            }
        });

        if (!mapStates.containsKey(id)) {
            mapStates.put(id, SwipeRevealLayout.STATE_CLOSE);
            paneLayout.close(false);
        } else {
            int state = mapStates.get(id);

            if (state == SwipeRevealLayout.STATE_CLOSE || state == SwipeRevealLayout.STATE_CLOSING ||
                    state == SwipeRevealLayout.STATE_DRAGGING) {
                paneLayout.close(false);
            } else {
                paneLayout.open(false);
            }


        }
    }
}
