package dev.answer.material.view

import javafx.geometry.Insets

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

open class LayoutParams(
    var width: Int = LayoutSize.WRAP_CONTENT,
    var height: Int = LayoutSize.WRAP_CONTENT,
    var margin: Insets = Insets.EMPTY
)

class LinearLayoutParams(
    width: Int = LayoutSize.WRAP_CONTENT,
    height: Int = LayoutSize.WRAP_CONTENT,
    var weight: Float = 0f,
    var gravity: Gravity = Gravity.START,
    margin: Insets
) : LayoutParams( width, height)

enum class Gravity {
    START,
    CENTER,
    END
}


