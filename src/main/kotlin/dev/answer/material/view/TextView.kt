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
