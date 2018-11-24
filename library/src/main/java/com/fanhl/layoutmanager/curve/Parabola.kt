package com.fanhl.layoutmanager.curve

import com.fanhl.layoutmanager.CurveLayoutManger

/** 抛物线 */
class Parabola(
    private val focus: Float = .2f
) : CurveLayoutManger.Curve {
    override fun getInterpolation(i: Float, position: CurveLayoutManger.Vector2) {
        position.x = i + 0.5f
        position.y = focus * i * i + 0.5f
    }

    override fun toString(): String {
        return "Parabola(focus:$focus)"
    }
}