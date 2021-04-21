package com.shijingfeng.widget_collection.widget.item_decoration;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shijingfeng.widget_collection.R;

import java.util.Objects;

/**
 * Function: 线性布局 分隔线
 * Date: 2021/4/20 10:55
 * Description:
 *
 * @author ShiJingFeng
 */
public class LinearDividerItemDecoration extends RecyclerView.ItemDecoration {

    /** 分隔线参数 */
    @NonNull
    private DividerItemDecorationParam mParam;
    /** 画笔 */
    @NonNull
    private Paint mPaint = new Paint();

    public LinearDividerItemDecoration(@NonNull DividerItemDecorationParam param) {
        this.mParam = param;
        init(param);
    }

    /**
     * 初始化
     */
    private void init(@NonNull DividerItemDecorationParam param) {
        mPaint.reset();
        mPaint.setColor(param.dividerColor);
        mPaint.setStrokeWidth(param.dividerThickness == 0 ? 0F : param.dividerThickness);
        mPaint.setAntiAlias(true);
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
        if (!(parent.getLayoutManager() instanceof LinearLayoutManager)) {
            throw new IllegalStateException("只有LinearLayoutManager才能使用线性分隔线");
        }

        final DividerItemDecorationParam param = this.mParam;
        final int thickness = param.dividerThickness;
        final int leftMargin = param.dividerLeftMargin;
        final int rightMargin = param.dividerRightMargin;
        final int topMargin = param.dividerTopMargin;
        final int bottomMargin = param.dividerBottomMargin;

        final LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
        // LinearLayoutManager方向
        final int orientation = layoutManager.getOrientation();
        // Item数量
        final int itemCount = parent.getAdapter().getItemCount();
        // 当前Item下标
        final int position = parent.getChildAdapterPosition(view);

        // Item右边Margin
        int itemRightMargin = 0;
        // Item底部Margin
        int itemBottomMargin = 0;

        switch (orientation) {
            // 竖向排列
            case RecyclerView.VERTICAL:
                if (position < itemCount - 1) {
                    // 不是最后一项
                    itemBottomMargin = topMargin + thickness + bottomMargin;
                }
                break;
            // 横向排列
            case RecyclerView.HORIZONTAL:
                if (position < itemCount - 1) {
                    // 不是最后一项
                    itemRightMargin = leftMargin + thickness + rightMargin;
                }
                break;
            default:
                break;
        }
        view.setTag(R.id.current_position, position);

        // 设置分隔间距
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
        final DividerItemDecorationParam param = this.mParam;
        final int thicknessHalf = param.dividerThickness / 2;
        final int leftMargin = param.dividerLeftMargin;
        final int rightMargin = param.dividerRightMargin;
        final int topMargin = param.dividerTopMargin;

        final LinearLayoutManager layoutManager = Objects.requireNonNull((LinearLayoutManager) parent.getLayoutManager());
        final int orientation = layoutManager.getOrientation();
        final int itemCount = parent.getAdapter() == null ? 0 : parent.getAdapter().getItemCount();
        final int childCount = parent.getChildCount();

        // 分隔线X轴起点坐标
        int startX;
        // 分隔线Y轴起点坐标
        int startY;
        // 分隔线X轴终点坐标
        int endX;
        // 分隔线Y轴终点坐标
        int endY;

        switch (orientation) {
            // 竖向排列
            case RecyclerView.VERTICAL:
                for (int index = 0; index < childCount; ++index) {
                    final View child = parent.getChildAt(index);
                    final int currentPosition = (int) child.getTag(R.id.current_position);

                    if (currentPosition < itemCount - 1) {
                        // 不是最后一行，则绘制分隔线
                        startX = child.getLeft() + leftMargin;
                        startY = child.getBottom() + topMargin + thicknessHalf;
                        endX = child.getRight() - rightMargin;
                        endY = child.getBottom() + topMargin + thicknessHalf;
                        // 绘制分隔线
                        canvas.drawLine(startX, startY, endX, endY, mPaint);
                    }
                }
                break;
            // 横向排列
            case RecyclerView.HORIZONTAL:
                for (int index = 0; index < childCount; ++index) {
                    final View child = parent.getChildAt(index);
                    final int currentPosition = (int) child.getTag(R.id.current_position);

                    if (currentPosition < itemCount - 1) {
                        // 不是最后一列，则绘制分隔线
                        startX = child.getRight() + rightMargin + thicknessHalf;
                        startY = child.getTop();
                        endX = child.getRight() + rightMargin + thicknessHalf;
                        endY = child.getBottom();
                        // 绘制分隔线
                        canvas.drawLine(startX, startY, endX, endY, mPaint);
                    }
                }
                break;
            default:
                break;
        }
    }
}
