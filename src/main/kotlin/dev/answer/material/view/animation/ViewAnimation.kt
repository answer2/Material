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
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.ParallelTransition
import javafx.animation.SequentialTransition
import javafx.animation.Timeline
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
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
    fun fadeIn(view: View, duration: Duration = Duration.millis(300.0)): Timeline {
        val alphaProperty = SimpleDoubleProperty(view.alpha)
        alphaProperty.addListener { _, _, newValue -> view.alpha = newValue.toDouble() }
        
        return Timeline(
            KeyFrame(Duration.ZERO, KeyValue(alphaProperty, view.alpha)),
            KeyFrame(duration, KeyValue(alphaProperty, 1.0))
        )
    }

    /**
     * 创建淡出动画
     */
    fun fadeOut(view: View, duration: Duration = Duration.millis(300.0)): Timeline {
        val alphaProperty = SimpleDoubleProperty(view.alpha)
        alphaProperty.addListener { _, _, newValue -> view.alpha = newValue.toDouble() }
        
        return Timeline(
            KeyFrame(Duration.ZERO, KeyValue(alphaProperty, view.alpha)),
            KeyFrame(duration, KeyValue(alphaProperty, 0.0))
        )
    }

    /**
     * 创建平移动画
     */
    fun translate(view: View, fromX: Double, fromY: Double, toX: Double, toY: Double, duration: Duration = Duration.millis(300.0)): Timeline {
        val translateXProperty = SimpleDoubleProperty(view.translateX)
        val translateYProperty = SimpleDoubleProperty(view.translateY)
        
        translateXProperty.addListener { _, _, newValue -> view.translateX = newValue.toDouble() }
        translateYProperty.addListener { _, _, newValue -> view.translateY = newValue.toDouble() }
        
        return Timeline(
            KeyFrame(Duration.ZERO, 
                KeyValue(translateXProperty, fromX),
                KeyValue(translateYProperty, fromY)
            ),
            KeyFrame(duration, 
                KeyValue(translateXProperty, toX),
                KeyValue(translateYProperty, toY)
            )
        )
    }

    /**
     * 创建缩放动画
     */
    fun scale(view: View, fromX: Double, fromY: Double, toX: Double, toY: Double, duration: Duration = Duration.millis(300.0)): Timeline {
        val scaleXProperty = SimpleDoubleProperty(view.scaleX)
        val scaleYProperty = SimpleDoubleProperty(view.scaleY)
        
        scaleXProperty.addListener { _, _, newValue -> view.scaleX = newValue.toDouble() }
        scaleYProperty.addListener { _, _, newValue -> view.scaleY = newValue.toDouble() }
        
        return Timeline(
            KeyFrame(Duration.ZERO, 
                KeyValue(scaleXProperty, fromX),
                KeyValue(scaleYProperty, fromY)
            ),
            KeyFrame(duration, 
                KeyValue(scaleXProperty, toX),
                KeyValue(scaleYProperty, toY)
            )
        )
    }

    /**
     * 创建旋转动画
     */
    fun rotate(view: View, fromAngle: Double, toAngle: Double, duration: Duration = Duration.millis(300.0)): Timeline {
        val rotationProperty = SimpleDoubleProperty(view.rotation)
        rotationProperty.addListener { _, _, newValue -> view.rotation = newValue.toDouble() }
        
        return Timeline(
            KeyFrame(Duration.ZERO, KeyValue(rotationProperty, fromAngle)),
            KeyFrame(duration, KeyValue(rotationProperty, toAngle))
        )
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
