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
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.scene.text.Text

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 20:55
 * @description TextView
 */

class TextView(context: Context, text: String = "") : View(context) {

    var text: String = text
        set(value) { field = value; invalidate() }

    var textSize: Double = 14.0
        set(value) { field = value; invalidate() }

    var textColor: Paint = Color.BLACK
        set(value) { field = value; invalidate() }

    var gravity: Gravity = Gravity.START
        set(value) { field = value; invalidate() }

    var verticalGravity: VerticalGravity = VerticalGravity.CENTER
        set(value) { field = value; invalidate() }

    var bold: Boolean = false
        set(value) { field = value; invalidate() }

    var italic: Boolean = false
        set(value) { field = value; invalidate() }


    override fun onMeasure(widthSpec: Int, heightSpec: Int) {

        val fontWeight = if (bold) FontWeight.BOLD else FontWeight.NORMAL
        val fontPosture = if (italic) FontPosture.ITALIC else FontPosture.REGULAR
        val font = Font.font("System", fontWeight, fontPosture, textSize)

        val textNode = Text(text)
        textNode.font = font

        val bounds = textNode.layoutBounds

        val desiredWidth = bounds.width + paddingLeft + paddingRight
        val desiredHeight = bounds.height + paddingTop + paddingBottom

        measuredWidth = resolveSize(desiredWidth, widthSpec)
        measuredHeight = resolveSize(desiredHeight, heightSpec)
    }


    override fun onDraw(gc: GraphicsContext) {

        if (text.isEmpty()) return

        val fontWeight = if (bold) FontWeight.BOLD else FontWeight.NORMAL
        val fontPosture = if (italic) FontPosture.ITALIC else FontPosture.REGULAR
        val font = Font.font("System", fontWeight, fontPosture, textSize)

        gc.font = font
        gc.fill = textColor

        // 水平对齐
        val textX = when (gravity) {
            Gravity.START -> {
                gc.textAlign = javafx.scene.text.TextAlignment.LEFT
                paddingLeft
            }
            Gravity.CENTER -> {
                gc.textAlign = javafx.scene.text.TextAlignment.CENTER
                width / 2
            }
            Gravity.END -> {
                gc.textAlign = javafx.scene.text.TextAlignment.RIGHT
                width - paddingRight
            }
        }

        // 垂直对齐
        val textY = when (verticalGravity) {
            VerticalGravity.TOP -> {
                gc.textBaseline = javafx.geometry.VPos.TOP
                paddingTop
            }
            VerticalGravity.CENTER -> {
                gc.textBaseline = javafx.geometry.VPos.CENTER
                height / 2
            }
            VerticalGravity.BOTTOM -> {
                gc.textBaseline = javafx.geometry.VPos.BOTTOM
                height - paddingBottom
            }
        }

        gc.fillText(text, textX, textY)
    }

}

