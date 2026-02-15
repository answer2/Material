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

import javafx.animation.Transition
import javafx.util.Duration
/**
 *
 * @author AnswerDev
 * @date 2026/2/15 21:21
 * @description AnimationExtra
 */
abstract class AnimationExtra(
    durationSeconds: Float, // 建议明确单位为秒
    protected val start: Float,
    protected val end: Float
) : Transition() {

    // 预先计算变化量，避免循环依赖
    protected val change: Float = end - start

    init {
        cycleDuration = Duration.seconds(durationSeconds.toDouble())
    }

    /**
     * JavaFX 会根据进度 (0.0 到 1.0) 自动调用此方法
     * @param frac 动画完成的比例 (0.0 ~ 1.0)
     */
    override fun interpolate(frac: Double) {
        // 这里的 frac 相当于你原来代码里的 timePassed / duration
        val value = animate(frac.toFloat()) * change + start
        onInterpolate(value)
    }



    /**
     * 定义缓动曲线 (Easing Function)
     * 例如：线性返回 x，平方返回 x*x
     */
    protected abstract fun animate(x: Float): Float

    /**
     * 子类在此处更新 UI 组件的状态（如 setOpacity, setTranslateX 等）
     */
    protected abstract fun onInterpolate(value: Float)

    fun getEndValue(): Float = end
}