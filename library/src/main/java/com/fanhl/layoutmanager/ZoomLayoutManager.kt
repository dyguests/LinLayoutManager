package com.fanhl.layoutmanager

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.util.SparseBooleanArray

/**
 * 可缩放的LayoutManager
 */
class ZoomLayoutManager(
) : RecyclerView.LayoutManager() {
    private var horizontalScrollOffset = 0
    private var totalWidth = 0

    //保存所有的Item的上下左右的偏移量信息
    private val allItemFrames = SparseArray<Rect>()
    //记录Item是否出现过屏幕且还没有回收。true表示出现过屏幕上，并且还没被回收
    private val hasAttachedItems = SparseBooleanArray()

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
        if (itemCount <= 0) {
            return
        }
        if (state.isPreLayout) {
            return
        }
        //在布局之前，将所有的子View先Detach掉，放入到Scrap缓存中
        detachAndScrapAttachedViews(recycler)

        //先默认为水平方向

        var offsetX = 0
        totalWidth = 0

        for (i in 0 until itemCount) {
            val view = recycler.getViewForPosition(i)

            addView(view)

            measureChildWithMargins(view, 0, 0)

            val width = getDecoratedMeasuredWidth(view)
            val height = getDecoratedMeasuredHeight(view)

            totalWidth += width

            val frame = allItemFrames[i] ?: Rect().also {
                allItemFrames.put(i, it)
            }

            frame.set(offsetX, 0, offsetX + width, height)

            hasAttachedItems.put(i, false)

            offsetX += width
        }

        //如果所有子View的高度和没有填满RecyclerView的高度，
        // 则将高度设置为RecyclerView的高度
        totalWidth = Math.max(totalWidth, getHorizontalSpace())

        recycleAndFillItems(recycler, state)
    }

    override fun canScrollHorizontally(): Boolean {
        return true
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
        offsetChildrenVertical(-travel)

        recycleAndFillItems(recycler, state)
        return travel
    }

    private fun recycleAndFillItems(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.isPreLayout) { // 跳过preLayout，preLayout主要用于支持动画
            return
        }

        // 当前scroll offset状态下的显示区域
        val displayFrame = Rect(horizontalScrollOffset, 0, horizontalScrollOffset + getHorizontalSpace(), getVerticalSpace())

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
            if (Rect.intersects(displayFrame, allItemFrames.get(i))) {
                val scrap = recycler.getViewForPosition(i)
                measureChildWithMargins(scrap, 0, 0)
                addView(scrap)

//                scrap.scaleX=.8f
//                scrap.scaleY=.8f

                val frame = allItemFrames.get(i)
                //将这个item布局出来
                layoutDecorated(
                    scrap,
                    frame.left - horizontalScrollOffset,
                    frame.top,
                    frame.right - horizontalScrollOffset,
                    frame.bottom
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
}