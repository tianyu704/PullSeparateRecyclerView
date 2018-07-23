package com.example.fir.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;


/**
 * Created by hugo on 2018/7/23
 */
public class PullSeparateRecyclerView extends RecyclerView {
    /**
     * 最大滑动距离
     */
    private static final float MAX_DELTAY = 10000;
    /**
     * 分离后恢复的动画时长
     */
    private static final long SEPARATE_RECOVER_DURATION = 300;
    /**
     * 摩擦系数
     */
    private static final float FACTOR = 0.3f;
    /**
     * 按下x的缩放比例
     */
    private static final float SCALEX = 1.02f;
    /**
     * 按下y的缩放比例
     */
    private static final float SCALEY = 1.1f;
    /**
     * 展开全部
     */
    private boolean separateAll;

    /**
     * 到达边界时，滑动的起始位置
     */
    private float startY;
    /**
     * 按下时的View
     */
    private View downView;

    private int touchSlop;

    private Rect mTouchFrame;

    private boolean separate = false;
    private boolean showDownAnim;

    /**
     * 原始按下位置(在所有Item中的位置)
     */
    private int originDownPosition;
    /**
     * 按下的位置(在屏幕中的位置)
     */
    private int downPosition;

    /**
     * 上次滑动的位置，用于判断方向
     */
    private float preY;

    private float deltaY;
    private boolean reachTop, reachBottom, move;
    private OnScrollListener mScrollListener;

    public PullSeparateRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.PullSeparateRecyclerView);
        separateAll = t.getBoolean(R.styleable.PullSeparateRecyclerView_separate_all, false);
        showDownAnim = t.getBoolean(R.styleable.PullSeparateRecyclerView_showDownAnim, true);
        t.recycle();
        init();
    }

    public PullSeparateRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public PullSeparateRecyclerView(Context context) {
        super(context);
        init();
    }


    @SuppressWarnings("deprecation")
    private void init() {
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        super.setOnScrollListener(listener);
    }

    /**
     * 是否全部分离
     *
     * @param separateAll 如果为true,那么全部都会分离。否则的话，如果是顶部下拉，只有点击位置之前的Item会分离</br>
     *                    如果是底部上拉，则只有点击位置之后的item会分离。默认为false
     */
    public void setSeparateAll(boolean separateAll) {
        this.separateAll = separateAll;
    }

    public boolean isSeparateAll() {
        return separateAll;
    }

    /**
     * 设置是否显示按下的Item的动画效果
     *
     * @param showDownAnim 默认为true
     */
    public void setShowDownAnim(boolean showDownAnim) {
        this.showDownAnim = showDownAnim;
    }

    public boolean isShowDownAnim() {
        return showDownAnim;
    }

    public void setOnScrollListener(OnScrollListener l) {
        mScrollListener = l;
    }


    //核心代码
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        float currentY = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float downX = ev.getX();
                float downY = ev.getY();
                //通过点击的坐标计算当前的position
                int mFirstPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
                Rect frame = mTouchFrame;
                if (frame == null) {
                    mTouchFrame = new Rect();
                    frame = mTouchFrame;
                }
                final int count = getChildCount();
                for (int i = count - 1; i >= 0; i--) {
                    final View child = getChildAt(i);
                    if (child.getVisibility() == View.VISIBLE) {
                        child.getHitRect(frame);
                        if (frame.contains((int) downX, (int) downY)) {
                            downPosition = mFirstPosition + i;
                            break;
                        }
                    }
                }
                if (showDownAnim) {
                    performDownAnim(downPosition);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //记录到达顶部或底部时手指的位置
                if (!separate) {
                    startY = currentY;
                }
                deltaY = currentY - startY;

                //到达顶部
                if (reachTop) {
                    if (!separateFromTop(currentY)) {
                        return super.dispatchTouchEvent(ev);
                    }
                    return false;
                }
                //到达底部
                if (reachBottom) {
                    if (!separateFromBottom(currentY)) {
                        return super.dispatchTouchEvent(ev);
                    }
                    return false;
                }
                preY = currentY;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                preY = 0;
                recoverDownView();
                if (separate) {
                    separate = false;
                    recoverSeparate();
                    //移动，不响应点击事件
                    if (move) {
                        move = false;
                        return false;
                    }
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean separateFromTop(float currentY) {
        //不能放在外部，否则在顶部滑动没有Fling效果
        if (deltaY > touchSlop) {
            move = true;
        }
        separate = true;
        //超过滑动允许的最大距离，则将起始位置向下移
        if (deltaY > MAX_DELTAY) {
            startY = currentY - MAX_DELTAY;
            //超过最大距离时，出现overScroll效果//有问题
            //return super.dispatchTouchEvent(ev);
        } else if (deltaY < 0) { //为负值时（说明反方向超过了起始位置startY）归0
            deltaY = 0;
            separate = false;
        }

        if (deltaY <= MAX_DELTAY) {
            for (int index = 0; index < getChildCount(); index++) {
                View child = getChildAt(index);
                int multiple = index;
                if (!separateAll) {
                    if (index > downPosition) {
                        multiple = Math.max(1, downPosition);
                    }
                }
                float distance = multiple * deltaY * FACTOR;
                child.setTranslationY(distance);
            }
            //向分离方向的反方向滑动，但位置还未复原时
            if (deltaY != 0 && currentY - preY < 0) {
                return true;
            }
            //deltaY=0，说明位置已经复原，然后交给父类处理
        }
        if (deltaY == 0) {
            return false;
        }
        return true;
    }

    private boolean separateFromBottom(float currentY) {
        if (Math.abs(deltaY) > touchSlop) {
            move = true;
        }
        separate = true;
        //超过滑动允许的最大距离，则将起始位置向上移
        if (Math.abs(deltaY) > MAX_DELTAY) {
            startY = currentY + MAX_DELTAY;
            //超过最大距离时，出现overScroll效果
            //return super.dispatchTouchEvent(ev);
        } else if (deltaY > 0) { //为正值时（说明反方向移动超过起始位置startY），归0
            deltaY = 0;
            separate = false;
        }
        if (Math.abs(deltaY) <= MAX_DELTAY) {
            int visibleCount = getChildCount();
            for (int inedex = 0; inedex < visibleCount; inedex++) {
                View child = getChildAt(inedex);
                int multiple = visibleCount - inedex - 1;
                if (!separateAll) {
                    if (inedex < downPosition) {
                        multiple = Math.max(1, visibleCount - downPosition - 1);
                    }
                }
                float distance = multiple * deltaY * FACTOR;
                child.setTranslationY(distance);
            }
            //向分离方向的反方向滑动，但位置还未复原时
            if (deltaY != 0 && currentY - preY > 0) {
                return true;
            }
            //deltaY=0，说明位置已经复原，然后交给父类处理
            if (deltaY == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 恢复
     */
    private void recoverSeparate() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            ViewCompat.animate(child)
                    .translationY(0).setDuration(SEPARATE_RECOVER_DURATION)
                    .setInterpolator(new AccelerateInterpolator());
        }
    }

    /**
     * 按下的动画
     *
     * @param downPosition 在屏幕中的位置
     */
    private void performDownAnim(int downPosition) {
        downView = getChildAt(downPosition);
        if (downView != null) {
            ViewCompat.animate(downView)
                    .scaleX(SCALEX).scaleY(SCALEY).setDuration(30)
                    .setInterpolator(new AccelerateInterpolator());
        }
    }

    /**
     * 恢复点击的View
     */
    private void recoverDownView() {
        if (showDownAnim && downView != null) {
            ViewCompat.animate(downView)
                    .scaleX(1f).scaleY(1f).setDuration(separate ? SEPARATE_RECOVER_DURATION : 100)
                    .setInterpolator(new AccelerateInterpolator());
        }
    }

    private OnScrollListener listener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//            super.onScrollStateChanged(recyclerView, newState);
            if (mScrollListener != null) {
                mScrollListener.onScrollStateChanged(recyclerView, newState);
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//            super.onScrolled(recyclerView, dx, dy);
            if (mScrollListener != null) {
                mScrollListener.onScrolled(recyclerView, dx, dy);
            }

            reachTop = !recyclerView.canScrollVertically(-1);

            reachBottom = !recyclerView.canScrollVertically(1);

        }
    };
}
