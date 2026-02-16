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
import dev.answer.material.theme.ColorPalette
import dev.answer.material.view.View
import javafx.animation.Interpolator
import javafx.animation.Transition
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.util.Duration

/**
 *
 * @author AnswerDev
 * @date 2026/2/16 14:37
 * @description MaterialSwitch
 */

class MaterialSwitch(
    context: Context,
    private var checked: Boolean = false
) : View(context) {

    private var pressed = false
    private var focused = false

    private var enableProgress = if (checked) 1.0 else 0.0
    private var pressProgress = 0.0
    private var focusProgress = 0.0

    init {
        width = 52.0
        height = 32.0
        clickable = true
    }

    private fun animate(
        from: Double,
        to: Double,
        duration: Double,
        onUpdate: (Double) -> Unit
    ) {
        object : Transition() {
            init {
                cycleDuration = Duration.millis(duration)
                interpolator = Interpolator.EASE_BOTH
            }

            override fun interpolate(frac: Double) {
                val value = from + (to - from) * frac
                onUpdate(value)
                invalidate()
            }
        }.play()
    }

    private fun animateEnable(target: Boolean) {
        animate(enableProgress, if (target) 1.0 else 0.0, 150.0) {
            enableProgress = it
        }
    }

    private fun animatePress(target: Boolean) {
        animate(pressProgress, if (target) 1.0 else 0.0, 100.0) {
            pressProgress = it
        }
    }

    private fun animateFocus(target: Boolean) {
        animate(focusProgress, if (target) 1.0 else 0.0, 120.0) {
            focusProgress = it
        }
    }


    override fun onDraw(gc: GraphicsContext) {

        val radius = height

        val palette: ColorPalette = context.colorScheme

        val surface = palette.surface
        val outline = palette.outline
        val primary = palette.primary
        val primaryContainer = palette.primaryContainer
        val onSurface = palette.onSurface
        val onPrimary = palette.onPrimary

        // 背景轨道
        gc.fill = surface
        gc.fillRoundRect(0.0, 0.0, width, height, radius, radius)

        gc.stroke = outline
        gc.lineWidth = 2.0
        gc.strokeRoundRect(0.0, 0.0, width, height, radius, radius)

        // 启用填充
        gc.fill = primary.deriveColor(0.0, 1.0, 1.0, enableProgress)
        gc.fillRoundRect(0.0, 0.0, width, height, radius, radius)

        // 滑块位置
        val thumbX = 16 + (20 * enableProgress)
        val thumbY = 16.0

        val thumbRadius =
            8 + (enableProgress * 4) + (pressProgress * 1)

        val focusColor =
            if (checked) primaryContainer else outline

        val edgeColor =
            if (checked) onPrimary else outline

        val pressColor =
            if (checked) primary else onSurface

        // 主滑块
        gc.fill = edgeColor
        gc.fillOval(
            thumbX - thumbRadius,
            thumbY - thumbRadius,
            thumbRadius * 2,
            thumbRadius * 2
        )

        // hover 光晕
        gc.fill = focusColor.deriveColor(0.0, 1.0, 1.0, focusProgress)
        gc.fillOval(
            thumbX - thumbRadius,
            thumbY - thumbRadius,
            thumbRadius * 2,
            thumbRadius * 2
        )

        // 按压光晕
        val pressRadius =
            8 + (enableProgress * 4) + (pressProgress * 10)

        gc.fill = pressColor.deriveColor(0.0, 1.0, 1.0, pressProgress * 0.12)
        gc.fillOval(
            thumbX - pressRadius,
            thumbY - pressRadius,
            pressRadius * 2,
            pressRadius * 2
        )
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        minimumWidth= 52
        minimumHeight = 32

        super.onMeasure(widthSpec, heightSpec)
    }

    override fun onTouchEvent(event: MouseEvent): Boolean {

        when (event.eventType) {

            MouseEvent.MOUSE_ENTERED -> {
                animateFocus(true)
            }

            MouseEvent.MOUSE_EXITED -> {
                animateFocus(false)
            }

            MouseEvent.MOUSE_PRESSED -> {
                pressed = true
                animatePress(true)
                return true
            }

            MouseEvent.MOUSE_RELEASED -> {
                if (pressed) {
                    val newChecked = !checked
                    setChecked(newChecked)
                }

                pressed = false
                animatePress(false)
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    fun isChecked() = checked

    private var onCheckedChangeListener: ((Boolean) -> Unit)? = null

    fun setOnCheckedChangeListener(listener: (Boolean) -> Unit) {
        onCheckedChangeListener = listener
    }
    fun setChecked(value: Boolean, animate: Boolean = true) {
        if (checked == value) return

        checked = value

        if (animate) {
            animateEnable(value)
        } else {
            enableProgress = if (value) 1.0 else 0.0
            invalidate()
        }

        onCheckedChangeListener?.invoke(checked)
    }

}
