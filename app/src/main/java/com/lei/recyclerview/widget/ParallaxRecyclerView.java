package com.lei.recyclerview.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.lei.recyclerview.utils.DisplayUtils;

/**
 * Created by dell on 2017/3/6.
 * 层叠效果列表
 */

public class ParallaxRecyclerView extends RecyclerView {

    private int mStackHeight;

    public ParallaxRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context);
        }
    }

    public ParallaxRecyclerView(Context context) {
        super(context);
        if (!isInEditMode()) {
            init(context);
        }
    }

    /**
     * 设置层叠高度
     *
     * @param stackHeight
     */
    public void setStackHeight(int stackHeight) {
        this.mStackHeight = stackHeight;
    }

    private void init(final Context context) {
        setLayoutManager(new LinearLayoutManager(context));
        addItemDecoration(new ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildAdapterPosition(view);
                int itemCount = parent.getAdapter().getItemCount();
                if (position != itemCount - 1) {
                    outRect.bottom = -DisplayUtils.convertDIP2PX(context, mStackHeight);
                }
            }
        });
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int firstPosition = layoutManager.findFirstVisibleItemPosition();
                int lastPosition = layoutManager.findLastVisibleItemPosition();
                int visibleCount = lastPosition - firstPosition;
                //重置控件的位置及高度
                int elevation = 1;
                for (int i = firstPosition - 1; i <= (firstPosition + visibleCount) + 1; i++) {
                    View view = layoutManager.findViewByPosition(i);
                    if (view != null) {
                        if (view instanceof CardView) {
                            ((CardView) view).setCardElevation(DisplayUtils.convertDIP2PX(context, elevation));
                            elevation += 5;
                        }
                        float translationY = view.getTranslationY();
                        if (i > firstPosition && translationY != 0) {
                            view.setTranslationY(0);
                        }
                    }
                }

                View firstView = layoutManager.findViewByPosition(firstPosition);
                float firstViewTop = firstView.getTop();
                firstView.setTranslationY(-firstViewTop / 2.0f);
            }
        });
    }
}
