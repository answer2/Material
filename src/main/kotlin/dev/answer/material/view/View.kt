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

import dev.answer.material.graphics.Drawable
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundImage
import javafx.scene.layout.BackgroundPosition
import javafx.scene.layout.BackgroundRepeat
import javafx.scene.layout.BackgroundSize
import javafx.scene.layout.StackPane

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 00:48
 * @description Vew
 */
open class View : StackPane() {

    var layoutParams: LayoutParams = LayoutParams()

    open fun onAttach() {}
    open fun onDetach() {}

    fun setPadding(
        start: Double,
        top: Double,
        end: Double,
        bottom: Double
    ) {
        padding = Insets(top, end, bottom, start)
    }

    fun setPadding(all: Double) {
        padding = Insets(all)
    }

    fun Gravity.toPos(isVertical: Boolean): Pos {
        return when (this) {
            Gravity.START -> if (isVertical) Pos.TOP_LEFT else Pos.CENTER_LEFT
            Gravity.CENTER -> Pos.CENTER
            Gravity.END -> if (isVertical) Pos.BOTTOM_RIGHT else Pos.CENTER_RIGHT
        }
    }

    fun setBackground(drawable: Drawable) {
        background = Background(
            BackgroundImage(
                drawable.getImage(),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT
            )
        )
    }


}