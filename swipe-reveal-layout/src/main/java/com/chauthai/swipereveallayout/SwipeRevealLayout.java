/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 Chau Thai
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.chauthai.swipereveallayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

@SuppressLint("RtlHardcoded")
public class SwipeRevealLayout extends ViewGroup {
    // These states are used only for ViewBindHelper
    protected static final int STATE_CLOSE = 0;
    protected static final int STATE_CLOSING = 1;
    protected static final int STATE_OPEN = 2;
    protected static final int STATE_OPENING = 3;
    protected static final int STATE_DRAGGING = 4;

    private static final int DEAFULT_GLANCING_ANIMATION_PAUSE = 300;
    private static final int DEFAULT_MIN_FLING_VELOCITY = 300; // dp per second
    private static final int DEFAULT_MIN_DIST_REQUEST_DISALLOW_PARENT = 1; // dp

    public static final int DRAG_EDGE_NONE = 0;
    public static final int DRAG_EDGE_LEFT = 0x1;
    public static final int DRAG_EDGE_RIGHT = 0x1 << 1;
    //public static final int DRAG_EDGE_TOP = 0x1 << 2;
    //public static final int DRAG_EDGE_BOTTOM = 0x1 << 3;

    /**
     * The secondary view will be under the main view.
     */
    public static final int MODE_NORMAL = 0;

    /**
     * The secondary view will stick the edge of the main view.
     */
    public static final int MODE_SAME_LEVEL = 1;

    /**
     * Main view is the view which is shown when the layout is closed.
     */
    private View mMainView;

    /**
     * The rectangle position of the main view when the layout is closed.
     */
    private Rect mRectMainClose = new Rect();

    /**
     * The minimum distance (px) to the closest drag edge that the SwipeRevealLayout
     * will disallow the parent to intercept touch event.
     */
    private int mMinDistRequestDisallowParent = 0;

    private final RevealableViewManager revealableViewManager = new RevealableViewManager();

    private boolean mGlancing = false;
    private volatile boolean mAborted = false;
    private volatile boolean mIsScrolling = false;
    private volatile boolean mLockDrag = false;

    private int mMinFlingVelocity = DEFAULT_MIN_FLING_VELOCITY;
    private int mState = STATE_CLOSE;
    private int mMode = MODE_SAME_LEVEL;

    private int mLastMainLeft = 0;
    private int mLastMainTop = 0;

    private int mDragEdge = DRAG_EDGE_LEFT | DRAG_EDGE_RIGHT;
    private int mEnableEdge = DRAG_EDGE_LEFT | DRAG_EDGE_RIGHT;

    private volatile int currentDragEdge = DRAG_EDGE_NONE;

    private ViewDragHelper mDragHelper;
    private GestureDetectorCompat mGestureDetector;

    private DragStateChangeListener mDragStateChangeListener; // only used for ViewBindHelper
    private SwipeListener mSwipeListener;

    private int mOnLayoutCount = 0;

    interface DragStateChangeListener {
        void onDragStateChanged(int state);
    }

    public SwipeRevealLayout(Context context) {
        super(context);
        init(context, null);
    }

    public SwipeRevealLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SwipeRevealLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mSwipeListener != null)
                mSwipeListener.onTouchUp(false);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mSwipeListener != null)
                mSwipeListener.onTouchUp(true);
        }

        mGestureDetector.onTouchEvent(event);
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mSwipeListener != null)
                mSwipeListener.onTouchUp(false);
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (mSwipeListener != null)
                mSwipeListener.onTouchUp(true);
        }
        mDragHelper.processTouchEvent(ev);
        mGestureDetector.onTouchEvent(ev);

        boolean settling = mDragHelper.getViewDragState() == ViewDragHelper.STATE_SETTLING;
        boolean idleAfterScrolled = mDragHelper.getViewDragState() == ViewDragHelper.STATE_IDLE
                && mIsScrolling;

        return settling || idleAfterScrolled;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // get views
        if (getChildCount() >= 2) {
            revealableViewManager.putRevealableView(new RevealableViewModel(getChildAt(0), DRAG_EDGE_LEFT));
            revealableViewManager.putRevealableView(new RevealableViewModel(getChildAt(1), DRAG_EDGE_RIGHT));
            mMainView = getChildAt(getChildCount() - 1);
        } else if (getChildCount() == 1) {
            mMainView = getChildAt(0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO: workaround to avoid re-layout on dragging when subview has changed and cause parent re-layout.
        if(mState == STATE_DRAGGING) return;

        mAborted = false;

        for (int index = 0; index < getChildCount(); index++) {
            final View child = getChildAt(index);

            int left, right, top, bottom;
            left = right = top = bottom = 0;

            final int minLeft = getPaddingLeft();
            final int maxRight = Math.max(r - getPaddingRight() - l, 0);
            final int minTop = getPaddingTop();
            final int maxBottom = Math.max(b - getPaddingBottom() - t, 0);

            int measuredChildHeight = child.getMeasuredHeight();
            int measuredChildWidth = child.getMeasuredWidth();

            // need to take account if child size is match_parent
            final LayoutParams childParams = child.getLayoutParams();
            boolean matchParentHeight = false;
            boolean matchParentWidth = false;

            if (childParams != null) {
                matchParentHeight = (childParams.height == LayoutParams.MATCH_PARENT) ||
                        (childParams.height == LayoutParams.FILL_PARENT);
                matchParentWidth = (childParams.width == LayoutParams.MATCH_PARENT) ||
                        (childParams.width == LayoutParams.FILL_PARENT);
            }

            if (matchParentHeight) {
                measuredChildHeight = maxBottom - minTop;
                childParams.height = measuredChildHeight;
            }

            if (matchParentWidth) {
                measuredChildWidth = maxRight - minLeft;
                childParams.width = measuredChildWidth;
            }

            int dragEdge = DRAG_EDGE_NONE;
            RevealableViewModel group = revealableViewManager.getGroupFromView(child);
            if (group != null) dragEdge = group.getDragEdge();

            switch (dragEdge) {
                case DRAG_EDGE_RIGHT:
                    left = Math.max(r - measuredChildWidth - getPaddingRight() - l, minLeft);
                    top = Math.min(getPaddingTop(), maxBottom);
                    right = Math.max(r - getPaddingRight() - l, minLeft);
                    bottom = Math.min(measuredChildHeight + getPaddingTop(), maxBottom);
                    break;

                case DRAG_EDGE_LEFT:
                    left = Math.min(getPaddingLeft(), maxRight);
                    top = Math.min(getPaddingTop(), maxBottom);
                    right = Math.min(measuredChildWidth + getPaddingLeft(), maxRight);
                    bottom = Math.min(measuredChildHeight + getPaddingTop(), maxBottom);
                    break;

                case DRAG_EDGE_NONE:
                    left = Math.min(getPaddingLeft(), maxRight);
                    top = Math.min(getPaddingTop(), maxBottom);
                    right = Math.min(measuredChildWidth + getPaddingLeft(), maxRight);
                    bottom = Math.min(measuredChildHeight + getPaddingTop(), maxBottom);
                    break;

            }

            child.layout(left, top, right, bottom);

            if (mMode == MODE_SAME_LEVEL) {
                switch (dragEdge) {
                    case DRAG_EDGE_LEFT:
                        group.getView().offsetLeftAndRight(-group.getWidth());
                        break;

                    case DRAG_EDGE_RIGHT:
                        group.getView().offsetLeftAndRight(group.getWidth());
                        break;
                }
            }
        }


        initRects();

        mLastMainLeft = mMainView.getLeft();
        mLastMainTop = mMainView.getTop();

        mOnLayoutCount++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        if (getChildCount() < 2) {
            throw new RuntimeException("Layout must have two children");
        }

        final LayoutParams params = getLayoutParams();

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        final int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);

        int desiredWidth = 0;
        int desiredHeight = 0;

        // first find the largest child
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            if(i == getChildCount() -1) {
                desiredWidth = Math.max(child.getMeasuredWidth(), desiredWidth);
                desiredHeight = Math.max(child.getMeasuredHeight(), desiredHeight);
            }
        }

        for (int i = 0; i < getChildCount() - 1; i++) {
            final View child = getChildAt(i);
            final LayoutParams childParams = child.getLayoutParams();

            final int newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(desiredWidth, MeasureSpec.AT_MOST);
            final int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(desiredHeight, MeasureSpec.AT_MOST);

            child.measure(newWidthMeasureSpec, newHeightMeasureSpec);

            //desiredWidth = Math.max(child.getMeasuredWidth(), desiredWidth);
            //desiredHeight = Math.max(child.getMeasuredHeight(), desiredHeight);
        }

        // taking accounts of padding
        desiredWidth += getPaddingLeft() + getPaddingRight();
        desiredHeight += getPaddingTop() + getPaddingBottom();

        // adjust desired width
        if (widthMode == MeasureSpec.EXACTLY) {
            desiredWidth = measuredWidth;
        } else {
            if (params.width == LayoutParams.MATCH_PARENT) {
                desiredWidth = measuredWidth;
            }

            if (widthMode == MeasureSpec.AT_MOST) {
                desiredWidth = (desiredWidth > measuredWidth) ? measuredWidth : desiredWidth;
            }
        }

        // adjust desired height
        if (heightMode == MeasureSpec.EXACTLY) {
            desiredHeight = measuredHeight;
        } else {
            if (params.height == LayoutParams.MATCH_PARENT) {
                desiredHeight = measuredHeight;
            }

            if (heightMode == MeasureSpec.AT_MOST) {
                desiredHeight = (desiredHeight > measuredHeight) ? measuredHeight : desiredHeight;
            }
        }

        setMeasuredDimension(desiredWidth, desiredHeight);
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * Open the panel to show the secondary view
     *
     * @param animation true to animate the open motion. {@link SwipeListener} won't be
     *                  called if is animation is false.
     */
    public void open(boolean animation) {
        mAborted = false;

        Rect rect = revealableViewManager.getMainOpenRect(mRectMainClose, currentDragEdge);

        if (animation) {
            mState = STATE_OPENING;
            mDragHelper.smoothSlideViewTo(mMainView, rect.left, rect.top);

            if (mDragStateChangeListener != null) {
                mDragStateChangeListener.onDragStateChanged(mState);
            }
        } else {
            mState = STATE_OPEN;
            mDragHelper.abort();

            mMainView.layout(
                    rect.left,
                    rect.top,
                    rect.right,
                    rect.bottom
            );

        }

        ViewCompat.postInvalidateOnAnimation(SwipeRevealLayout.this);
    }

    /**
     * Close the panel to hide the secondary view
     *
     * @param animation true to animate the close motion. {@link SwipeListener} won't be
     *                  called if is animation is false.
     */
    public void close(boolean animation) {
        mAborted = false;

        if (animation) {
            mState = STATE_CLOSING;
            mDragHelper.smoothSlideViewTo(mMainView, mRectMainClose.left, mRectMainClose.top);

            if (mDragStateChangeListener != null) {
                mDragStateChangeListener.onDragStateChanged(mState);
            }

        } else {
            mState = STATE_CLOSE;
            mDragHelper.abort();

            mMainView.layout(
                    mRectMainClose.left,
                    mRectMainClose.top,
                    mRectMainClose.right,
                    mRectMainClose.bottom
            );

        }

        ViewCompat.postInvalidateOnAnimation(SwipeRevealLayout.this);
    }

    /**
     * Animation that swipe a little bit as a glance.
     *
     */

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void doCornerGlanceAnimation(int dragEdge) {
        mGlancing = true;

        Rect rect = revealableViewManager.getMainOpenRect(mRectMainClose, dragEdge);

        mDragHelper.smoothSlideViewTo(mMainView, rect.left / 2, rect.top);

        ViewCompat.postInvalidateOnAnimation(SwipeRevealLayout.this);

    }

    /**
     * Set the minimum fling velocity to cause the layout to open/close.
     *
     * @param velocity dp per second
     */
    public void setMinFlingVelocity(int velocity) {
        mMinFlingVelocity = velocity;
    }

    /**
     * Get the minimum fling velocity to cause the layout to open/close.
     *
     * @return dp per second
     */
    public int getMinFlingVelocity() {
        return mMinFlingVelocity;
    }

    /**
     * Set the edge where the layout can be dragged from.
     *
     * @param dragEdge Can be one of these
     *                 <ul>
     *                 <li>{@link #DRAG_EDGE_LEFT}</li>
     *                 <li>{@link #DRAG_EDGE_RIGHT}</li>
     *                 </ul>
     */
    public void setDragEdge(int dragEdge) {
        mDragEdge = dragEdge;
    }

    /**
     * Get the edge where the layout can be dragged from.
     *
     * @return Can be one of these
     * <ul>
     * <li>{@link #DRAG_EDGE_LEFT}</li>
     * <li>{@link #DRAG_EDGE_RIGHT}</li>
     * </ul>
     */
    public int getDragEdge() {
        return mDragEdge;
    }

    public void setSwipeListener(SwipeListener listener) {
        mSwipeListener = listener;
    }


    /**
     * Set enable to the edges dynamically.
     *
     * @param edges
     */
    public void setEnableEdge(int edges) {
        this.mEnableEdge = edges;
    }

    /**
     * @param lock if set to true, the user cannot drag/swipe the layout.
     */
    public void setLockDrag(boolean lock) {
        mLockDrag = lock;
    }

    /**
     * @return true if the drag/swipe motion is currently locked.
     */
    public boolean isDragLocked() {
        return mLockDrag;
    }

    /**
     * @return true if layout is fully opened, false otherwise.
     */
    public boolean isOpened() {
        return (mState == STATE_OPEN);
    }

    /**
     * @return true if layout is fully closed, false otherwise.
     */
    public boolean isClosed() {
        return (mState == STATE_CLOSE);
    }

    /**
     * Only used for {@link ViewBinderHelper}
     */
    void setDragStateChangeListener(DragStateChangeListener listener) {
        mDragStateChangeListener = listener;
    }

    /**
     * Abort current motion in progress. Only used for {@link ViewBinderHelper}
     */
    protected void abort() {
        mAborted = true;
        mDragHelper.abort();
    }

    /**
     * In RecyclerView/ListView, onLayout should be called 2 times to display children views correctly.
     * This method check if it've already called onLayout two times.
     *
     * @return true if you should call {@link #requestLayout()}.
     */
    protected boolean shouldRequestLayout() {
        return mOnLayoutCount < getChildCount();
    }


    private void initRects() {
        // close position of main view
        mRectMainClose.set(
                mMainView.getLeft(),
                mMainView.getTop(),
                mMainView.getRight(),
                mMainView.getBottom()
        );

    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null && context != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.SwipeRevealLayout,
                    0, 0
            );

            //mDragEdge = a.getInteger(R.styleable.SwipeRevealLayout_dragEdge, DRAG_EDGE_LEFT);
            mMinFlingVelocity = a.getInteger(R.styleable.SwipeRevealLayout_flingVelocity, DEFAULT_MIN_FLING_VELOCITY);
            mMode = a.getInteger(R.styleable.SwipeRevealLayout_mode, MODE_NORMAL);

            mMinDistRequestDisallowParent = a.getDimensionPixelSize(
                    R.styleable.SwipeRevealLayout_minDistRequestDisallowParent,
                    dpToPx(DEFAULT_MIN_DIST_REQUEST_DISALLOW_PARENT)
            );
        }

        mDragHelper = ViewDragHelper.create(this, 1.0f, mDragHelperCallback);
        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_ALL);

        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
    }

    private final GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        boolean hasDisallowed = false;

        @Override
        public boolean onDown(MotionEvent e) {
            mIsScrolling = false;
            hasDisallowed = false;
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mIsScrolling = true;
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mIsScrolling = true;

            if (getParent() != null) {
                boolean shouldDisallow;

                if (!hasDisallowed) {
                    shouldDisallow = getDistToClosestEdge() >= mMinDistRequestDisallowParent;
                    if (shouldDisallow) {
                        hasDisallowed = true;
                    }
                } else {
                    shouldDisallow = true;
                }

                // disallow parent to intercept touch event so that the layout will work
                // properly on RecyclerView or view that handles scroll gesture.
                // TODO: simply intercept all event from parents now.
                getParent().requestDisallowInterceptTouchEvent(true);
            }

            return false;
        }
    };

    private int getDistToClosestEdge() {
        //TODO: later

        return 0;
    }

    private int getHalfwayPivotHorizontal() {
        if (currentDragEdge == DRAG_EDGE_LEFT) {
            return mRectMainClose.left + revealableViewManager.getGroupFromEdge(DRAG_EDGE_LEFT).getWidth() / 2;
        } else {
            return mRectMainClose.right - revealableViewManager.getGroupFromEdge(DRAG_EDGE_RIGHT).getWidth() / 2;
        }
    }

    private float getSlideOffset() {
        switch (currentDragEdge) {
            case DRAG_EDGE_LEFT:
                return (float) (mMainView.getLeft() - mRectMainClose.left) / revealableViewManager.getGroupFromEdge
                        (DRAG_EDGE_LEFT).getWidth();

            case DRAG_EDGE_RIGHT:
                return (float) (mMainView.getLeft() - mRectMainClose.left) / revealableViewManager.getGroupFromEdge
                        (DRAG_EDGE_RIGHT).getWidth();

            default:
                return 0;
        }
    }

    private final ViewDragHelper.Callback mDragHelperCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            mAborted = false;

            if (mLockDrag)
                return false;

            mDragHelper.captureChildView(mMainView, pointerId);
            return false;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            switch (mDragEdge) {

                default:
                    return child.getTop();
            }
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if ((mDragEdge & DRAG_EDGE_LEFT) > 0 && left > mRectMainClose.left && (mEnableEdge & DRAG_EDGE_LEFT) > 0) {
                RevealableViewModel group = revealableViewManager.getGroupFromEdge(DRAG_EDGE_LEFT);
                if (group != null) {
                    currentDragEdge = DRAG_EDGE_LEFT;
                    return Math.max(
                            Math.min(left, mRectMainClose.left + group.getWidth()),
                            mRectMainClose.left
                    );
                }
            }

            if ((mDragEdge & DRAG_EDGE_RIGHT) > 0 && left < mRectMainClose.left && (mEnableEdge & DRAG_EDGE_RIGHT) > 0) {
                RevealableViewModel group = revealableViewManager.getGroupFromEdge(DRAG_EDGE_RIGHT);
                if (group != null) {
                    currentDragEdge = DRAG_EDGE_RIGHT;
                    return Math.max(
                            Math.min(left, mRectMainClose.left),
                            mRectMainClose.left - group.getWidth()
                    );
                }
            }

            return child.getLeft();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            final boolean velRightExceeded = pxToDp((int) xvel) >= mMinFlingVelocity;
            final boolean velLeftExceeded = pxToDp((int) xvel) <= -mMinFlingVelocity;

            final int pivotHorizontal = getHalfwayPivotHorizontal();

            switch (currentDragEdge & mEnableEdge) {
                case DRAG_EDGE_RIGHT:
                    if (velRightExceeded) {
                        close(true);
                    } else if (velLeftExceeded) {
                        open(true);
                    } else {
                        if (mMainView.getRight() < pivotHorizontal) {
                            open(true);
                        } else {
                            close(true);
                        }
                    }
                    break;

                case DRAG_EDGE_LEFT:
                    if (velRightExceeded) {
                        open(true);
                    } else if (velLeftExceeded) {
                        close(true);
                    } else {
                        if (mMainView.getLeft() < pivotHorizontal) {
                            close(true);
                        } else {
                            open(true);
                        }
                    }
                    break;
            }
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            super.onEdgeDragStarted(edgeFlags, pointerId);

            if (mLockDrag) {
                return;
            }

            boolean edgeStartLeft = (mDragEdge == DRAG_EDGE_RIGHT)
                    && edgeFlags == ViewDragHelper.EDGE_LEFT;

            boolean edgeStartRight = (mDragEdge == DRAG_EDGE_LEFT)
                    && edgeFlags == ViewDragHelper.EDGE_RIGHT;


            if (edgeStartLeft || edgeStartRight) {
                mDragHelper.captureChildView(mMainView, pointerId);
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if (mMode == MODE_SAME_LEVEL) {
                if ((mDragEdge & DRAG_EDGE_LEFT) > 0 || (mDragEdge & DRAG_EDGE_RIGHT) > 0) {
                    revealableViewManager.onViewPositionChanged(dx);
                } else {
                    //TODO: later
                    //mSecondaryView.offsetTopAndBottom(dy);
                }
            }

            boolean isMoved = (mMainView.getLeft() != mLastMainLeft) || (mMainView.getTop() != mLastMainTop);
            int currentOffSet = currentDragEdge == DRAG_EDGE_LEFT ? mMainView.getLeft() : mMainView.getRight();
            if (mSwipeListener != null && isMoved) {
                if (mMainView.getLeft() == mRectMainClose.left && mMainView.getTop() == mRectMainClose.top) {
                    mSwipeListener.onClosed(SwipeRevealLayout.this);
                } else if (getSlideOffset() == 1.0f) {
                    mSwipeListener.onOpened(SwipeRevealLayout.this, DRAG_EDGE_LEFT);
                } else if (getSlideOffset() == -1.0f) {
                    mSwipeListener.onOpened(SwipeRevealLayout.this, DRAG_EDGE_RIGHT);
                } else {
                    mSwipeListener.onSlide(SwipeRevealLayout.this, getSlideOffset());
                }
            }

            mLastMainLeft = mMainView.getLeft();
            mLastMainTop = mMainView.getTop();
            ViewCompat.postInvalidateOnAnimation(SwipeRevealLayout.this);
        }


        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            final int prevState = mState;

            switch (state) {
                case ViewDragHelper.STATE_DRAGGING:
                    mState = STATE_DRAGGING;
                    break;

                case ViewDragHelper.STATE_IDLE:
                    if (mGlancing) {
                        mGlancing = false;
                        getHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mDragHelper.smoothSlideViewTo(mMainView, mRectMainClose.left, mRectMainClose.top);
                                ViewCompat.postInvalidateOnAnimation(SwipeRevealLayout.this);
                            }
                        }, DEAFULT_GLANCING_ANIMATION_PAUSE);
                        break;
                    }

                    // drag edge is left or right
                    if (mDragEdge == DRAG_EDGE_LEFT || mDragEdge == DRAG_EDGE_RIGHT) {
                        if (mMainView.getLeft() == mRectMainClose.left) {
                            mState = STATE_CLOSE;
                        } else {
                            mState = STATE_OPEN;
                        }
                    }

                    // drag edge is top or bottom
                    else {
                        if (mMainView.getTop() == mRectMainClose.top) {
                            mState = STATE_CLOSE;
                        } else {
                            mState = STATE_OPEN;
                        }
                    }
                    break;
            }

            if (mDragStateChangeListener != null && !mAborted && prevState != mState) {
                mDragStateChangeListener.onDragStateChanged(mState);
            }
        }
    };

    private int pxToDp(int px) {
        Resources resources = getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private int dpToPx(int dp) {
        Resources resources = getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
