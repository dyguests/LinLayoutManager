package com.fanhl.layoutmanager.curve

import com.fanhl.layoutmanager.CurveLayoutManger

/**
 * Ferris wheel
 *
 * Resources:[https://github.com/danylovolokh/LondonEyeLayoutManager]
 */
class FerrisWheel : CurveLayoutManger.Curve() {
    private val center: CurveLayoutManger.Vector2 = CurveLayoutManger.Vector2(0f, 0f)
    private val radius: Float

    init {
        //起始位置
        val startV2 = CurveLayoutManger.Vector2(.5f, .5f)
        radius = Math.sqrt(((center.x - startV2.x) * (center.x - startV2.x) + (center.y - startV2.y) * (center.y - startV2.y)).toDouble()).toFloat()
    }

    override fun getInterpolation(i: Float, position: CurveLayoutManger.Vector2) {
        //弧度
        val startAngle = 45 * Math.PI / 180 - i

        //fixme 套圈了的位置直接放到 Vector2(-1，-1) 上去

        val x = Math.cos(startAngle) * radius
        val y = Math.sin(startAngle) * radius

        position.x = (center.x + x).toFloat()
        position.y = (center.y + y).toFloat()
    }

    override fun getItemSpacing(size1: CurveLayoutManger.Vector2, size2: CurveLayoutManger.Vector2): Float {
        return spacingToRadian(getRadius(size1) + getRadius(size2))
    }

    private fun getRadius(size: CurveLayoutManger.Vector2) = (Math.sqrt((size.x * size.x + size.y * size.y).toDouble()) / 2).toFloat()

    override fun toString(): String {
        return "FerrisWheel(center:(${center.x},${center.y}))"
    }

    private fun spacingToRadian(spacing: Float): Float {
        return Math.acos(((2 * radius * radius - spacing * spacing) / 2 / radius / radius).toDouble()).toFloat()
    }
}