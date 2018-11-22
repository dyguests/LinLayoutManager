package com.fanhl.layoutmanager

import android.support.annotation.IntDef
import android.support.v7.widget.RecyclerView

/**
 * 自定义曲线LayoutManager
 */
class CurveLayoutManger : RecyclerView.LayoutManager() {
    var curve: Curve = Parabola()

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

    }

    override fun canScrollVertically(): Boolean {
        return curve.canScrollVertically()
    }

    override fun canScrollHorizontally(): Boolean {
        return curve.canScrollHorizontally()
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

        fun canScrollVertically() = getScrollOrientation() == VERTICAL
        fun canScrollHorizontally() = getScrollOrientation() == HORIZONTAL
    }

    /** 抛物线 */
    class Parabola : Curve {
        override fun getScrollOrientation() = HORIZONTAL

    }
}