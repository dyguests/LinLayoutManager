package com.fanhl.layoutmanager.curve

import com.fanhl.layoutmanager.CurveLayoutManger
import kotlin.math.PI

/**
 * Ferris wheel
 *
 * Resources:[https://github.com/danylovolokh/LondonEyeLayoutManager]
 */
class FerrisWheel : CurveLayoutManger.Curve() {
    private val center: CurveLayoutManger.Vector2 = CurveLayoutManger.Vector2(0f, 0f)
    private var radius: Float = 1f

    init {
        updateRadius()
    }

    override fun onRatioChanged() {
        updateRadius()
    }

    override fun getInterpolation(i: Float, position: CurveLayoutManger.Vector2) {
        //弧度
        val startAngle = 45 * Math.PI / 180 - i

        // 套圈了的位置直接放到 Vector2(-1，-1) 上去
        if (startAngle > PI || startAngle < -PI / 2) {
            position.x = -1f
            position.y = -1f
            return
        }

        val x = Math.cos(startAngle) * radius / ratio
        val y = Math.sin(startAngle) * radius

        position.x = (center.x + x).toFloat()
        position.y = (center.y + y).toFloat()
    }

    override fun getItemSpacing(size1: CurveLayoutManger.Vector2, size2: CurveLayoutManger.Vector2): Float {
        val sapcing = Math.sqrt(((size1.x + size2.x) * (size1.x + size2.x) + (size1.y + size2.y) * (size1.y + size2.y) / ratio / ratio).toDouble()) / 2
        return Math.acos(((2 * radius * radius / ratio - sapcing * sapcing) * ratio / 2 / radius / radius)).toFloat()
    }

    override fun toString(): String {
        return "FerrisWheel(center:(${center.x},${center.y}))"
    }

    private fun updateRadius() {
        center.x = .5f - .5f / ratio

        radius = Math.sqrt((.5f * .5f + .5f * .5f).toDouble()).toFloat()
    }
}