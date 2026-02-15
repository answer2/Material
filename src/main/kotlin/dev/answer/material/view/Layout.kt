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

import javafx.geometry.Insets
import javafx.geometry.Pos

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 20:39
 * @description Layout
 */

object LayoutSize {
    const val MATCH_PARENT = -1
    const val WRAP_CONTENT = -2
}

enum class VerticalGravity { TOP, CENTER, BOTTOM }

enum class Gravity {
    START,
    CENTER,
    END
}


// 布局参数类
open class LayoutParams {
    var width: Int = WRAP_CONTENT
    var height: Int = WRAP_CONTENT
    var marginLeft: Double = 0.0
    var marginTop: Double = 0.0
    var marginRight: Double = 0.0
    var marginBottom: Double = 0.0

    open fun copy(): LayoutParams {
        return LayoutParams(width, height)
    }

    constructor()

    constructor(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    companion object {
        const val MATCH_PARENT = -1
        const val WRAP_CONTENT = -2
    }
}


class LinearLayoutParams(
    width: Int = LayoutSize.WRAP_CONTENT,
    height: Int = LayoutSize.WRAP_CONTENT,
    var weight: Float = 0f,
    var gravity: Gravity = Gravity.START,
) : LayoutParams(width, height) {

    override fun copy(): LayoutParams {
        return LinearLayoutParams(width, height, weight, gravity)
    }
}


class FrameLayoutParams(
    width: Int = LayoutSize.WRAP_CONTENT,
    height: Int = LayoutSize.WRAP_CONTENT,
    var alignment: Pos = Pos.TOP_LEFT,
) : LayoutParams(width, height)

