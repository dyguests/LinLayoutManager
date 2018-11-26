package com.fanhl.layoutmanager.curve

import android.graphics.Path
import com.fanhl.layoutmanager.CurveLayoutManger

/**
 * 贪吃蛇
 *
 * @author fanhl
 */
class Snake(
    private val row: Int = 5,
    private val col: Int = 4
) : PathCurve() {
    private var itemSpacing = 1f

    init {
        createPath(row, col, path)
        pathMeasure.setPath(path, false)
        updateItemSpacing()
    }

    override fun getItemSpacing(size1: CurveLayoutManger.Vector2, size2: CurveLayoutManger.Vector2): Float {
        return itemSpacing
    }

    override fun getEndOffset(size: CurveLayoutManger.Vector2): Float {
        return -itemSpacing * (row * col - 1)
    }

    override fun toString(): String {
        return "Snake"
    }

    private fun updateItemSpacing() {
//        itemSpacing = pathMeasure.length / (row * col - 1)
        itemSpacing = 1f / (row * col - 1)
    }

    private fun createPath(row: Int, col: Int, path: Path) {
        val eachWidth = 1f / col
        val eachHeight = 1f / row

        path.reset()

        //初始位置
        val vector2 = CurveLayoutManger.Vector2(eachWidth / 2, eachHeight / 2)

        path.moveTo(vector2.x, vector2.y)

        /*
        当前方向
        0 右
        1 下
        2 左
        3 下
        */
        var direction = 0

        var currRow = 0
        while (currRow < row) {
            if (direction == 0) {
                //绘制到当前行最右边
                vector2.x = (col - .5f) * eachWidth

                currRow++
            } else if (direction == 1) {
                //绘制到正下方
                vector2.y += eachHeight
            } else if (direction == 2) {
                //绘制到当前行最左边
                vector2.x = .5f * eachWidth

                currRow++
            } else if (direction == 3) {
                //绘制到正下方
                vector2.y += eachHeight
            }

            path.lineTo(vector2.x, vector2.y)

            direction = (direction + 1) % 4
        }
    }
}