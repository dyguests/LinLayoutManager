package com.fanhl.layoutmanager

import android.R.attr.topOffset
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.View


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

    override fun canScrollVertically(): Boolean {
        return true
    }

    private fun updateWindowSizing() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun fillGrid(direction: Int, childLeft: Int, childTop: Int, recycler: RecyclerView.Recycler) {
        val viewCache = SparseArray<View>(childCount)
        //...
        if (childCount != 0) {
            //...
            //Cache all views by their existing position, before updating counts
            for (i in 0 until childCount) {
                val position = positionOfIndex(i)
                val child = getChildAt(i)
                viewCache.put(position, child)
            }
            //Temporarily detach all views.
            // Views we still need will be added back at the proper index.
            for (i in 0 until viewCache.size()) {
                detachView(viewCache.valueAt(i))
            }
        }


//        for (i in 0 until getVisibleChildCount()) {
//            //...
//
//            //Layout this position
//            var view = viewCache.get(nextPosition)
//            if (view == null) {
//                /*
//                 * The Recycler will give us either a newly constructed view,
//                 * or a recycled view it has on-hand. In either case, the
//                 * view will already be fully bound to the data by the
//                 * adapter for us.
//                 */
//                view = recycler.getViewForPosition(nextPosition)
//                addView(view)
//
//                /*
//                 * It is prudent to measure/layout each new view we
//                 * receive from the Recycler. We don't have to do
//                 * this for views we are just re-arranging.
//                 */
//                measureChildWithMargins(view, 0, 0)
//                layoutDecorated(
//                    view, leftOffset, topOffset,
//                    leftOffset + mDecoratedChildWidth,
//                    topOffset + mDecoratedChildHeight
//                )
//            } else {
//                //Re-attach the cached view at its new index
//                attachView(view)
//                viewCache.remove(nextPosition)
//            }
//
//            //...
//         }


        for (i in 0 until viewCache.size()) {
            recycler.recycleView(viewCache.valueAt(i))
        }
    }

    private fun positionOfIndex(index: Int): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        const val DIRECTION_NONE: Int = 0
    }
}