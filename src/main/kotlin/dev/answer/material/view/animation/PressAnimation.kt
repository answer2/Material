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

package dev.answer.material.view.animation

import dev.answer.material.view.View
import dev.answer.material.view.animation.cubicbezier.impl.EaseEmphasizedDecelerate
import javafx.animation.Animation
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import kotlin.math.pow
import kotlin.math.sqrt
/**
 *
 * @author AnswerDev
 * @date 2026/2/16 01:59
 * @description PressAnimation
 */

class PressAnimation(private val view: View) {

    private var progress = 0f
    private var stateEnd = 0f // 0=无状态, 1=按下, 2=释放
    private var pressedX = 0.0
    private var pressedY = 0.0

    private val duration = 0.6f

    // 保存动画引用以便取消
    private var currentAnimation: Animation? = null

    fun draw(
        gc: GraphicsContext,
        width: Double,
        height: Double,
        rippleColor: Color,
        baseAlpha: Double
    ) {
        if (stateEnd == 0f) return

        val resultAlpha = when (stateEnd) {
            1f -> progress * baseAlpha
            2f -> (2.0 - progress).coerceAtLeast(0.0) * baseAlpha
            else -> 0.0
        }

        if (resultAlpha <= 0.0) return

        val maxRadius = calculateMaxRadius(width, height) * 1.1
        val currentRadius = maxRadius * progress.coerceIn(0f, 1f)

        gc.fill = Color(
            rippleColor.red,
            rippleColor.green,
            rippleColor.blue,
            resultAlpha.coerceIn(0.0, 1.0)
        )

        gc.fillOval(
            pressedX - currentRadius,
            pressedY - currentRadius,
            currentRadius * 2,
            currentRadius * 2
        )
    }

    fun onPressed(mouseX: Double, mouseY: Double) {
        pressedX = mouseX
        pressedY = mouseY
        stateEnd = 1f
        progress = 0f

        // 取消之前的动画
        currentAnimation?.let { view.cancelAnimation(it) }

        currentAnimation = object : AnimationExtra(duration, 0f, 1f) {
            override fun animate(x: Float): Float {
                // 使用缓动函数实现平滑效果
                return if (x < 0.5f) 2f * x * x else -1f + (4f - 2f * x) * x
            }

            override fun onInterpolate(value: Float) {
                progress = value
                view.invalidate()
            }
        }.apply {
            // 设置动画结束监听器
            setOnFinished {
                // 如果动画结束时仍在按下状态，说明用户长时间按住
                if (stateEnd == 1f) {
                    stateEnd = 0f
                    progress = 0f
                }
            }
        }

        view.startAnimation(currentAnimation!!)
    }

    fun onReleased() {
        if (stateEnd != 1f) return
        stateEnd = 2f

        // 取消按下动画
        currentAnimation?.let { view.cancelAnimation(it) }

        currentAnimation = object : AnimationExtra(duration, progress, 2f) {
            override fun animate(x: Float): Float {
                // 释放动画使用线性插值
                return x
            }

            override fun onInterpolate(value: Float) {
                progress = value
                view.invalidate()
            }
        }.apply {
            setOnFinished {
                stateEnd = 0f
                progress = 0f
            }
        }

        view.startAnimation(currentAnimation!!)
    }

    // 新增：取消动画方法（用于处理拖出等场景）
    fun cancel() {
        currentAnimation?.let { view.cancelAnimation(it) }
        stateEnd = 0f
        progress = 0f
    }

    // 新增：检查是否处于按下状态
    fun isPressed(): Boolean = stateEnd == 1f

    private fun calculateMaxRadius(width: Double, height: Double): Double {
        return sqrt(width.pow(2) + height.pow(2))
    }
}
