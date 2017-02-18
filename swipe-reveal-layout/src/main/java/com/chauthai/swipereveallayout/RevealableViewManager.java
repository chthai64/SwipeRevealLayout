package com.chauthai.swipereveallayout;

import android.graphics.Rect;
import android.view.View;

import com.chauthai.swipereveallayout.RevealableViewModel;

import static com.chauthai.swipereveallayout.SwipeRevealLayout.*;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by hydra on 2017/2/16.
 */

public class RevealableViewManager {

    private HashSet<RevealableViewModel> list = new HashSet<>();

    private HashMap<Integer, RevealableViewModel> edgeMap = new HashMap<>();
    private HashMap<View, RevealableViewModel> viewMap = new HashMap<>();

    public RevealableViewManager() {

    }

    public void putRevealableView(RevealableViewModel group) {
        list.add(group);
        generateDataSet();
    }

    public RevealableViewModel getGroupFromView(View view) {
        return viewMap.get(view);
    }

    public RevealableViewModel getGroupFromEdge(int edge) {
        return edgeMap.get(edge);
    }

    public Rect getMainOpenRect(Rect main, int edge) {
        Rect mRectMainOpen = new Rect();
        View view = getGroupFromEdge(edge).getView();
        switch (edge) {
            case DRAG_EDGE_LEFT:
                mRectMainOpen.set(main.left + view.getWidth(), main.top, main.right + view.getWidth(), main.bottom);
                break;
            case DRAG_EDGE_RIGHT:
                mRectMainOpen.set(main.left - view.getWidth(), main.top, main.right - view.getWidth(), main.bottom);
                break;

        }
        return mRectMainOpen;
    }

    public void onViewPositionChanged(int dx) {
        for(RevealableViewModel model : list) {
            model.getView().offsetLeftAndRight(dx);
        }
    }

    private void generateDataSet() {
        edgeMap.clear();
        viewMap.clear();
        for (RevealableViewModel group : list) {
            edgeMap.put(group.getDragEdge(), group);
            viewMap.put(group.getView(), group);
        }
    }

}
