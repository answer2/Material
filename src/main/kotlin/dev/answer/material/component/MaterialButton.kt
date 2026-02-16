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

import dev.answer.material.content.Context
import dev.answer.material.graphics.RippleDrawable
import dev.answer.material.theme.ColorPalette
import dev.answer.material.view.View
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment

/**
 *
 * @author AnswerDev
 * @date 2026/2/16 02:08
 * @description MaterialButton with Ripple Effect
 */
class MaterialButton(
    context: Context,
    private val text: String,
    private val style: Style = Style.FILLED,
) : View(context) {

    private val rippleDrawable = RippleDrawable { this.invalidate() }
    init {
        clickable = true
    }


    private val textNode = javafx.scene.text.Text()

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val font = Font.font(16.0)

        textNode.text = text
        textNode.font = font

        val textWidth = textNode.layoutBounds.width
        val textHeight = textNode.layoutBounds.height

        val horizontalPadding = 24.0 * 2
        val verticalPadding = 8.0 * 2

        val desiredWidth = textWidth + horizontalPadding + paddingLeft + paddingRight
        val desiredHeight = maxOf(40.0, textHeight + verticalPadding) + paddingTop + paddingBottom

        measuredWidth = resolveSize(desiredWidth, widthSpec)
        measuredHeight = resolveSize(desiredHeight, heightSpec)
    }

    override fun onDraw(gc: GraphicsContext) {
        val colors = getColors()
        val cornerRadius = height

        // 绘制背景
        gc.fill = colors.first
        gc.fillRoundRect(0.0, 0.0, width, height, cornerRadius, cornerRadius)

        rippleDrawable.color = colors.second
        rippleDrawable.draw(gc, width, height, cornerRadius)

        // 绘制文本
        gc.fill = colors.second
        gc.font = Font.font(16.0)
        gc.textAlign = TextAlignment.CENTER
        gc.fillText(text, width / 2, height / 2 + 5)
    }

    override fun onTouchEvent(event: MouseEvent): Boolean {
        when (event.eventType) {
            MouseEvent.MOUSE_PRESSED -> {

                val (absX, absY) = getLocationInWindow()
                val localX = event.x - absX
                val localY = event.y - absY

                rippleDrawable.trigger(localX, localY, width, height)
                return true
            }
            MouseEvent.MOUSE_RELEASED -> {
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    private fun getColors(): Pair<Color, Color> {
        val palette: ColorPalette = context.colorScheme

        if (palette != null) {
            return when (style) {
                Style.FILLED -> palette.surfaceContainerLow to palette.primary
                Style.ELEVATED ->  palette.primary to palette.onPrimary
                Style.TONAL ->palette.secondaryContainer to palette.onSecondaryContainer
            }
        }
        return Color.RED to Color.RED
    }

    private fun getRippleColor(textColor: Color): Color {
        return textColor.deriveColor(0.0, 1.0, 1.0, 1.0)
    }

    enum class Style {
        FILLED, ELEVATED, TONAL
    }
}