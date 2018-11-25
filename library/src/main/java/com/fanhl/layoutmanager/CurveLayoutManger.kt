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
        private var curve: Curve = Parabola()
) : RecyclerView.LayoutManager() {
    private var horizontalScrollOffset = 0
    private var verticalScrollOffset = 0

    /** 总共可以偏移的距离 */
    private var totalDistance = 0

    // 当前scroll offset状态下的显示区域
    private val displayFrame = Rect()
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
    private val lastVector2 = Vector2()
    private val nextVector2 = Vector2()

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

        //先更新上宽比
        curve.ratio = getHorizontalSpace().toFloat() / getVerticalSpace()

        totalDistance = 0
        for (i in 0 until itemCount) {
            val view = recycler.getViewForPosition(i)

            addView(view)

            measureChildWithMargins(view, 0, 0)

            val width = getDecoratedMeasuredWidth(view)
            val height = getDecoratedMeasuredHeight(view)

            //累积(两个元素间的距离)偏移值
            allItemSize[i]?.apply {
                this.width = width
                this.height = height
            } ?: Size(width, height).also {
                allItemSize.put(i, it)
            }

            //当前点信息
            nextVector2.apply {
                x = width.toFloat() / getHorizontalSpace()
                y = height.toFloat() / getVerticalSpace()
            }

            if (i == 0) {
                totalDistance += (curve.getStartOffset(nextVector2) * getUnitSpace()).toInt()
            } else if (i == itemCount - 1) {
                totalDistance += (curve.getEndOffset(nextVector2) * getUnitSpace()).toInt()
            }

            //累积总totalDistance
            if (i > 0) {
                totalDistance += (curve.getItemSpacing(lastVector2, nextVector2) * getUnitSpace()).toInt()
            }
            lastVector2.apply {
                x = nextVector2.x
                y = nextVector2.y
            }

            hasAttachedItems.put(i, false)
        }

        displayFrame.apply {
            //            left=0
            //            top=0
            right = getHorizontalSpace()
            bottom = getVerticalSpace()
        }

//        log("totalDistance$:$totalDistance totalOffset:${totalDistance.toFloat() / getHorizontalSpace()}")

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
        }
        // FIXME: 2018/11/24 fanhl 另外还需要一种scroll方式用于贴边
        //这种scroll的区域中 居中，第一个元素可以居中，最后一个元素也可以居中
        else if (horizontalScrollOffset + dx > totalDistance) {
            travel = totalDistance - horizontalScrollOffset
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
        } else if (verticalScrollOffset + dy > totalDistance) {
            travel = totalDistance - verticalScrollOffset
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
//        log("recycleAndFillItems: before for")
        //存放每个元素的偏移值（百分比）
        var indexOffset = 0f
        for (index in 0 until itemCount) {
            // 对应view的显示尺寸
            val (width, height) = allItemSize[index]

            //累积(两个元素间的距离)偏移值
            nextVector2.apply {
                x = width.toFloat() / getHorizontalSpace()
                y = height.toFloat() / getVerticalSpace()
            }
            if (index == 0) {
                indexOffset += curve.getStartOffset(nextVector2)
            } else if (index > 0) {
                indexOffset += curve.getItemSpacing(lastVector2, nextVector2)
            }
            lastVector2.apply {
                x = nextVector2.x
                y = nextVector2.y
            }

            // 滑动偏移值（百分比）
            val scrollOffset = if (curve.canScrollHorizontally()) {
                horizontalScrollOffset.toFloat() / getHorizontalSpace()
            } else {
                verticalScrollOffset.toFloat() / getVerticalSpace()
            }

            // 对应view的布局位置
            curve.getInterpolation(indexOffset - scrollOffset, vector2)
//            log("recycleAndFillItems: offset: indexOffset:$indexOffset scrollOffset:$scrollOffset")

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
        }
//        log("recycleAndFillItems: after for")
    }

    /**
     * 获取单屏滚动距离
     */
    private fun getUnitSpace(): Int {
        return (if (curve.canScrollHorizontally()) {
            getHorizontalSpace()
        } else {
            getVerticalSpace()
        })
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

    /**
     * layout用的曲线
     */
    abstract class Curve {
        /**
         * 宽高比
         */
        var ratio = 1f
            set(value) {
                if (field == value) {
                    return
                }
                field = value
                onRatioChanged()
            }

        /**
         * 滚动方向
         */
        @Orientation
        open fun getScrollOrientation(): Int {
            return HORIZONTAL
        }

        fun canScrollHorizontally() = getScrollOrientation() != VERTICAL

        fun canScrollVertically() = getScrollOrientation() == VERTICAL

        /**
         * This method will be called at CurveLayoutManger::onLayoutChildren
         */
        open fun onRatioChanged() {
        }

        /**
         * @param i index 当前元素的位置(in itemCount)，当前child的宽
         * @param position 第i个元素的位置
         */
        abstract fun getInterpolation(
                i: Float,
                position: Vector2
        )

        /**
         * 获取两个元素之间的间距
         */
        open fun getItemSpacing(size1: Vector2, size2: Vector2): Float {
            return if (canScrollHorizontally()) {
                size1.x / 2 + size2.x / 2
            } else {
                size1.y / 2 + size2.y / 2
            }
        }

        /**
         * 滚动的起点位置
         */
        open fun getStartOffset(size: Vector2) = 0f

        /**
         * 滚动的终点位置
         */
        open fun getEndOffset(size: Vector2) = 0f
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