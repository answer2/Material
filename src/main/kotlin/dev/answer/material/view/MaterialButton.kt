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

import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import javafx.scene.text.Text

/**
 *
 * @author AnswerDev
 * @date 2026/2/15 02:03
 * @description MaterialButton
 */

    class MaterialButton(
        context: Context,
        private var text: String,
        private var style: Style = Style.FILLED
    ) : View(context) {

        // 按压动画控制器
        private val pressAnimation = PressAnimation()

        // 文本字体大小
        var textSize: Double = 16.0
            set(value) {
                field = value
                invalidate()
            }

        // 按钮圆角半径
        var cornerRadius: Double = 25.0

        // 水平内边距（文本与按钮边缘的距离）
        var horizontalPadding: Double = 24.0

        // 按钮固定高度（与原始 Java 代码一致）
        init {
            height = 40.0
            // 根据文本内容计算按钮宽度
            updateWidth()
            // 设置为可点击
            clickable = true
            // 默认背景为透明，我们将在 onDraw 中自行绘制
            background = null
        }

        /**
         * 当文本或字体变化时更新按钮宽度
         */
        private fun updateWidth() {
            val font = Font.font("System", textSize)
            val textNode = Text(text)
            textNode.font = font
            val bounds = textNode.layoutBounds
            width = bounds.width + horizontalPadding * 2
        }

        override fun onDraw(gc: GraphicsContext) {
            // 获取当前样式对应的颜色
            val colors = getColors()

            // 绘制圆角矩形背景（colors[0] 为背景色）
            gc.fill = colors[0]
            gc.fillRoundRect(0.0, 0.0, width, height, cornerRadius, cornerRadius)

            // 保存画布状态，用于裁剪按压动画区域
            gc.save()
            // 裁剪圆角区域，使按压动画也被裁剪为圆角
            gc.beginPath()
            gc.arc(0.0, 0.0, width, height, cornerRadius, cornerRadius) // 简化，实际应使用 roundRect 裁剪
            // JavaFX 裁剪路径可用 fillRoundRect 的路径，这里简化：直接使用矩形裁剪，但为了圆角，我们可以用 fillRoundRect 绘制覆盖层
            // 更好的方法：创建一个圆角矩形裁剪区域
            gc.beginPath()
            gc.moveTo(cornerRadius, 0.0)
            gc.lineTo(width - cornerRadius, 0.0)
            gc.quadraticCurveTo(width, 0.0, width, cornerRadius)
            gc.lineTo(width, height - cornerRadius)
            gc.quadraticCurveTo(width, height, width - cornerRadius, height)
            gc.lineTo(cornerRadius, height)
            gc.quadraticCurveTo(0.0, height, 0.0, height - cornerRadius)
            gc.lineTo(0.0, cornerRadius)
            gc.quadraticCurveTo(0.0, 0.0, cornerRadius, 0.0)
            gc.closePath()
            gc.clip()

            // 绘制按压动画（如果处于按下状态，绘制半透明覆盖层）
            pressAnimation.draw(gc, width, height, colors[1])

            // 恢复画布
            gc.restore()

            // 绘制文本
            gc.fill = colors[1] // 文本颜色
            val font = Font.font("System", textSize)
            gc.font = font

            val textNode = Text(text)
            textNode.font = font
            val bounds = textNode.layoutBounds

            // 计算文本居中位置
            val textX = (width - bounds.width) / 2
            val textY = (height + bounds.height) / 2 - bounds.minY // 垂直居中

            gc.fillText(text, textX, textY)
        }

        override fun onTouchEvent(event: MouseEvent): Boolean {
            // 先调用父类处理（触摸监听器等）
            val handled = super.onTouchEvent(event)
            if (handled) return true

            if (!clickable) return false

            when (event.eventType) {
                MouseEvent.MOUSE_PRESSED -> {
                    if (event.button == javafx.scene.input.MouseButton.PRIMARY) {
                        pressAnimation.onPressed(event.x, event.y)
                        invalidate()
                        return true
                    }
                }
                MouseEvent.MOUSE_RELEASED -> {
                    if (event.button == javafx.scene.input.MouseButton.PRIMARY) {
                        pressAnimation.onReleased()
                        invalidate()
                        // 触发点击事件
                        if (hitTest(event.sceneX, event.sceneY)) {
                            performClick()
                        }
                        return true
                    }
                }
                MouseEvent.MOUSE_EXITED -> {
                    pressAnimation.onReleased()
                    invalidate()
                }
                else -> {}
            }
            return false
        }

        /**
         * 获取当前样式对应的颜色对：[背景色, 内容色（文本/覆盖层）]
         */
        private fun getColors(): Array<Paint> {
            // 这里使用模拟的颜色管理器，实际应从全局 ColorManager 获取
            // 假设通过 context 可以获取到 Soar 实例
            val palette = Soar.getInstance().colorManager.palette
            return when (style) {
                Style.ELEVATED -> arrayOf(palette.surfaceContainerLow, palette.primary)
                Style.FILLED -> arrayOf(palette.primary, palette.onPrimary)
                Style.TONAL -> arrayOf(palette.secondaryContainer, palette.onSecondaryContainer)
            }
        }

        /**
         * 设置点击回调（简化方法）
         */
        fun setOnClicked(action: () -> Unit) {
            onClickListener = object : OnClickListener {
                override fun onClick(v: View) {
                    action()
                }
            }
        }

        /**
         * 按钮样式枚举
         */
        enum class Style {
            FILLED, ELEVATED, TONAL
        }

        /**
         * 简单的按压动画控制器：记录按压状态，在绘制时添加半透明覆盖层
         */
        inner class PressAnimation {
            private var isPressed = false
            private var pressX = 0.0
            private var pressY = 0.0

            fun onPressed(x: Double, y: Double) {
                isPressed = true
                pressX = x
                pressY = y
            }

            fun onReleased() {
                isPressed = false
            }

            fun draw(gc: GraphicsContext, width: Double, height: Double, color: Paint) {
                if (isPressed) {
                    // 绘制半透明覆盖层，alpha 固定为 0.12（与原代码一致）
                    gc.globalAlpha = 0.12
                    gc.fill = color
                    gc.fillRect(0.0, 0.0, width, height)
                    gc.globalAlpha = 1.0 // 恢复透明度
                }
            }
        }
    }

    // ========== 以下为模拟的 Soar 单例及颜色管理，实际项目中应替换为真实实现 ==========
    object Soar {
        private val instance = Soar
        fun getInstance(): Soar = instance

        val colorManager = ColorManager()
    }

    class ColorManager {
        val palette = Palette()
    }

    class Palette {
        val surfaceContainerLow: Paint = Color.web("#F5F5F5")
        val primary: Paint = Color.web("#6200EE")
        val onPrimary: Paint = Color.WHITE
        val secondaryContainer: Paint = Color.web("#E8DEF8")
        val onSecondaryContainer: Paint = Color.BLACK
    }