package com.chauthai.swipereveallayout;

/**
 * Created by hydra on 2017/2/16.
 */

/**
 * Listener for monitoring events about swipe layout.
 */
public interface SwipeListener {
    /**
     * Called when the main view becomes completely closed.
     */
    void onClosed(SwipeRevealLayout view);

    /**
     * Called when the main view becomes completely opened.
     */
    void onOpened(SwipeRevealLayout view);

    /**
     * Called when the main view's position changes.
     *
     * @param slideOffset The new offset of the main view within its range, from 0-1
     */
    void onSlide(SwipeRevealLayout view, float slideOffset);
}