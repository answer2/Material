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
import javafx.animation.Animation
import javafx.animation.FadeTransition
import javafx.animation.ParallelTransition
import javafx.animation.RotateTransition
import javafx.animation.ScaleTransition
import javafx.animation.SequentialTransition
import javafx.animation.TranslateTransition
import javafx.util.Duration

/**
 *
 * @author AnswerDev
 * @date 2026/2/14 00:48
 * @description ViewAnimation
 */

/**
 * 动画工具类
 */
object ViewAnimation {

    /**
     * 创建淡入动画
     */
    fun fadeIn(view: View, duration: Duration = Duration.millis(300.0)): FadeTransition {
        val fadeTransition = FadeTransition(duration)
        fadeTransition.fromValue = 0.0
        fadeTransition.toValue = 1.0
        return fadeTransition
    }

    /**
     * 创建淡出动画
     */
    fun fadeOut(view: View, duration: Duration = Duration.millis(300.0)): FadeTransition {
        val fadeTransition = FadeTransition(duration)
        fadeTransition.fromValue = 1.0
        fadeTransition.toValue = 0.0
        return fadeTransition
    }

    /**
     * 创建平移动画
     */
    fun translate(view: View, fromX: Double, fromY: Double, toX: Double, toY: Double, duration: Duration = Duration.millis(300.0)): TranslateTransition {
        val translateTransition = TranslateTransition(duration)
        translateTransition.fromX = fromX
        translateTransition.fromY = fromY
        translateTransition.toX = toX
        translateTransition.toY = toY
        return translateTransition
    }

    /**
     * 创建缩放动画
     */
    fun scale(view: View, fromX: Double, fromY: Double, toX: Double, toY: Double, duration: Duration = Duration.millis(300.0)): ScaleTransition {
        val scaleTransition = ScaleTransition(duration)
        scaleTransition.fromX = fromX
        scaleTransition.fromY = fromY
        scaleTransition.toX = toX
        scaleTransition.toY = toY
        return scaleTransition
    }

    /**
     * 创建旋转动画
     */
    fun rotate(view: View, fromAngle: Double, toAngle: Double, duration: Duration = Duration.millis(300.0)): RotateTransition {
        val rotateTransition = RotateTransition(duration)
        rotateTransition.fromAngle = fromAngle
        rotateTransition.toAngle = toAngle
        return rotateTransition
    }

    /**
     * 创建并行动画
     */
    fun parallel(vararg animations: Animation): ParallelTransition {
        val parallelTransition = ParallelTransition()
        parallelTransition.children.addAll(*animations)
        return parallelTransition
    }

    /**
     * 创建序列动画
     */
    fun sequential(vararg animations: Animation): SequentialTransition {
        val sequentialTransition = SequentialTransition()
        sequentialTransition.children.addAll(*animations)
        return sequentialTransition
    }
}

/**
 * 动画监听器接口
 */
interface AnimationListener {
    fun onAnimationStart(animation: Animation)
    fun onAnimationEnd(animation: Animation)
    fun onAnimationRepeat(animation: Animation)
}

/**
 * 动画适配器
 */
open class AnimationAdapter : AnimationListener {
    override fun onAnimationStart(animation: Animation) {}
    override fun onAnimationEnd(animation: Animation) {}
    override fun onAnimationRepeat(animation: Animation) {}
}
