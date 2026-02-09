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

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.paint.Paint
import javafx.scene.text.Font
/**
 *
 * @author AnswerDev
 * @date 2026/2/9 20:55
 * @description TextView
 */


class TextView(
    text: String = ""
) : View() {

    private val label = Label()

    init {
        children += label
        setText(text)
        setGravity(Gravity.START)
    }

    fun setText(text: String) {
        label.text = text
    }

    fun getText(): String = label.text

    fun setTextSize(size: Double) {
        label.font = Font.font(label.font.family, size)
    }

    fun setTextColor(color: Paint) {
        label.textFill = color
    }

    fun setGravity(gravity: Gravity) {
        alignment = gravity.toPos(true)
    }

    fun setSingleLine(single: Boolean) {
        label.isWrapText = !single
    }

    fun setBold(bold: Boolean) {
        label.font = Font.font(
            label.font.family,
            if (bold) javafx.scene.text.FontWeight.BOLD else javafx.scene.text.FontWeight.NORMAL,
            label.font.size
        )
    }

    fun setItalic(italic: Boolean) {
        label.font = Font.font(
            label.font.family,
            if (italic) javafx.scene.text.FontPosture.ITALIC else javafx.scene.text.FontPosture.REGULAR,
            label.font.size
        )
    }
}
