package com.chauthai.swipereveallayout;

import android.graphics.Rect;
import android.view.View;

import static com.chauthai.swipereveallayout.SwipeRevealLayout.*;

/**
 * Created by hydra on 2017/2/15.
 */

public class RevealableViewModel {

    private View view;

    private int mDragEdge;

    private Rect mRectOpen = new Rect();
    private Rect mRectClose = new Rect();

    public RevealableViewModel(View view, int edge) {
        this.view = view;
        this.mDragEdge = edge;

        initRect();
    }

    public void layoutClose() {
        view.layout(
                mRectClose.left,
                mRectClose.top,
                mRectClose.right,
                mRectClose.bottom
        );
    }

    public void layoutOpen() {
        view.layout(
                mRectOpen.left,
                mRectOpen.top,
                mRectOpen.right,
                mRectOpen.bottom
        );
    }

    // getter setter

    public int getDragEdge() {
        return mDragEdge;
    }


    public int getWidth() {
        return view.getWidth();
    }

    public View getView() {
        return view;
    }

    private void initRect() {

        mRectClose.set(
                view.getLeft(),
                view.getTop(),
                view.getRight(),
                view.getBottom()
        );

        mRectOpen.set(
                getSecOpenLeft(),
                getSecOpenTop(),
                getSecOpenLeft() + view.getWidth(),
                getSecOpenTop() + view.getHeight()
        );
    }

    private int getSecOpenLeft() {
        //TODO: hotcode mMode
        if (0 == MODE_NORMAL || mDragEdge == DRAG_EDGE_BOTTOM || mDragEdge == DRAG_EDGE_TOP) {
            return mRectClose.left;
        }

        if (mDragEdge == DRAG_EDGE_LEFT) {
            return mRectClose.left + view.getWidth();
        } else {
            return mRectClose.left - view.getWidth();
        }
    }

    private int getSecOpenTop() {
        //TODO: hotcode mMode
        if (0 == MODE_NORMAL || mDragEdge == DRAG_EDGE_LEFT || mDragEdge == DRAG_EDGE_RIGHT) {
            return mRectClose.top;
        }

        if (mDragEdge == DRAG_EDGE_TOP) {
            return mRectClose.top + view.getHeight();
        } else {
            return mRectClose.top - view.getHeight();
        }
    }
}
