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
import dev.answer.material.graphics.ColorDrawable
import dev.answer.material.graphics.Drawable
import dev.answer.material.graphics.StateDrawable
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text

/**
 *
 * @author AnswerDev
 * @date 2026/2/10 12:21
 * @description Button
 */
class Button(
    context: Context,
    private val drawable: Drawable? = null,
    private var text: String = "",
) : View(context) {

    private val stateDrawable: StateDrawable? = drawable as? StateDrawable

    private var cachedTextNode = Text()

    var textSize: Double = 14.0
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }

    var textColor: Color = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    init {
        clickable = true

        background = when {
            drawable is StateDrawable -> drawable
            drawable != null -> drawable
            else -> ColorDrawable(Color.web("#3498DB"))
        }

        updateTextNode()
    }

    fun setText(value: String) {
        text = value
        updateTextNode()
        requestLayout()
        invalidate()
    }

    private fun updateTextNode() {
        cachedTextNode.text = text
        cachedTextNode.font = Font.font("System", textSize)
    }


    override fun onMeasure(widthSpec: Int, heightSpec: Int) {

        updateTextNode()

        val bounds = cachedTextNode.layoutBounds

        val desiredWidth = bounds.width + paddingLeft + paddingRight + 24
        val desiredHeight = bounds.height + paddingTop + paddingBottom + 12

        measuredWidth = resolveSize(desiredWidth, widthSpec)
        measuredHeight = resolveSize(desiredHeight, heightSpec)
    }

    fun setOnClicked(action: () -> Unit) {
        onClickListener = object : View.OnClickListener {
            override fun onClick(v: View) {
                action()
            }
        }
    }

    override fun onTouchEvent(event: MouseEvent): Boolean {

        when (event.eventType) {

            MouseEvent.MOUSE_ENTERED ->
                stateDrawable?.setState(StateDrawable.State.HOVER)

            MouseEvent.MOUSE_EXITED ->
                stateDrawable?.setState(StateDrawable.State.NORMAL)

            MouseEvent.MOUSE_PRESSED ->
                stateDrawable?.setState(StateDrawable.State.PRESSED)

            MouseEvent.MOUSE_RELEASED ->
                stateDrawable?.setState(StateDrawable.State.NORMAL)
        }

        invalidate()
        return super.onTouchEvent(event)
    }

    override fun onDraw(gc: GraphicsContext) {

        if (text.isEmpty()) return

        gc.font = cachedTextNode.font
        gc.fill = textColor

        val bounds = cachedTextNode.layoutBounds

        gc.textAlign = javafx.scene.text.TextAlignment.CENTER
        gc.textBaseline = javafx.geometry.VPos.CENTER

        val centerX = width / 2
        val centerY = height / 2

        gc.fillText(text, centerX, centerY)
    }
}
