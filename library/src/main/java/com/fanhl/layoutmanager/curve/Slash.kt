package com.fanhl.layoutmanager.curve

import com.fanhl.layoutmanager.CurveLayoutManger

/**
 * 斜线
 *
 * @param slope 斜率
 */
class Slash(
    private val slope: Float = .2f
) : CurveLayoutManger.Curve {
    override fun getInterpolation(i: Float, position: CurveLayoutManger.Vector2) {
        position.x = i + 0.5f
        position.y = slope * i + 0.5f
    }
}