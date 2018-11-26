package com.fanhl.layoutmanager.curve

import android.graphics.Path
import android.graphics.PathMeasure
import com.fanhl.layoutmanager.CurveLayoutManger

/**
 * 路径
 */
open class PathCurve : CurveLayoutManger.Curve() {
    /**
     * 比例尺
     *
     * item移动距离:手势滑动距离 (两者的比)
     */
    var scale = 1f


    protected val path = Path()
    protected val pathMeasure = PathMeasure()

    protected val pos = FloatArray(2)

    override fun getInterpolation(i: Float, position: CurveLayoutManger.Vector2) {
        pathMeasure.getPosTan((getInitOffset() + i/scale) * pathMeasure.length, pos, null)
        position.x = pos[0]
        position.y = pos[1]
    }

    /**
     * 获取初始位置的偏移值
     */
    open fun getInitOffset(): Float {
        return 0f
    }

    override fun toString(): String {
        return "PathCurve"
    }
}