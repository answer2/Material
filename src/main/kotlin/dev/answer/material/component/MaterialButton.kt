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
import dev.answer.material.view.View
import dev.answer.material.view.animation.PressAnimation
import javafx.animation.Interpolator
import javafx.animation.Transition
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import javafx.util.Duration
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.sqrt

/**
 *
 * @author AnswerDev
 * @date 2026/2/16 02:08
 * @description MaterialButton with Ripple Effect
 */
class MaterialButton(
    context: Context,
    private val text: String,
    private val style: Style = Style.FILLED
) : View(context) {

    // 1. 实例化 RippleDrawable，传入 invalidate() 作为回调
    private val rippleDrawable = RippleDrawable { this.invalidate() }
    init {
        clickable = true
    }

    fun setOnClicked(action: () -> Unit) {
        onClickListener = OnClickListener {
            action()
        }
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
        val cornerRadius = 25.0

        // 绘制背景
        gc.fill = colors.first
        gc.fillRoundRect(0.0, 0.0, width, height, cornerRadius, cornerRadius)

        // 2. 配置并绘制 Ripple
        // 通常 Ripple 颜色与文本颜色一致（即前景色）
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
                // --- 核心修复：坐标转换 ---
                val (absX, absY) = getLocationInWindow()
                val localX = event.x - absX
                val localY = event.y - absY

                // 传入转换后的局部坐标
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
        return when (style) {
            Style.FILLED -> Color.web("#6750A4") to Color.WHITE
            Style.ELEVATED -> Color.web("#F6F0FF") to Color.web("#6750A4")
            Style.TONAL -> Color.web("#E8DEF8") to Color.web("#1D192B")
        }
    }

    // 根据文本颜色推断水波纹颜色（通常是文本颜色的半透明版）
    private fun getRippleColor(textColor: Color): Color {
        return textColor.deriveColor(0.0, 1.0, 1.0, 1.0) // 基础颜色，透明度在绘制时控制
    }

    enum class Style {
        FILLED, ELEVATED, TONAL
    }
}