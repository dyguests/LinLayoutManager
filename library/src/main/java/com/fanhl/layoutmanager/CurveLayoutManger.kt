package com.fanhl.layoutmanager

import android.graphics.Rect
import android.support.annotation.IntDef
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.SparseArray
import android.util.SparseBooleanArray
import com.fanhl.layoutmanager.curve.Parabola

/**
 * 自定义曲线LayoutManager
 *
 * @author fanhl
 */
class CurveLayoutManger(
    var curve: Curve = Parabola()
) : RecyclerView.LayoutManager() {
    private var horizontalScrollOffset = 0
    private var verticalScrollOffset = 0

    private var totalWidth = 0
    private var totalHeight = 0

    //保存所有的Item的尺寸
    private val allItemSize = SparseArray<Size>()
    //记录Item是否出现过屏幕且还没有回收。true表示出现过屏幕上，并且还没被回收
    private val hasAttachedItems = SparseBooleanArray()

    /**
     *  临时存放rect
     *  用于参与 recycleAndFillItems 中的计算
     */
    private val childFrame = Rect()
    /**
     *  临时存放坐标
     *
     * 用于参考坐标计算
     */
    private val vector2 = Vector2()

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
        totalHeight = 0
        for (i in 0 until itemCount) {
            val view = recycler.getViewForPosition(i)

            addView(view)

            measureChildWithMargins(view, 0, 0)

            val width = getDecoratedMeasuredWidth(view)
            val height = getDecoratedMeasuredHeight(view)

            totalWidth += width
            totalHeight += height

            //缓存每个item的尺寸
            allItemSize[i]?.apply {
                this.width = width
                this.height = height
            } ?: Size(width, height).also {
                allItemSize.put(i, it)
            }

            hasAttachedItems.put(i, false)
        }

        //如果所有子View的高度和没有填满RecyclerView的高度，
        // 则将高度设置为RecyclerView的高度
        totalWidth = Math.max(totalWidth, getHorizontalSpace())
        totalHeight = Math.max(totalHeight, getVerticalSpace())

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
//        } else if (horizontalScrollOffset + dx > totalWidth - getHorizontalSpace()) {
//            travel = totalWidth - getHorizontalSpace() - horizontalScrollOffset
//        }
        }
        // FIXME: 2018/11/24 fanhl 另外还需要一种scroll方式用于贴边

        //这种scroll的区域中 居中，第一个元素可以居中，最后一个元素也可以居中
        else {
            //这个值是不能滚动的区域宽度。比如第一个元素的左半部分，和最后一个元素的右边部分
            val notScrollWidth = if (allItemSize.size() > 0) {
                (allItemSize.get(0).width.toFloat() / 2 + allItemSize[itemCount - 1].width.toFloat() / 2).toInt()
            } else {
                0
            }
            if (horizontalScrollOffset + dx > (totalWidth - notScrollWidth)) {
                travel = (totalWidth - notScrollWidth - horizontalScrollOffset)
            }
        }

        //将水平方向的偏移量+travel
        horizontalScrollOffset += travel

        // 平移容器内的item
        offsetChildrenHorizontal(-travel)

        recycleAndFillItems(recycler, state)
        return travel
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        //先detach掉所有的子View
        detachAndScrapAttachedViews(recycler)

        var travel = dy

        if (verticalScrollOffset + dy < 0) {
            travel = -verticalScrollOffset
        } else if (verticalScrollOffset + dy > totalHeight - getVerticalSpace()) {
            travel = totalHeight - getVerticalSpace() - verticalScrollOffset
        }

        //将水平方向的偏移量+travel
        verticalScrollOffset += travel

        // 平移容器内的item
        offsetChildrenVertical(-travel)

        recycleAndFillItems(recycler, state)
        return travel
    }

    private fun recycleAndFillItems(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        // 跳过preLayout，preLayout主要用于支持动画
        if (state.isPreLayout) {
            return
        }

        // 当前scroll offset状态下的显示区域
        val displayFrame = Rect(0, 0, getHorizontalSpace(), getVerticalSpace())

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
        log("recycleAndFillItems: before for")
        //存放每个元素的偏移值（百分比）
        var indexOffset = 0f
        for (index in 0 until itemCount) {
            // 滑动偏移值（百分比）
            val scrollOffset = if (curve.canScrollHorizontally()) {
                horizontalScrollOffset.toFloat() / getHorizontalSpace()
            } else {
                verticalScrollOffset.toFloat() / getVerticalSpace()
            }

            // 对应view的显示尺寸
            val (width, height) = allItemSize[index]

            // 对应view的布局位置
            curve.getInterpolation(indexOffset - scrollOffset, vector2)
            log("recycleAndFillItems: offset: indexOffset:$indexOffset scrollOffset:$scrollOffset")

            childFrame.apply {
                left = (vector2.x * getHorizontalSpace() - 0.5f * width).toInt()
                top = (vector2.y * getVerticalSpace() - 0.5f * height).toInt()
                right = (vector2.x * getHorizontalSpace() + 0.5f * width).toInt()
                bottom = (vector2.y * getVerticalSpace() + 0.5f * height).toInt()
            }

            if (Rect.intersects(displayFrame, childFrame)) {
                val scrap = recycler.getViewForPosition(index)

                measureChildWithMargins(scrap, 0, 0)

                addView(scrap)

                //将这个item布局出来
                layoutDecorated(
                    scrap,
                    childFrame.left,
                    childFrame.top,
                    childFrame.right,
                    childFrame.bottom
                )
            }

            //累积偏移值
            indexOffset += curve.getIndexOffset(width.toFloat() / getHorizontalSpace(), height.toFloat() / getVerticalSpace())
        }
        log("recycleAndFillItems: after for")
    }

    private fun getHorizontalSpace(): Int {
        return width - paddingLeft - paddingRight
    }

    private fun getVerticalSpace(): Int {
        return height - paddingBottom - paddingTop
    }

    private fun log(msg: String) {
        if (IS_DEBUG) Log.d(TAG, msg)
    }

    companion object {
        private val TAG = CurveLayoutManger::class.java.simpleName

        private var IS_DEBUG = true

        const val HORIZONTAL = 0
        const val VERTICAL = 1
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(HORIZONTAL, VERTICAL)
    annotation class Orientation

    /** layout用的曲线 */
    interface Curve {
        @Orientation
        fun getScrollOrientation(): Int {
            return HORIZONTAL
        }

        fun canScrollHorizontally() = getScrollOrientation() != VERTICAL
        fun canScrollVertically() = getScrollOrientation() == VERTICAL

        /**
         * @param i index 当前元素的位置(in itemCount)，当前child的宽
         * @param position 第i个元素的位置
         */
        fun getInterpolation(
            i: Float,
            position: Vector2
        )

        /**
         * 获取两个元素之间的间距
         *
         * 注：TODO 目前是根据 第n个元素的width/height来计算第n+1个元素与第n个元素的偏移值
         * TODO 之后可能要改成 根据两个元素的宽高来计算偏移值
         */
        fun getIndexOffset(width: Float, height: Float): Float {
            return if (canScrollHorizontally()) {
                width
            } else {
                height
            }
        }
    }

    private data class Size(
        var width: Int = 0,
        var height: Int = 0
    )

    data class Vector2(
        var x: Float = 0f,
        var y: Float = 0f
    )
}