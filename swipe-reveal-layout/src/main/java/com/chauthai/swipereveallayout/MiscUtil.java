package com.chauthai.swipereveallayout;

import static com.chauthai.swipereveallayout.SwipeRevealLayout.*;

/**
 * Created by hydra on 2017/2/18.
 */

public class MiscUtil {

    public static String getStateString(int state) {
        switch (state) {
            case STATE_CLOSE:
                return "state_close";

            case STATE_CLOSING:
                return "state_closing";

            case STATE_OPEN:
                return "state_open";

            case STATE_OPENING:
                return "state_opening";

            case STATE_DRAGGING:
                return "state_dragging";

            default:
                return "undefined";
        }
    }
}
