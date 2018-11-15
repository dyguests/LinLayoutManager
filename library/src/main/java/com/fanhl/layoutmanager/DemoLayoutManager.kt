package com.fanhl.layoutmanager

import android.support.v7.widget.RecyclerView

class DemoLayoutManager : RecyclerView.LayoutManager() {
    private var verticalScrollOffset = 0
    private var totalHeight = 0

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
            //最后，将View布局
            layoutDecorated(view, 0, offsetY, width, offsetY + height)
            //将竖直方向偏移量增大height
            offsetY += height
            //
            totalHeight += height
        }

        //如果所有子View的高度和没有填满RecyclerView的高度，
        // 则将高度设置为RecyclerView的高度
        totalHeight = Math.max(totalHeight, getVerticalSpace())
    }

    override fun canScrollVertically(): Boolean {
        return true
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
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

        return travel
    }

    private fun getVerticalSpace(): Int {
        return height - paddingBottom - paddingTop
    }
}