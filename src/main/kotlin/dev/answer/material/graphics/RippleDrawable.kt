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

package dev.answer.material.graphics
import dev.answer.material.theme.drawRoundedRect
import javafx.animation.Interpolator
import javafx.animation.Transition
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.util.Duration
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import kotlin.math.sqrt

/**
 *
 * @author AnswerDev
 * @date 2026/2/16 04:23
 * @description RippleDrawable
 */

class RippleDrawable(private val invalidateCallback: () -> Unit) {
    var color: Color = Color.BLACK
    var baseOpacity: Double = 0.25

    private val activeRipples = CopyOnWriteArrayList<RippleAnimation>()

    fun draw(gc: GraphicsContext, width: Double, height: Double, cornerRadius: Double = 0.0) {
        if (activeRipples.isEmpty()) return

        gc.save()

        // 1. 完善裁剪逻辑
        gc.beginPath()

        val radius = cornerRadius / 2
        gc.drawRoundedRect(0.0, 0.0, width, height,
            radius, radius, radius, radius)
        gc.clip()

        // 2. 绘制波纹
        for (ripple in activeRipples) {
            val alpha = baseOpacity * ripple.currentOpacityFactor
            if (alpha <= 0) continue

            gc.fill = color.deriveColor(0.0, 1.0, 1.0, alpha)
            gc.fillOval(
                ripple.x - ripple.radius,
                ripple.y - ripple.radius,
                ripple.radius * 2,
                ripple.radius * 2
            )
        }

        gc.restore()
    }


    fun trigger(x: Double, y: Double, containerWidth: Double, containerHeight: Double) {
        // 计算点击点到四个角的最远距离
        val dx1 = x
        val dx2 = containerWidth - x
        val dy1 = y
        val dy2 = containerHeight - y

        val maxRadius = sqrt(max(dx1 * dx1, dx2 * dx2) + max(dy1 * dy1, dy2 * dy2))

        // 稍微延长动画时间，增加视觉停留感 (从 400ms 增加到 550ms)
        val animation = RippleAnimation(x, y, maxRadius)
        activeRipples.add(animation)
        animation.play()
    }

    private inner class RippleAnimation(
        val x: Double,
        val y: Double,
        val targetRadius: Double,
    ) : Transition() {

        var radius: Double = 0.0
        var currentOpacityFactor: Double = 1.0

        init {
            cycleDuration = Duration.millis(550.0)
            // 使用更符合物理感的加速减速曲线
            interpolator = Interpolator.SPLINE(0.4, 0.0, 0.2, 1.0)

            setOnFinished {
                activeRipples.remove(this)
                invalidateCallback()
            }
        }

        override fun interpolate(frac: Double) {
            // 半径扩散：frac 映射到半径
            radius = targetRadius * frac

            // 透明度逻辑优化：
            // 0.0 -> 0.2: 快速淡入 (factor 从 0 到 1)
            // 0.2 -> 0.5: 保持最亮
            // 0.5 -> 1.0: 缓慢淡出
            currentOpacityFactor = when {
                frac < 0.2 -> frac / 0.2
                frac > 0.5 -> 1.0 - ((frac - 0.5) / 0.5)
                else -> 1.0
            }

            invalidateCallback()
        }
    }
}