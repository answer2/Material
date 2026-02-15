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

package dev.answer.material.view.animation.cubicbezier

import dev.answer.material.view.animation.AnimationExtra

/**
 *
 * @author AnswerDev
 * @date 2026/2/15 21:18
 * @description CubicBezierAnimation
 */
open class CubicBezier(
    private val x1: Float,
    private val y1: Float,
    private val x2: Float,
    private val y2: Float,
    duration: Float,
    start: Float,
    end: Float
) : AnimationExtra(duration, start, end) {

    companion object {
        private const val CUBIC_ERROR_BOUND = 0.001f
    }

    override fun animate(x: Float): Float {
        var start = 0.0f
        var end = 1.0f

        while (true) {
            val midpoint = (start + end) / 2
            val estimate = evaluateCubic(x1, x2, midpoint)

            if (kotlin.math.abs(x - estimate) < CUBIC_ERROR_BOUND) {
                return evaluateCubic(y1, y2, midpoint)
            }

            if (estimate < x) {
                start = midpoint
            } else {
                end = midpoint
            }
        }
    }

    override fun onInterpolate(value: Float) {

    }

    private fun evaluateCubic(a: Float, b: Float, m: Float): Float {
        return 3 * a * (1 - m) * (1 - m) * m +
                3 * b * (1 - m) * m * m +
                m * m * m
    }
}
