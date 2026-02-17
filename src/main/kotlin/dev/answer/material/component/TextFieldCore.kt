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

package dev.answer.material.component

/**
 *
 * @author AnswerDev
 * @date 2026/2/17 17:12
 * @description TextFieldCore
 */

import dev.answer.material.content.Activity
import dev.answer.material.view.View
import dev.answer.material.view.Window
import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.scene.input.InputMethodEvent
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.util.Duration

internal class TextFieldCore(
    private val view: View
) {
    var window : Window = (view.context as Activity).getWindow();
    var text: String = ""
        private set

    var cursorPosition: Int = 0
        private set

    var maxLength: Int = -1
    var passwordMode: Boolean = false

    var onTextChanged: ((String) -> Unit)? = null

    // IME composition text（拼音未确认部分）
    private var composingText: String = ""

    // 光标 overlay
    private var cursorNode: Pane? = null
    private var blinkTimeline: Timeline? = null

    private val fontSize = 16.0

    // =========================================================
    // Public
    // =========================================================

    fun getDisplayText(): String {
        return if (passwordMode) {
            "•".repeat(text.length)
        } else {
            text
        }
    }

    fun getDisplayTextWithComposition(): String {
        val base = getDisplayText()
        return base.substring(0, cursorPosition) +
                composingText +
                base.substring(cursorPosition)
    }

    fun attachCursor() {
        val node = Pane().apply {
            prefWidth = 1.5
            prefHeight = fontSize + 2
            background = null
            style = "-fx-background-color: #6200EE;"
        }

        cursorNode = node
//        window.addOverlay(node)

        startBlink()
        updateCursor()
    }

    fun detachCursor() {
        blinkTimeline?.stop()
        blinkTimeline = null
//        cursorNode?.let { window?.removeOverlay(it) }
        cursorNode = null
    }

    // =========================================================
    // Key Events
    // =========================================================

    fun onKeyPressed(event: KeyEvent) {
        when (event.code) {
            KeyCode.BACK_SPACE -> {
                if (composingText.isNotEmpty()) {
                    composingText = ""
                    notifyChanged()
                    return
                }

                if (cursorPosition > 0) {
                    text = text.removeRange(cursorPosition - 1, cursorPosition)
                    cursorPosition--
                    notifyChanged()
                }
            }
            KeyCode.LEFT -> {
                if (cursorPosition > 0) {
                    cursorPosition--
                    updateCursor()
                }
            }
            KeyCode.RIGHT -> {
                if (cursorPosition < text.length) {
                    cursorPosition++
                    updateCursor()
                }
            }

            else -> {}
        }
    }

    fun onKeyTyped(event: KeyEvent) {
        val ch = event.character
        if (ch.isNullOrEmpty()) return
        if (ch[0].isISOControl()) return

        if (maxLength >= 0 && text.length >= maxLength) return

        text = text.substring(0, cursorPosition) +
                ch +
                text.substring(cursorPosition)

        cursorPosition++
        notifyChanged()
    }


    // =========================================================
    // IME 处理
    // =========================================================

    fun onInputMethod(event: InputMethodEvent) {

        // 1️⃣ commitText（已确认输入）
        val committed = event.committed

        if (committed.isNotEmpty()) {
            text = text.substring(0, cursorPosition) +
                    committed +
                    text.substring(cursorPosition)

            cursorPosition += committed.length
        }

        // 2️⃣ 处理 composingText（拼音候选中）
        composingText = buildString {
            event.composed.forEach {
                append(it.text)
            }
        }

        notifyChanged()
    }

    // =========================================================
    // 光标更新
    // =========================================================

    private fun updateCursor() {
        val node = cursorNode ?: return

        val display = getDisplayTextWithComposition()
        val font = Font.font(fontSize)

        val before = display.substring(0, cursorPosition + composingText.length)

        val width = measureTextWidth(before, font)

        node.layoutX = view.x + view.paddingLeft + width
        node.layoutY = view.y + view.paddingTop - fontSize
    }

    private fun startBlink() {
        blinkTimeline?.stop()

        val node = cursorNode ?: return

        blinkTimeline = Timeline(
            KeyFrame(Duration.millis(0.0), KeyValue(node.opacityProperty(), 1.0)),
            KeyFrame(Duration.millis(500.0), KeyValue(node.opacityProperty(), 0.0)),
            KeyFrame(Duration.millis(1000.0), KeyValue(node.opacityProperty(), 1.0))
        ).apply {
            cycleCount = Animation.INDEFINITE
            play()
        }
    }

    private fun notifyChanged() {
        updateCursor()
        onTextChanged?.invoke(text)
        view.invalidate()
    }

    private fun measureTextWidth(str: String, font: Font): Double {
        val t = Text(str)
        t.font = font
        return t.layoutBounds.width
    }
}