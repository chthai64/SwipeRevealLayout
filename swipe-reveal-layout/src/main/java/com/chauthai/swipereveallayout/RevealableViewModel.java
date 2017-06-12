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

    public RevealableViewModel(View view, int edge) {
        this.view = view;
        this.mDragEdge = edge;
    }

    public void layoutClose() {
        view.layout(
                getCloseRect().left,
                getCloseRect().top,
                getCloseRect().right,
                getCloseRect().bottom
        );
    }

    public void layoutOpen() {
        view.layout(
                getOpenRect().left,
                getOpenRect().top,
                getOpenRect().right,
                getOpenRect().bottom
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

    //dynamic generate view rect to avoid view hasn't initialized size yet.
    // TODO: still buggy in MODE_SAME_LEVEL since it moves.
    private Rect getCloseRect() {
        return new Rect(
                view.getLeft(),
                view.getTop(),
                view.getRight(),
                view.getBottom()
        );
    }

    private Rect getOpenRect() {
        return new Rect(
                getSecOpenLeft(),
                getSecOpenTop(),
                getSecOpenLeft() + view.getWidth(),
                getSecOpenTop() + view.getHeight()
        );
    }

    private int getSecOpenLeft() {
        if (mDragEdge == DRAG_EDGE_LEFT) {
            return view.getLeft() + view.getWidth();
        } else {
            return view.getLeft() - view.getWidth();
        }
    }

    private int getSecOpenTop() {
        return view.getTop();
    }
}
