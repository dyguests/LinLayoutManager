package com.fanhl.layoutmanager.curve

import com.fanhl.layoutmanager.CurveLayoutManger
import kotlin.math.PI

/**
 * Ferris wheel
 *
 * Resources:[https://github.com/danylovolokh/LondonEyeLayoutManager]
 */
class FerrisWheel : CurveLayoutManger.Curve() {
    /** 圆心 */
    private val center = CurveLayoutManger.Vector2(0f, 0f)
    /** 起始位置 */
    private val starting = CurveLayoutManger.Vector2(.5f, .5f)

    /** 半径 */
    private var radius: Float = 1f
    /** 初始角度 */
    private var startingAngle = 45 * PI / 180

    init {
        updateRadius()
    }

    override fun onRatioChanged() {
        updateRadius()
    }

    override fun getInterpolation(i: Float, position: CurveLayoutManger.Vector2) {
        //弧度
        val startAngle = startingAngle - i

        // 套圈了的位置直接放到 Vector2(-1，-1) 上去
        if (startAngle > PI || startAngle < -PI / 2) {
            position.x = -1f
            position.y = -1f
            return
        }

        val x = Math.cos(startAngle) * radius
        val y = Math.sin(startAngle) * radius * ratio

        position.x = (center.x + x).toFloat()
        position.y = (center.y + y).toFloat()
    }

    override fun getItemSpacing(size1: CurveLayoutManger.Vector2, size2: CurveLayoutManger.Vector2): Float {
        val spacing = Math.sqrt(((size1.x + size2.x) * (size1.x + size2.x) + (size1.y + size2.y) * (size1.y + size2.y) / ratio / ratio).toDouble()) / 2
        val radian = Math.acos(((2 * radius * radius / ratio - spacing * spacing) * ratio / 2 / radius / radius)).toFloat()
        return radian
    }

    override fun toString(): String {
        return "FerrisWheel"
    }

    private fun updateRadius() {
        radius = Math.sqrt(((center.x - starting.x) * (center.x - starting.x) + (center.y - starting.y) * (center.y - starting.y) / ratio / ratio).toDouble()).toFloat()
        startingAngle = Math.atan((1 / ratio).toDouble())
//        Log.d("Wheel", "startingAngle:${startingAngle * 180 / PI}")
    }
}