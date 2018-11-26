package com.fanhl.layoutmanager.curve

import android.graphics.Path
import com.fanhl.layoutmanager.CurveLayoutManger

/**
 * 贪吃蛇
 *
 * @author fanhl
 */
class Snake(
    row: Int = 4,
    col: Int = 4
) : PathCurve(createPath(row, col)) {
    override fun toString(): String {
        return "Snake"
    }

    companion object {
        private fun createPath(row: Int, col: Int): Path {
            val eachWidth = 1f / col
            val eachHeight = 1f / row

            val path = Path()

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

            return path
        }
    }
}