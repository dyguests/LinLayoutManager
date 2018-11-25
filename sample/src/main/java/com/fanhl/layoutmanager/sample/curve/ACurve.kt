package com.fanhl.layoutmanager.sample.curve

import com.fanhl.layoutmanager.CurveLayoutManger

class ACurve : CurveLayoutManger.Curve() {
    private val points = listOf(
            CurveLayoutManger.Vector2(-.5f, 0f),
            CurveLayoutManger.Vector2(.1f, 0f),
            CurveLayoutManger.Vector2(.7f, .1f),
            CurveLayoutManger.Vector2(1.2f, .6f),
            CurveLayoutManger.Vector2(.8f, 1.1f),
            CurveLayoutManger.Vector2(.2f, 1.1f),
            CurveLayoutManger.Vector2(-.2f, .6f),
            CurveLayoutManger.Vector2(.3f, .1f),
            CurveLayoutManger.Vector2(.9f, 0f),
            CurveLayoutManger.Vector2(1.5f, 0f)
    )

    private val factorials = listOf(
            1,
            1,
            2,
            6,
            24,
            120,
            720,
            5040,
            40320,
            362880,
            3628800
    )

    private val N = points.size - 1

    override fun getInterpolation(i: Float, position: CurveLayoutManger.Vector2) {
        // x=(b(t,0,x_1)+b(t,1,x_2)+b(t,2,x_3)+b(t,3,x_4)+b(t,4,x_5)+b(t,5,x_6)+b(t,6,x_7)+b(t,7,x_8)+b(t,8,x_9)+b(t,9,x_{10})
        // y=b(t,0,y_1)+b(t,1,y_2)+b(t,2,y_3)+b(t,3,y_4)+b(t,4,y_5)+b(t,5,y_6)+b(t,6,y_7)+b(t,7,y_8)+b(t,8,y_9)+b(t,9,y_{10}))

        val t = i + .5f

        position.apply {
            x = points.asSequence().mapIndexed { index, vector2 -> b(t, index, vector2.x) }.sum()
            y = 1 - points.asSequence().mapIndexed { index, vector2 -> b(t, index, vector2.y) }.sum()
        }
    }

    /**
     * This function calculates the binomial factor (n k). It is needed further in the Bézier curve calculations.
     */
    private fun f(n: Int, k: Int): Int {
        return factorials[n] / factorials[k] / factorials[n - k]
    }

    /**
     * Next function simplifies the bézier curve calculation. Call this function separately for the X and Y coordinates, and sum each function call to previous calls for constructing the curve. K is the current point index starting from 0 (e.g. 3rd point is index 2). v is either the X or Y point coordinate. t is the "function time", but no special treatment is needed; simply put t in the function call.
     */
    private fun b(t: Float, K: Int, v: Float): Float {
        return (f(N, K) * Math.pow(((1f - t).toDouble()), ((N - K).toDouble())) * Math.pow(t.toDouble(), K.toDouble()) * v).toFloat()
    }

    override fun toString(): String {
        return "ACurve"
    }
}