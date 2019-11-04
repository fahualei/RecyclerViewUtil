package com.lei.recyclerview.divider;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * Created by lei on 2016/12/08.
 */
public class ItemDividerDecoration extends RecyclerView.ItemDecoration {

    private static final int DEFAULT_SIZE = 2;
    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };

    private enum DividerType {
        DRAWABLE, PAINT, COLOR
    }

    private DividerType mDividerType = DividerType.DRAWABLE;
    private VisibilityProvider mVisibilityProvider;
    private PaintProvider mPaintProvider;
    private ColorProvider mColorProvider;
    private DrawableProvider mDrawableProvider;
    private SizeProvider mSizeProvider;
    private MarginProvider mMarginProvider;
    private boolean mShowLastDivider;
    private boolean mPositionInsideItem;
    private Paint mPaint;
    private boolean isFullGridView = false;

    protected ItemDividerDecoration(Builder builder) {
        if (builder.mPaintProvider != null) {
            mDividerType = DividerType.PAINT;
            mPaintProvider = builder.mPaintProvider;
        } else if (builder.mColorProvider != null) {
            mDividerType = DividerType.COLOR;
            mColorProvider = builder.mColorProvider;
            mPaint = new Paint();
            setSizeProvider(builder);
        } else {
            mDividerType = DividerType.DRAWABLE;
            if (builder.mDrawableProvider == null) {
                TypedArray a = builder.mContext.obtainStyledAttributes(ATTRS);
                final Drawable divider = a.getDrawable(0);
                a.recycle();
                mDrawableProvider = new DrawableProvider() {
                    @Override
                    public Drawable drawableProvider(int position, RecyclerView parent) {
                        return divider;
                    }
                };
            } else {
                mDrawableProvider = builder.mDrawableProvider;
            }
            mSizeProvider = builder.mSizeProvider;
        }

        mVisibilityProvider = builder.mVisibilityProvider;
        mShowLastDivider = builder.mShowLastDivider;
        mPositionInsideItem = builder.mPositionInsideItem;
        mMarginProvider = builder.mMarginProvider;
    }

    private void setSizeProvider(Builder builder) {
        mSizeProvider = builder.mSizeProvider;
        if (mSizeProvider == null) {
            mSizeProvider = new SizeProvider() {
                @Override
                public int dividerSize(int position, RecyclerView parent) {
                    return DEFAULT_SIZE;
                }
            };
        }
    }

    /**
     * 计算网格布局是否是满屏, 在每次更新适配器数据时调用
     *
     * @param parent
     */
    public void computeGridViewIsFull(RecyclerView parent) {
        isFullGridView = true;
        if (parent.getAdapter() == null) {
            return;
        }
        if (parent.getLayoutManager() instanceof GridLayoutManager) {
            GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
            GridLayoutManager.SpanSizeLookup spanSizeLookup = layoutManager.getSpanSizeLookup();
            int spanCount = layoutManager.getSpanCount();
            int itemCount = parent.getAdapter().getItemCount();
            int lastRowFirstItem = 0;
            int rank = 0;
            for (int i = itemCount - 1; i >= 0; i--) {
                if (spanSizeLookup.getSpanIndex(i, spanCount) == 0) {
                    lastRowFirstItem = i;
                    break;
                }
            }
            for (int i = lastRowFirstItem; i < itemCount; i++) {
                rank += spanSizeLookup.getSpanSize(i);
            }
            if (rank != spanCount) {
                isFullGridView = false;
            }
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter == null || parent.getLayoutManager() == null) {
            return;
        }
        if (parent.getLayoutManager() instanceof GridLayoutManager) {
            drawGridViewDivider(c, parent);
        } else if (parent.getLayoutManager() instanceof LinearLayoutManager) {
            drawLinearDivider(c, parent, adapter);
        }
    }

    private int getDividerSize(int position, RecyclerView parent) {
        if (mPaintProvider != null) {
            return (int) mPaintProvider.dividerPaint(position, parent).getStrokeWidth();
        } else if (mSizeProvider != null) {
            return mSizeProvider.dividerSize(position, parent);
        } else if (mDrawableProvider != null) {
            Drawable drawable = mDrawableProvider.drawableProvider(position, parent);
            return drawable.getIntrinsicWidth();
        }
        throw new RuntimeException("failed to get size");
    }

    private Rect getVerticalDividerBound(int position, RecyclerView parent, View child) {
        Rect bounds = new Rect(0, 0, 0, 0);
        int transitionX = (int) ViewCompat.getTranslationX(child);
        int transitionY = (int) ViewCompat.getTranslationY(child);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        bounds.top = parent.getPaddingTop() +
                mMarginProvider.dividerTopMargin(position, parent) + transitionY;
        bounds.bottom = parent.getHeight() - parent.getPaddingBottom() -
                mMarginProvider.dividerBottomMargin(position, parent) + transitionY;

        int dividerSize = getDividerSize(position, parent);
        boolean isReverseLayout = isReverseLayout(parent);
        if (mDividerType == DividerType.DRAWABLE) {
            // set left and right position of divider
            if (isReverseLayout) {
                bounds.right = child.getLeft() - params.leftMargin + transitionX;
                bounds.left = bounds.right - dividerSize;
            } else {
                bounds.left = child.getRight() + params.rightMargin + transitionX;
                bounds.right = bounds.left + dividerSize;
            }
        } else {
            // set center point of divider
            int halfSize = dividerSize / 2;
            if (isReverseLayout) {
                bounds.left = child.getLeft() - params.leftMargin - halfSize + transitionX;
            } else {
                bounds.left = child.getRight() + params.rightMargin + halfSize + transitionX;
            }
            bounds.right = bounds.left;
        }

        if (mPositionInsideItem) {
            if (isReverseLayout) {
                bounds.left += dividerSize;
                bounds.right += dividerSize;
            } else {
                bounds.left -= dividerSize;
                bounds.right -= dividerSize;
            }
        }

        return bounds;
    }

    private Rect getHorizontalDividerBound(int position, RecyclerView parent, View child) {
        Rect bounds = new Rect(0, 0, 0, 0);
        int transitionX = (int) ViewCompat.getTranslationX(child);
        int transitionY = (int) ViewCompat.getTranslationY(child);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        bounds.left = parent.getPaddingLeft() +
                mMarginProvider.dividerLeftMargin(position, parent) + transitionX;
        bounds.right = parent.getWidth() - parent.getPaddingRight() -
                mMarginProvider.dividerRightMargin(position, parent) + transitionX;

        int dividerSize = getDividerSize(position, parent);
        boolean isReverseLayout = isReverseLayout(parent);
        if (mDividerType == DividerType.DRAWABLE) {
            // set top and bottom position of divider
            if (isReverseLayout) {
                bounds.bottom = child.getTop() - params.topMargin + transitionY;
                bounds.top = bounds.bottom - dividerSize;
            } else {
                bounds.top = child.getBottom() + params.bottomMargin + transitionY;
                bounds.bottom = bounds.top + dividerSize;
            }
        } else {
            // set center point of divider
            int halfSize = dividerSize / 2;
            if (isReverseLayout) {
                bounds.top = child.getTop() - params.topMargin - halfSize + transitionY;
            } else {
                bounds.top = child.getBottom() + params.bottomMargin + halfSize + transitionY;
            }
            bounds.bottom = bounds.top;
        }

        if (mPositionInsideItem) {
            if (isReverseLayout) {
                bounds.top += dividerSize;
                bounds.bottom += dividerSize;
            } else {
                bounds.top -= dividerSize;
                bounds.bottom -= dividerSize;
            }
        }

        return bounds;
    }

    private void drawLinearDivider(Canvas c, RecyclerView parent, RecyclerView.Adapter adapter) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
        int itemCount = adapter.getItemCount();
        int validChildCount = parent.getChildCount();
        int lastChildPosition = -1;
        for (int i = 0; i < validChildCount; i++) {
            View child = parent.getChildAt(i);
            int childPosition = parent.getChildAdapterPosition(child);

            if (childPosition < lastChildPosition) {
                // Avoid remaining divider when animation starts
                continue;
            }
            lastChildPosition = childPosition;

            if (!mShowLastDivider && childPosition >= itemCount - 1) {
                // Don't draw divider for last line if mShowLastDivider = false
                continue;
            }

            if (mVisibilityProvider.shouldHideDivider(childPosition, parent)) {
                continue;
            }

            Rect bounds;

            if (layoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {
                bounds = getHorizontalDividerBound(childPosition, parent, child);
            } else {
                bounds = getVerticalDividerBound(childPosition, parent, child);
            }

            switch (mDividerType) {
                case DRAWABLE:
                    Drawable drawable = mDrawableProvider.drawableProvider(childPosition, parent);
                    drawable.setBounds(bounds);
                    drawable.draw(c);
                    break;
                case PAINT:
                    mPaint = mPaintProvider.dividerPaint(childPosition, parent);
                    c.drawLine(bounds.left, bounds.top, bounds.right, bounds.bottom, mPaint);
                    break;
                case COLOR:
                    mPaint.setColor(mColorProvider.dividerColor(childPosition, parent));
                    mPaint.setStrokeWidth(mSizeProvider.dividerSize(childPosition, parent));
                    c.drawLine(bounds.left, bounds.top, bounds.right, bounds.bottom, mPaint);
                    break;
            }
        }
    }

    private void drawGridViewDivider(Canvas c, RecyclerView parent) {
        int validChildCount = parent.getChildCount();
        int lastChildPosition = -1;
        for (int i = 0; i < validChildCount; i++) {
            View child = parent.getChildAt(i);
            int childPosition = parent.getChildAdapterPosition(child);

            if (childPosition < lastChildPosition) {
                // Avoid remaining divider when animation starts
                continue;
            }
            lastChildPosition = childPosition;

            int groupIndex = getGroupIndex(childPosition, parent);
            if (mVisibilityProvider.shouldHideDivider(groupIndex, parent)) {
                continue;
            }

            //画Item项下横线
            if (!isHLastDividerOffset(parent, childPosition)) {
                Rect horizontalDividerBounds = getHorizontalDividerBound(groupIndex, parent, child);
                switch (mDividerType) {
                    case DRAWABLE:
                        Drawable drawable = mDrawableProvider.drawableProvider(groupIndex, parent);
                        drawable.setBounds(horizontalDividerBounds);
                        drawable.draw(c);
                        break;
                    case PAINT:
                        mPaint = mPaintProvider.dividerPaint(groupIndex, parent);
                        c.drawLine(horizontalDividerBounds.left, horizontalDividerBounds.top, horizontalDividerBounds
                                .right, horizontalDividerBounds.bottom, mPaint);
                        break;
                    case COLOR:
                        mPaint.setColor(mColorProvider.dividerColor(groupIndex, parent));
                        mPaint.setStrokeWidth(mSizeProvider.dividerSize(groupIndex, parent));
                        c.drawLine(horizontalDividerBounds.left, horizontalDividerBounds.top, horizontalDividerBounds
                                .right, horizontalDividerBounds.bottom, mPaint);
                        break;
                }
            }
            //画Item项右竖线
            if (!isVLastDividerOffset(parent, childPosition)) {
                Rect verticalDividerBounds = getVerticalDividerBound(groupIndex, parent, child);
                switch (mDividerType) {
                    case DRAWABLE:
                        Drawable drawable = mDrawableProvider.drawableProvider(groupIndex, parent);
                        drawable.setBounds(verticalDividerBounds);
                        drawable.draw(c);
                        break;
                    case PAINT:
                        mPaint = mPaintProvider.dividerPaint(groupIndex, parent);
                        c.drawLine(verticalDividerBounds.left, verticalDividerBounds.top, verticalDividerBounds
                                .right, verticalDividerBounds.bottom, mPaint);
                        break;
                    case COLOR:
                        mPaint.setColor(mColorProvider.dividerColor(groupIndex, parent));
                        mPaint.setStrokeWidth(mSizeProvider.dividerSize(groupIndex, parent));
                        c.drawLine(verticalDividerBounds.left, verticalDividerBounds.top, verticalDividerBounds
                                .right, verticalDividerBounds.bottom, mPaint);
                        break;
                }
            }
        }
    }

    @Override
    public void getItemOffsets(Rect rect, View v, RecyclerView parent, RecyclerView.State state) {
        if (mPositionInsideItem) {
            rect.set(0, 0, 0, 0);
            return;
        }
        int position = parent.getChildAdapterPosition(v);
        int itemCount = parent.getAdapter().getItemCount();
        if (parent.getLayoutManager() != null) {
            if (parent.getLayoutManager() instanceof GridLayoutManager) {
                int groupIndex = getGroupIndex(position, parent);

                if (mVisibilityProvider.shouldHideDivider(groupIndex, parent)) {
                    return;
                }
                boolean isVLastDivider = isVLastDividerOffset(parent, position);
                boolean isHLastDivider = isHLastDividerOffset(parent, position);
                if (!isVLastDivider && !isHLastDivider) {
                    rect.set(0, 0, getDividerSize(position, parent), getDividerSize(position, parent));
                } else if (isVLastDivider && !isHLastDivider) {
                    rect.set(0, 0, 0, getDividerSize(position, parent));
                } else if (!isVLastDivider && isHLastDivider) {
                    rect.set(0, 0, getDividerSize(position, parent), 0);
                }
            } else if (parent.getLayoutManager() instanceof LinearLayoutManager) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
                if (!mShowLastDivider && position >= itemCount - 1) {
                    // Don't set item offset for last line if mShowLastDivider = false
                    return;
                }

                if (mVisibilityProvider.shouldHideDivider(position, parent)) {
                    return;
                }
                if (layoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {
                    if (isReverseLayout(parent)) {
                        rect.set(0, getDividerSize(position, parent), 0, 0);
                    } else {
                        rect.set(0, 0, 0, getDividerSize(position, parent));
                    }
                } else {
                    if (isReverseLayout(parent)) {
                        rect.set(getDividerSize(position, parent), 0, 0, 0);
                    } else {
                        rect.set(0, 0, getDividerSize(position, parent), 0);
                    }
                }
            }
        }
    }

    /**
     * Check if recyclerview is reverse layout
     *
     * @param parent RecyclerView
     * @return true if recyclerview is reverse layout
     */
    protected boolean isReverseLayout(RecyclerView parent) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).getReverseLayout();
        } else {
            return false;
        }
    }

    /**
     * 判断该项是否在最后一行
     *
     * @param parent
     * @param childPosition
     * @return
     */
    private boolean isHLastDividerOffset(RecyclerView parent, int childPosition) {
        GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
        GridLayoutManager.SpanSizeLookup spanSizeLookup = layoutManager.getSpanSizeLookup();
        int spanCount = layoutManager.getSpanCount();
        int itemCount = parent.getAdapter().getItemCount();
        int lastRow = spanSizeLookup.getSpanGroupIndex(itemCount - 1, spanCount);
        int row = spanSizeLookup.getSpanGroupIndex(childPosition, spanCount);
        if (row == lastRow) {
            return true;
        }
        return false;
    }

    /**
     * 判断该项是否在最后一列
     *
     * @param parent
     * @param childPosition
     * @return
     */
    private boolean isVLastDividerOffset(RecyclerView parent, int childPosition) {
        GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
        GridLayoutManager.SpanSizeLookup spanSizeLookup = layoutManager.getSpanSizeLookup();
        int spanCount = layoutManager.getSpanCount();
        int itemCount = parent.getAdapter().getItemCount();
        int row = spanSizeLookup.getSpanGroupIndex(childPosition, spanCount);
        int nextChildPos = childPosition + 1 < itemCount ? childPosition + 1 : childPosition;
        int nextRow = spanSizeLookup.getSpanGroupIndex(nextChildPos, spanCount);
        if (nextRow > row) {
            return true;
        }
        if (childPosition == itemCount - 1 && isFullGridView) {//列表最后一项根据是否满屏GridView单独处理
            return true;
        }
        return false;
    }

    /**
     * Returns a group index for GridLayoutManager.
     * for LinearLayoutManager, always returns position.
     *
     * @param position current view position to draw divider
     * @param parent   RecyclerView
     * @return group index of items
     */
    private int getGroupIndex(int position, RecyclerView parent) {
        if (parent.getLayoutManager() instanceof GridLayoutManager) {
            GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
            GridLayoutManager.SpanSizeLookup spanSizeLookup = layoutManager.getSpanSizeLookup();
            int spanCount = layoutManager.getSpanCount();
            return spanSizeLookup.getSpanGroupIndex(position, spanCount);
        }

        return position;
    }

    /**
     * Interface for controlling divider visibility
     */
    public interface VisibilityProvider {

        /**
         * Returns true if divider should be hidden.
         *
         * @param position Divider position (or group index for GridLayoutManager)
         * @param parent   RecyclerView
         * @return True if the divider at position should be hidden
         */
        boolean shouldHideDivider(int position, RecyclerView parent);
    }

    /**
     * Interface for controlling paint instance for divider drawing
     */
    public interface PaintProvider {

        /**
         * Returns {@link Paint} for divider
         *
         * @param position Divider position (or group index for GridLayoutManager)
         * @param parent   RecyclerView
         * @return Paint instance
         */
        Paint dividerPaint(int position, RecyclerView parent);
    }

    /**
     * Interface for controlling divider color
     */
    public interface ColorProvider {

        /**
         * Returns {@link android.graphics.Color} value of divider
         *
         * @param position Divider position (or group index for GridLayoutManager)
         * @param parent   RecyclerView
         * @return Color value
         */
        int dividerColor(int position, RecyclerView parent);
    }

    /**
     * Interface for controlling drawable object for divider drawing
     */
    public interface DrawableProvider {

        /**
         * Returns drawable instance for divider
         *
         * @param position Divider position (or group index for GridLayoutManager)
         * @param parent   RecyclerView
         * @return Drawable instance
         */
        Drawable drawableProvider(int position, RecyclerView parent);
    }

    /**
     * Interface for controlling divider size
     */
    public interface SizeProvider {

        /**
         * Returns size value of divider.
         * Height for horizontal divider, width for vertical divider
         *
         * @param position Divider position (or group index for GridLayoutManager)
         * @param parent   RecyclerView
         * @return Size of divider
         */
        int dividerSize(int position, RecyclerView parent);
    }

    /**
     * Interface for controlling divider margin
     */
    public interface MarginProvider {

        /**
         * Returns top margin of divider.
         *
         * @param position Divider position (or group index for GridLayoutManager)
         * @param parent   RecyclerView
         * @return top margin
         */
        int dividerTopMargin(int position, RecyclerView parent);

        /**
         * Returns bottom margin of divider.
         *
         * @param position Divider position (or group index for GridLayoutManager)
         * @param parent   RecyclerView
         * @return bottom margin
         */
        int dividerBottomMargin(int position, RecyclerView parent);

        /**
         * Returns left margin of divider.
         *
         * @param position Divider position (or group index for GridLayoutManager)
         * @param parent   RecyclerView
         * @return left margin
         */
        int dividerLeftMargin(int position, RecyclerView parent);

        /**
         * Returns right margin of divider.
         *
         * @param position Divider position (or group index for GridLayoutManager)
         * @param parent   RecyclerView
         * @return right margin
         */
        int dividerRightMargin(int position, RecyclerView parent);
    }

    public static class Builder {

        private Context mContext;
        private Resources mResources;
        private PaintProvider mPaintProvider;
        private ColorProvider mColorProvider;
        private DrawableProvider mDrawableProvider;
        private SizeProvider mSizeProvider;
        private VisibilityProvider mVisibilityProvider = new VisibilityProvider() {
            @Override
            public boolean shouldHideDivider(int position, RecyclerView parent) {
                return false;
            }
        };
        private boolean mShowLastDivider = false;
        private boolean mPositionInsideItem = false;

        private MarginProvider mMarginProvider = new MarginProvider() {

            @Override
            public int dividerTopMargin(int position, RecyclerView parent) {
                return 0;
            }

            @Override
            public int dividerBottomMargin(int position, RecyclerView parent) {
                return 0;
            }

            @Override
            public int dividerLeftMargin(int position, RecyclerView parent) {
                return 0;
            }

            @Override
            public int dividerRightMargin(int position, RecyclerView parent) {
                return 0;
            }
        };

        public Builder(Context context) {
            mContext = context;
            mResources = context.getResources();
        }

        public Builder paint(final Paint paint) {
            return paintProvider(new PaintProvider() {
                @Override
                public Paint dividerPaint(int position, RecyclerView parent) {
                    return paint;
                }
            });
        }

        public Builder paintProvider(PaintProvider provider) {
            mPaintProvider = provider;
            return this;
        }

        public Builder color(final int color) {
            return colorProvider(new ColorProvider() {
                @Override
                public int dividerColor(int position, RecyclerView parent) {
                    return color;
                }
            });
        }

        public Builder colorResId(@ColorRes int colorId) {
            return color(ContextCompat.getColor(mContext, colorId));
        }

        public Builder colorProvider(ColorProvider provider) {
            mColorProvider = provider;
            return this;
        }

        public Builder drawable(@DrawableRes int id) {
            return drawable(ContextCompat.getDrawable(mContext, id));
        }

        public Builder drawable(final Drawable drawable) {
            return drawableProvider(new DrawableProvider() {
                @Override
                public Drawable drawableProvider(int position, RecyclerView parent) {
                    return drawable;
                }
            });
        }

        public Builder drawableProvider(DrawableProvider provider) {
            mDrawableProvider = provider;
            return this;
        }

        public Builder size(final int size) {
            return sizeProvider(new SizeProvider() {
                @Override
                public int dividerSize(int position, RecyclerView parent) {
                    return size;
                }
            });
        }

        public Builder sizeResId(@DimenRes int sizeId) {
            return size(mResources.getDimensionPixelSize(sizeId));
        }

        public Builder sizeProvider(SizeProvider provider) {
            mSizeProvider = provider;
            return this;
        }


        public Builder margin(final int leftMargin, final int rightMargin, final int topMargin, final int
                bottomMargin) {
            return marginProvider(new MarginProvider() {
                @Override
                public int dividerTopMargin(int position, RecyclerView parent) {
                    return topMargin;
                }

                @Override
                public int dividerBottomMargin(int position, RecyclerView parent) {
                    return bottomMargin;
                }

                @Override
                public int dividerLeftMargin(int position, RecyclerView parent) {
                    return leftMargin;
                }

                @Override
                public int dividerRightMargin(int position, RecyclerView parent) {
                    return rightMargin;
                }
            });
        }

        public Builder margin(int margin) {
            return margin(margin, margin, margin, margin);
        }

        public Builder marginResId(@DimenRes int leftMarginId, @DimenRes int rightMarginId, @DimenRes int
                topMarginId, @DimenRes int bottomMarginId) {
            return margin(mResources.getDimensionPixelSize(leftMarginId), mResources.getDimensionPixelSize
                    (rightMarginId), mResources.getDimensionPixelSize(topMarginId), mResources.getDimensionPixelSize
                    (bottomMarginId));
        }

        public Builder marginResId(@DimenRes int marginId) {
            return marginResId(marginId, marginId, marginId, marginId);
        }

        public Builder marginProvider(MarginProvider provider) {
            mMarginProvider = provider;
            return this;
        }

        public Builder visibilityProvider(VisibilityProvider provider) {
            mVisibilityProvider = provider;
            return this;
        }

        public Builder showLastDivider() {
            mShowLastDivider = true;
            return this;
        }

        public Builder positionInsideItem(boolean positionInsideItem) {
            mPositionInsideItem = positionInsideItem;
            return this;
        }

        public ItemDividerDecoration build() {
            checkBuilderParams();
            return new ItemDividerDecoration(this);
        }

        private void checkBuilderParams() {
            if (mPaintProvider != null) {
                if (mColorProvider != null) {
                    throw new IllegalArgumentException(
                            "Use setColor method of Paint class to specify line color. Do not provider ColorProvider " +
                                    "if you set PaintProvider.");
                }
                if (mSizeProvider != null) {
                    throw new IllegalArgumentException(
                            "Use setStrokeWidth method of Paint class to specify line size. Do not provider " +
                                    "SizeProvider if you set PaintProvider.");
                }
            }
        }
    }
}
