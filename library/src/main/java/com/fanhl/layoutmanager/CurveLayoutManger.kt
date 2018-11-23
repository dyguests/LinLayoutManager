package com.fanhl.layoutmanager

import android.graphics.Rect
import android.support.annotation.IntDef
import android.support.v7.widget.RecyclerView
import android.util.SparseBooleanArray

/**
 * 自定义曲线LayoutManager
 *
 * @author fanhl
 */
class CurveLayoutManger : RecyclerView.LayoutManager() {
    private var horizontalScrollOffset = 0
    private var verticalScrollOffset = 0

    private var totalWidth = 0

    //记录Item是否出现过屏幕且还没有回收。true表示出现过屏幕上，并且还没被回收
    private val hasAttachedItems = SparseBooleanArray()

    var curve: Curve = Parabola()

    /**
     *  临时存放rect
     *  用于参与 recycleAndFillItems 中的计算
     */
    private val childFrame = Rect()

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        //如果没有item，直接返回
        if (itemCount <= 0) return
        // 跳过preLayout，preLayout主要用于支持动画
        if (state.isPreLayout) {
            return
        }
        //在布局之前，将所有的子View先Detach掉，放入到Scrap缓存中
        detachAndScrapAttachedViews(recycler)

        totalWidth = 0
        for (i in 0 until itemCount) {
            val view = recycler.getViewForPosition(i)

            addView(view)

            measureChildWithMargins(view, 0, 0)

            val width = getDecoratedMeasuredWidth(view)
            val height = getDecoratedMeasuredHeight(view)

            totalWidth += width

            hasAttachedItems.put(i, false)
        }

        //如果所有子View的高度和没有填满RecyclerView的高度，
        // 则将高度设置为RecyclerView的高度
        totalWidth = Math.max(totalWidth, getHorizontalSpace())

        recycleAndFillItems(recycler, state)
    }

    override fun canScrollHorizontally(): Boolean {
        return curve.canScrollHorizontally()
    }

    override fun canScrollVertically(): Boolean {
        return curve.canScrollVertically()
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        //先detach掉所有的子View
        detachAndScrapAttachedViews(recycler)

        var travel = dx

        if (horizontalScrollOffset + dx < 0) {
            travel = -horizontalScrollOffset
        } else if (horizontalScrollOffset + dx > totalWidth - getHorizontalSpace()) {
            travel = totalWidth - getHorizontalSpace() - horizontalScrollOffset
        }

        //将水平方向的偏移量+travel
        horizontalScrollOffset += travel

        // 平移容器内的item
        offsetChildrenHorizontal(-travel)

        recycleAndFillItems(recycler, state)
        return travel
    }

    private fun recycleAndFillItems(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        // 跳过preLayout，preLayout主要用于支持动画
        if (state.isPreLayout) {
            return
        }

        // 当前scroll offset状态下的显示区域
        val displayFrame = Rect(horizontalScrollOffset, verticalScrollOffset, horizontalScrollOffset + getHorizontalSpace(), verticalScrollOffset + getVerticalSpace())

        /*
         * 将滑出屏幕的Items回收到Recycle缓存中
         */
        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            childFrame.left = getDecoratedLeft(child)
            childFrame.top = getDecoratedTop(child)
            childFrame.right = getDecoratedRight(child)
            childFrame.bottom = getDecoratedBottom(child)
            //如果Item没有在显示区域，就说明需要回收
            if (!Rect.intersects(displayFrame, childFrame)) {
                //回收掉滑出屏幕的View
                removeAndRecycleView(child, recycler)
            }
        }

        //重新显示需要出现在屏幕的子View
        for (i in 0 until itemCount) {
            // 获取对应view
            val scrap = recycler.getViewForPosition(i)
            // 对应view的显示尺寸
            val width = getDecoratedMeasuredWidth(scrap)
            val height = getDecoratedMeasuredHeight(scrap)

            //对应view的布局位置
            curve.getFrame(displayFrame, horizontalScrollOffset, verticalScrollOffset, i, width, height, childFrame)

            if (Rect.intersects(displayFrame, childFrame)) {
                measureChildWithMargins(scrap, 0, 0)

                addView(scrap)

                //将这个item布局出来
                layoutDecorated(
                    scrap,
                    childFrame.left - horizontalScrollOffset,
                    childFrame.top,
                    childFrame.right - horizontalScrollOffset,
                    childFrame.bottom
                )
            }
        }
    }

    private fun getHorizontalSpace(): Int {
        return width - paddingLeft - paddingRight
    }

    private fun getVerticalSpace(): Int {
        return height - paddingBottom - paddingTop
    }

    companion object {

        const val HORIZONTAL = 0
        const val VERTICAL = 1
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(HORIZONTAL, VERTICAL)
    annotation class Orientation

    /** layout用的曲线 */
    interface Curve {
        @Orientation
        fun getScrollOrientation(): Int

        fun canScrollHorizontally() = getScrollOrientation() == HORIZONTAL
        fun canScrollVertically() = getScrollOrientation() == VERTICAL
        /**
         * @param displayFrame 屏幕显示区域
         * @param position 当前元素的位置(in itemCount)，当前child的宽
         * @param width 当前元素宽
         * @param height 当前元素高
         * @param horizontalScrollOffset 当前屏幕水平偏移
         * @param verticalScrollOffset 当前屏幕垂直偏移
         */
        fun getFrame(
            displayFrame: Rect,
            horizontalScrollOffset: Int,
            verticalScrollOffset: Int,
            position: Int,
            width: Int,
            height: Int,
            frame: Rect
        )
    }

    /** 抛物线 */
    class Parabola : Curve {
        override fun getScrollOrientation() = HORIZONTAL

        override fun getFrame(
            displayFrame: Rect,
            horizontalScrollOffset: Int, verticalScrollOffset: Int,
            position: Int,
            width: Int, height: Int,
            frame: Rect
        ) {
            frame.apply {
                left = position * width
                top = 0
                right = (position + 1) * width
                bottom = height
            }
        }
    }
}