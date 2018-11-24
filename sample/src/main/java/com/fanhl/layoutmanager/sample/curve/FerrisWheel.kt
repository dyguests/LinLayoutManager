package com.fanhl.layoutmanager.sample.curve

import com.fanhl.layoutmanager.CurveLayoutManger

/**
 * Ferris wheel
 * Resources:[https://github.com/danylovolokh/LondonEyeLayoutManager]
 */
class FerrisWheel(
        private val center: CurveLayoutManger.Vector2 = CurveLayoutManger.Vector2(0f, 0f)
) : CurveLayoutManger.Curve {
    private val radius: Float

    init {
        //起始位置
        val startV2 = CurveLayoutManger.Vector2(.5f, .5f)
        radius = Math.sqrt(((center.x - startV2.x) * (center.x - startV2.x) + (center.y - startV2.y) * (center.y - startV2.y)).toDouble()).toFloat()
    }

    override fun getInterpolation(i: Float, position: CurveLayoutManger.Vector2) {
        //弧度
        val startAngle = 45 * Math.PI / 180 - i
        val x = Math.cos(startAngle) * radius
        val y = Math.sin(startAngle) * radius

        position.x = (center.x + x).toFloat()
        position.y = (center.y + y).toFloat()
    }

    override fun toString(): String {
        return "FerrisWheel(center:(${center.x},${center.y}))"
    }
}