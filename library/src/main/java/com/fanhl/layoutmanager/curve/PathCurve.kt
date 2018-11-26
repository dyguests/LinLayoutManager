package com.fanhl.layoutmanager.curve

import android.graphics.Path
import android.graphics.PathMeasure
import com.fanhl.layoutmanager.CurveLayoutManger

/**
 * 路径
 */
open class PathCurve(
    path: Path
) : CurveLayoutManger.Curve() {

    private val pathMeasure = PathMeasure(path, false)

    private val pos = FloatArray(2)

    init {
        if (pathMeasure.length <= 0f) {
            throw Exception("Don't use empty path")
        }
    }

    override fun getInterpolation(i: Float, position: CurveLayoutManger.Vector2) {
        pathMeasure.getPosTan((getInitOffset() + i) * pathMeasure.length, pos, null)
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