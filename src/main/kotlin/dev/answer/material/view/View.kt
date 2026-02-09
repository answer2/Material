package dev.answer.material.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Region
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


    // Android 风格 padding
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

}