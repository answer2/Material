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

/**
 *
 * @author AnswerDev
 * @date 2026/2/16 01:53
 * @description SlideAnimation
 */
class SlideAnimation(
    duration: Float,
    start: Float,
    end: Float,
    private val callback: (Float) -> Unit
) : AnimationExtra(duration, start, end) {

    override fun animate(x: Float): Float {
        // 这里实现 EaseInOut 效果
        return if (x < 0.5f) 2f * x * x else -1f + (4f - 2f * x) * x
    }

    override fun onInterpolate(value: Float) {
        // 将计算出的值应用到 UI
        callback(value)
    }
}