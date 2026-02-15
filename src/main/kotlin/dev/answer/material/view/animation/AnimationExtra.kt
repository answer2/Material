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
    duration: Float,
    protected val start: Float,
    protected val end: Float,
) : Transition() {

    init {
        cycleDuration = Duration.seconds(duration.toDouble())
    }

    protected val change: Float = getEndValue() - start

    override fun interpolate(frac: Double) {
        val value = animate(frac.toFloat()) * change + start
        onInterpolate(value)
    }


    protected abstract fun animate(x: Float): Float

    // 子类需要实现的更新方法
    protected abstract fun onInterpolate(value: Float)

    fun getEndValue(): Float = start + change
}