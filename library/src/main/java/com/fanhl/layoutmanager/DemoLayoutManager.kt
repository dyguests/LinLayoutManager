package com.fanhl.layoutmanager

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.util.SparseBooleanArray


class DemoLayoutManager : RecyclerView.LayoutManager() {
    private var verticalScrollOffset = 0
    private var totalHeight = 0

    //保存所有的Item的上下左右的偏移量信息
    private val allItemFrames = SparseArray<Rect>()
    //记录Item是否出现过屏幕且还没有回收。true表示出现过屏幕上，并且还没被回收
    private val hasAttachedItems = SparseBooleanArray()

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
        //定义竖直方向的偏移量
        var offsetY = 0
        totalHeight = 0
        for (i in 0 until itemCount) {
            //这里就是从缓存里面取出
            val view = recycler.getViewForPosition(i)
            //将View加入到RecyclerView中
            addView(view)

            measureChildWithMargins(view, 0, 0)

            val width = getDecoratedMeasuredWidth(view)
            val height = getDecoratedMeasuredHeight(view)

            totalHeight += height
            val frame = allItemFrames.get(i) ?: Rect()
            frame.set(0, offsetY, width, offsetY + height)
            // 将当前的Item的Rect边界数据保存
            allItemFrames.put(i, frame)
            // 由于已经调用了detachAndScrapAttachedViews，因此需要将当前的Item设置为未出现过
            hasAttachedItems.put(i, false)
            //将竖直方向偏移量增大height
            offsetY += height
        }

        //如果所有子View的高度和没有填满RecyclerView的高度，
        // 则将高度设置为RecyclerView的高度
        totalHeight = Math.max(totalHeight, getVerticalSpace())
        recycleAndFillItems(recycler, state)
    }

    override fun canScrollVertically(): Boolean {
        return true
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

        //将竖直方向的偏移量+travel
        verticalScrollOffset += travel

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
        val displayFrame = Rect(0, verticalScrollOffset, getHorizontalSpace(), verticalScrollOffset + getVerticalSpace())

        /**
         * 将滑出屏幕的Items回收到Recycle缓存中
         */
        val childFrame = Rect()
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

                val frame = allItemFrames.get(i)
                //将这个item布局出来
                layoutDecorated(
                    scrap,
                    frame.left,
                    frame.top - verticalScrollOffset,
                    frame.right,
                    frame.bottom - verticalScrollOffset
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