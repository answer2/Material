/*
 * Copyright (C) 2026 AnswerDev
 * Licensed under the GNU General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by AnswerDev
 */

package dev.answer.material.view

import dev.answer.material.content.Context
import javafx.scene.paint.Paint
import javafx.scene.text.Font
/**
 *
 * @author AnswerDev
 * @date 2026/2/9 20:55
 * @description TextView
 */
class TextView(
    context: Context,
    text: String = ""
) : View(context) {

    var text: String = text
        set(value) {
            field = value
            invalidate()
        }

    var textSize: Double = 14.0
        set(value) {
            field = value
            invalidate()
        }

    var textColor: Paint = javafx.scene.paint.Color.BLACK
        set(value) {
            field = value
            invalidate()
        }

    var gravity: Gravity = Gravity.START
        set(value) {
            field = value
            invalidate()
        }

    var singleLine: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var bold: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var italic: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(gc: javafx.scene.canvas.GraphicsContext) {
        super.onDraw(gc)

        // 设置字体
        val fontWeight = if (bold) javafx.scene.text.FontWeight.BOLD else javafx.scene.text.FontWeight.NORMAL
        val fontPosture = if (italic) javafx.scene.text.FontPosture.ITALIC else javafx.scene.text.FontPosture.REGULAR
        val font = Font.font("System", fontWeight, fontPosture, textSize)
        gc.font = font

        // 设置文本颜色
        gc.fill = textColor

        // 计算文本位置
        val textWidth = gc.font.size * text.length * 0.6 // 粗略估算文本宽度
        val textHeight = gc.font.size

        var textX = left
        var textY = top + textHeight

        // 应用重力
        when (gravity) {
            Gravity.START -> { /* 左侧对齐 */ }
            Gravity.CENTER -> {
                textX = left + (width - textWidth) / 2
            }
            Gravity.END -> {
                textX = left + width - textWidth
            }
            else -> {}
        }

        // 绘制文本
        gc.fillText(text, textX, textY)
    }
}