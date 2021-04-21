package com.shijingfeng.widget_collection.widget.item_decoration;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.shijingfeng.widget_collection.R;

/**
 * Function: 网格分隔线 ItemDecoration
 * Date: 2021/4/20 13:20
 * Description:
 *
 * @author ShiJingFeng
 */
public class GridDividerItemDecoration extends RecyclerView.ItemDecoration {

    /** 水平分隔线 ItemDecoration 参数 */
    private DividerItemDecorationParam mHorizontalParam;
    /** 垂直分隔线 ItemDecoration 参数 */
    private DividerItemDecorationParam mVerticalParam;

    /** 水平分隔线 画笔 */
    private final Paint mHorizontalPaint = new Paint();
    /** 垂直分隔线 画笔 */
    private final Paint mVerticalPaint = new Paint();

    public GridDividerItemDecoration(@Nullable DividerItemDecorationParam horizontalParam, @Nullable DividerItemDecorationParam verticalParam) {
        this.mHorizontalParam = horizontalParam;
        this.mVerticalParam = verticalParam;
        init(horizontalParam, verticalParam);
    }

    /**
     * 初始化
     *
     * @param horizontalParam 水平排列 分隔线 (分割线是竖着的) ItemDecoration 参数
     * @param verticalParam 垂直排列 分隔线 (分割线是横着的) ItemDecoration 参数
     */
    private void init(@Nullable DividerItemDecorationParam horizontalParam, @Nullable DividerItemDecorationParam verticalParam) {
        mHorizontalPaint.reset();
        if (horizontalParam != null) {
            mHorizontalPaint.setColor(horizontalParam.dividerColor);
            mHorizontalPaint.setStrokeWidth(horizontalParam.dividerThickness == 0 ? 0F : horizontalParam.dividerThickness);
            mHorizontalPaint.setAntiAlias(true);
        }
        mVerticalPaint.reset();
        if (verticalParam != null) {
            mVerticalPaint.setColor(verticalParam.dividerColor);
            mVerticalPaint.setStrokeWidth(verticalParam.dividerThickness == 0 ? 0F : verticalParam.dividerThickness);
            mVerticalPaint.setAntiAlias(true);
        }
    }

    /**
     * 设置分隔间距
     *
     * @param outRect Rect
     * @param view View
     * @param parent RecyclerView
     * @param state RecyclerView.State
     */
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (parent.getAdapter() == null || parent.getLayoutManager() == null) {
            // 没有设置 适配器 和 LayoutManager 则不设置分隔线
            return;
        }
        if (!(parent.getLayoutManager() instanceof GridLayoutManager) && !(parent.getLayoutManager() instanceof StaggeredGridLayoutManager)) {
            throw new IllegalStateException("只有 GridLayoutManager 或 StaggeredGridLayoutManager 才能使用网格分隔线");
        }

        // 水平分隔线 ItemDecoration 参数
        final DividerItemDecorationParam horizontalParam = this.mVerticalParam;
        // 垂直分隔线 ItemDecoration 参数
        final DividerItemDecorationParam verticalParam = this.mHorizontalParam;

        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        final int itemCount = parent.getAdapter() == null ? 0 : parent.getAdapter().getItemCount();
        final int position = parent.getChildAdapterPosition(view);
        final int orientation;
        final int spanCount;

        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;

            orientation = gridLayoutManager.getOrientation();
            spanCount = gridLayoutManager.getSpanCount();
        } else {
            final StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;

            orientation = staggeredGridLayoutManager.getOrientation();
            spanCount = staggeredGridLayoutManager.getSpanCount();
        }

        // 总行数
        int totalRaw = 0;
        // 总列数
        int totalColumn = 0;
        // 当前Position所属的行数 (从0开始)
        int curRaw = 0;
        // 当前Position所属的列数 (从0开始)
        int curColumn = 0;

        switch (orientation) {
            // 竖向排列
            case RecyclerView.VERTICAL:
                //        0  1  2
                //        3  4  5
                //        6  7  8
                //        9 10 11
                totalColumn = spanCount;
                totalRaw = itemCount % totalColumn == 0 ? itemCount / totalColumn : itemCount/ totalColumn + 1;
                curRaw = position / totalColumn;
                curColumn = position % totalColumn;
                break;
            // 横向排列
            case RecyclerView.HORIZONTAL:
                //        0  3  6  9
                //        1  4  7 10
                //        2  5  8 11
                totalRaw = spanCount;
                totalColumn = itemCount % totalRaw == 0 ? itemCount / totalRaw : itemCount/ totalRaw + 1;
                curRaw = position % totalRaw;
                curColumn = position / totalRaw;
                break;
            default:
                break;
        }

        // Item右边Margin
        final int itemRightMargin;
        // Item底部Margin
        final int itemBottomMargin;

        if (horizontalParam != null && curColumn < totalColumn - 1) {
            // 水平分隔线, 不是最后一列
            final int thickness = horizontalParam.dividerThickness;
            final int leftMargin = horizontalParam.dividerLeftMargin;
            final int rightMargin = horizontalParam.dividerRightMargin;

            itemRightMargin = leftMargin + thickness + rightMargin;
        } else {
            itemRightMargin = 0;
        }
        if (verticalParam != null && curRaw < totalRaw - 1) {
            // 垂直分隔线, 不是最后一行
            final int thickness = verticalParam.dividerThickness;
            final int topMargin = verticalParam.dividerTopMargin;
            final int bottomMargin = verticalParam.dividerBottomMargin;

            itemBottomMargin = topMargin + thickness + bottomMargin;
        } else {
            itemBottomMargin = 0;
        }
        view.setTag(R.id.total_raw, totalRaw);
        view.setTag(R.id.total_column, totalColumn);
        view.setTag(R.id.current_raw, curRaw);
        view.setTag(R.id.current_column, curColumn);

        outRect.set(0, 0, itemRightMargin, itemBottomMargin);
    }

    /**
     * 绘制分隔线
     *
     * @param canvas Canvas
     * @param parent RecyclerView
     * @param state RecyclerView.State
     */
    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(canvas, parent, state);
        // 绘制水平分隔线
        drawHorizontalDividerLine(canvas, parent);
        // 绘制垂直分隔线
        drawVerticalDividerLine(canvas, parent);
    }

    /**
     * 绘制水平分隔线
     *
     * @param canvas Canvas
     * @param parent RecyclerView
     */
    private void drawHorizontalDividerLine(@NonNull Canvas canvas, @NonNull RecyclerView parent) {
        // 水平分隔线 ItemDecoration 参数
        final DividerItemDecorationParam horizontalParam = this.mVerticalParam;
        // 垂直分隔线 ItemDecoration 参数
        final DividerItemDecorationParam verticalParam = this.mHorizontalParam;
        final int childCount = parent.getChildCount();

        // 分隔线X轴起点坐标
        int startX;
        // 分隔线Y轴起点坐标
        int startY;
        // 分隔线X轴终点坐标
        int endX;
        // 分隔线Y轴终点坐标
        int endY;

        // 绘制水平分割线
        if (horizontalParam != null) {
            final int thickness = horizontalParam.dividerThickness;
            final int thicknessHalf = thickness / 2;
            int rightMargin = horizontalParam.dividerRightMargin;
            int topMargin = horizontalParam.dividerTopMargin;
            int bottomMargin = horizontalParam.dividerBottomMargin;

            for (int index = 0; index < childCount; ++index) {
                final View child = parent.getChildAt(index);
                final int totalRaw = (int) child.getTag(R.id.total_raw);
                final int totalColumn = (int) child.getTag(R.id.total_column);
                final int currentRaw = (int) child.getTag(R.id.current_raw);
                final int currentColumn = (int) child.getTag(R.id.current_column);

                if (verticalParam != null) {
                    if (currentRaw == 0) {
                        // 第一行
                        if (bottomMargin == 0) {
                            bottomMargin = -(verticalParam.dividerTopMargin + verticalParam.dividerThickness / 2);
                        }
                    } else if (currentRaw == totalRaw - 1) {
                        // 最后一行
                        if (topMargin == 0) {
                            topMargin = -(verticalParam.dividerBottomMargin + verticalParam.dividerThickness / 2);
                        }
                    } else {
                        if (topMargin == 0) {
                            topMargin = -(verticalParam.dividerBottomMargin + verticalParam.dividerThickness / 2);
                        }
                        if (bottomMargin == 0) {
                            bottomMargin = -(verticalParam.dividerTopMargin + verticalParam.dividerThickness / 2);
                        }
                    }
                }
                if (currentColumn < totalColumn - 1) {
                    startX = child.getRight() + rightMargin + thicknessHalf;
                    startY = child.getTop() + topMargin;
                    endX = child.getRight() + rightMargin + thicknessHalf;
                    endY = child.getBottom() - bottomMargin;
                } else {
                    startX = 0;
                    startY = 0;
                    endX = 0;
                    endY = 0;
                }
                // 绘制分隔线
                canvas.drawLine(startX, startY, endX, endY, mHorizontalPaint);
            }
        }
    }

    /**
     * 绘制垂直分隔线
     *
     * @param canvas Canvas
     * @param parent RecyclerView
     */
    private void drawVerticalDividerLine(@NonNull Canvas canvas, @NonNull RecyclerView parent) {
        // 水平分隔线 ItemDecoration 参数
        final DividerItemDecorationParam horizontalParam = this.mVerticalParam;
        // 垂直分隔线 ItemDecoration 参数
        final DividerItemDecorationParam verticalParam = this.mHorizontalParam;
        final int childCount = parent.getChildCount();

        // 分隔线X轴起点坐标
        int startX;
        // 分隔线Y轴起点坐标
        int startY;
        // 分隔线X轴终点坐标
        int endX;
        // 分隔线Y轴终点坐标
        int endY;

        // 绘制垂直分割线
        if (verticalParam != null) {
            final int thickness = verticalParam.dividerThickness;
            final int thicknessHalf = thickness / 2;
            int leftMargin = verticalParam.dividerLeftMargin;
            int rightMargin = verticalParam.dividerRightMargin;
            int topMargin = verticalParam.dividerTopMargin;

            for (int index = 0; index < childCount; ++index) {
                final View child = parent.getChildAt(index);
                final int totalRaw = (int) child.getTag(R.id.total_raw);
                final int totalColumn = (int) child.getTag(R.id.total_column);
                final int currentRaw = (int) child.getTag(R.id.current_raw);
                final int currentColumn = (int) child.getTag(R.id.current_column);

                if (horizontalParam != null) {
                    if (currentColumn == 0) {
                        // 第一列
                        if (rightMargin == 0) {
                            rightMargin = -(horizontalParam.dividerLeftMargin + horizontalParam.dividerThickness / 2);
                        }
                    } else if (currentColumn == totalColumn - 1) {
                        // 最后一列
                        if (leftMargin == 0) {
                            leftMargin = -(horizontalParam.dividerRightMargin + horizontalParam.dividerThickness / 2);
                        }
                    } else {
                        if (leftMargin == 0) {
                            leftMargin = -(horizontalParam.dividerRightMargin + horizontalParam.dividerThickness / 2);
                        }
                        if (rightMargin == 0) {
                            rightMargin = -(horizontalParam.dividerLeftMargin + horizontalParam.dividerThickness / 2);
                        }
                    }
                }
                if (currentRaw < totalRaw - 1) {
                    startX = child.getLeft() + leftMargin;
                    startY = child.getBottom() + topMargin + thicknessHalf;
                    endX = child.getRight() - rightMargin;
                    endY = child.getBottom() + topMargin + thicknessHalf;
                } else {
                    // 最后一行
                    startX = 0;
                    startY = 0;
                    endX = 0;
                    endY = 0;
                }
                // 绘制分隔线
                canvas.drawLine(startX, startY, endX, endY, mVerticalPaint);
            }
        }
    }

}
