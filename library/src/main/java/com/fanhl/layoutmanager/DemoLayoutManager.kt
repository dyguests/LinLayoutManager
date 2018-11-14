package com.fanhl.layoutmanager

import android.support.v7.widget.RecyclerView

class DemoLayoutManager : RecyclerView.LayoutManager() {
    private var mDecoratedChildWidth: Int = 0
    private var mDecoratedChildHeight: Int = 0

    private var mFirstVisiblePosition: Int = 0

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        val scrap = recycler.getViewForPosition(0)
        addView(scrap)
        measureChildWithMargins(scrap, 0, 0)

        /*
         * We make some assumptions in this code based on every child
         * view being the same size (i.e. a uniform grid). This allows
         * us to compute the following values up front because they
         * won't change.
         */
        mDecoratedChildWidth = getDecoratedMeasuredWidth(scrap)
        mDecoratedChildHeight = getDecoratedMeasuredHeight(scrap)
        detachAndScrapView(scrap, recycler)

        updateWindowSizing()
        val childLeft: Int
        val childTop: Int

        /*
         * Reset the visible and scroll positions
         */
        mFirstVisiblePosition = 0
        childLeft = 0
        childTop = 0

        //Clear all attached views into the recycle bin
        detachAndScrapAttachedViews(recycler)
        //Fill the grid for the initial layout of views
        fillGrid(DIRECTION_NONE, childLeft, childTop, recycler)
    }

    private fun updateWindowSizing() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun fillGrid(direction: Int, childLeft: Int, childTop: Int, recycler: RecyclerView.Recycler) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        const val DIRECTION_NONE: Int = 0
    }
}