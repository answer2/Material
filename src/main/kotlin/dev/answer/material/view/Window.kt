package dev.answer.material.view

import javafx.stage.Stage
import javafx.scene.Scene
/**
 *
 * @author AnswerDev
 * @date 2026/2/9 01:01
 * @description Window
 */
class Window {

    private val stage = Stage()

    var width: Double = 0.0
    var height: Double = 0.0
    var title : String = "Title"

    fun setContent(view: View) {
        stage.scene = Scene(view)
    }

    fun show() {
        if (width > 0) stage.width = width
        if (height > 0) stage.height = height
        stage.title = title;
        stage.show()
    }

    fun close() {
        stage.close()
    }
}
