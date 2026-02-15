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

package dev.answer.test.activity

import dev.answer.material.content.Activity
import dev.answer.material.graphics.ColorDrawable
import dev.answer.material.view.Button
import dev.answer.material.view.LayoutParams
import dev.answer.material.view.LinearView
import dev.answer.material.view.MaterialButton
import dev.answer.material.view.Orientation
import dev.answer.material.view.TextView
import dev.answer.material.view.View
import dev.answer.material.view.animation.AnimationListener
import dev.answer.material.view.animation.ViewAnimation
import javafx.animation.Animation
import javafx.scene.paint.Color
import javafx.util.Duration

/**
 *
 * @author AnswerDev
 * @date 2026/2/10 13:29
 * @description TestActivity
 */
class TestActivity : Activity() {
    
    private lateinit var animationView: View
    
    override fun onCreate() {
        super.onCreate()

        setWindowSize(400.0, 600.0)

        val layout = LinearView(this, Orientation.VERTICAL)
        layout.layoutParams  = LayoutParams(width = 400, height = 600)
        layout.background = ColorDrawable(Color.web("#2C3E50"))
        layout.spacing = 10.0
        layout.setPadding(10.0)

        animationView = LinearView(this).apply {
            background = ColorDrawable(Color.web("#E74C3C"))
            layoutParams = LayoutParams(width = 100, height = 100)
            onClickListener = View.OnClickListener { view ->
                println("按钮被点击: $view")
            }
        }

        val statusText = TextView(this).apply {
            text = "Animation Test"
            textSize = 20.0
            textColor = Color.WHITE
            layoutParams = LayoutParams(width = LayoutParams.WRAP_CONTENT, height = LayoutParams.WRAP_CONTENT)
            onClickListener = View.OnClickListener { view ->
                println("按钮被点击: $view")
            }
        }

        val fadeButton = Button(this, text = "Fade In/Out").apply {
            layoutParams = LayoutParams(width = LayoutParams.WRAP_CONTENT, height = LayoutParams.WRAP_CONTENT)
            setOnClicked {
                if (animationView.alpha > 0.5) {
                    val anim = ViewAnimation.fadeOut(animationView, Duration.millis(500.0))
                    startAnimation(anim, object : AnimationListener {
                        override fun onAnimationStart(animation: Animation) {
                            statusText.text = "Fade Out Started"
                        }
                        override fun onAnimationEnd(animation: Animation) {
                            statusText.text = "Fade Out Completed"
                        }
                        override fun onAnimationRepeat(animation: Animation) {}
                    })
                } else {
                    val anim = ViewAnimation.fadeIn(animationView, Duration.millis(500.0))
                    startAnimation(anim, object : AnimationListener {
                        override fun onAnimationStart(animation: Animation) {
                            statusText.text = "Fade In Started"
                        }
                        override fun onAnimationEnd(animation: Animation) {
                            statusText.text = "Fade In Completed"
                        }
                        override fun onAnimationRepeat(animation: Animation) {}
                    })
                }
            }
        }

        val translateButton = Button(this, text = "Translate").apply {
            layoutParams =LayoutParams(width = LayoutParams.WRAP_CONTENT, height = LayoutParams.WRAP_CONTENT)
            setOnClicked {
                val anim = ViewAnimation.translate(
                    animationView, 
                    animationView.translateX, 
                    animationView.translateY, 
                    animationView.translateX + 50, 
                    animationView.translateY + 50, 
                    Duration.millis(500.0)
                )
                startAnimation(anim, object : AnimationListener {
                    override fun onAnimationStart(animation: Animation) {
                        statusText.text = "Translate Started"
                    }
                    override fun onAnimationEnd(animation: Animation) {
                        statusText.text = "Translate Completed"
                    }
                    override fun onAnimationRepeat(animation: Animation) {}
                })
            }
        }

        val scaleButton = Button(this, text = "Scale").apply {
            layoutParams = LayoutParams(width = LayoutParams.WRAP_CONTENT, height = LayoutParams.WRAP_CONTENT)
            setOnClicked {
                val targetScale = if (animationView.scaleX > 1.5) 1.0 else 2.0
                val anim = ViewAnimation.scale(
                    animationView, 
                    animationView.scaleX, 
                    animationView.scaleY, 
                    targetScale, 
                    targetScale, 
                    Duration.millis(500.0)
                )
                startAnimation(anim, object : AnimationListener {
                    override fun onAnimationStart(animation: Animation) {
                        statusText.text = "Scale Started"
                    }
                    override fun onAnimationEnd(animation: Animation) {
                        statusText.text = "Scale Completed"
                    }
                    override fun onAnimationRepeat(animation: Animation) {}
                })
            }
        }

        val rotateButton = Button(this, text = "Rotate").apply {
            layoutParams = LayoutParams(width = LayoutParams.WRAP_CONTENT, height = LayoutParams.WRAP_CONTENT)
            setOnClicked {
                val anim = ViewAnimation.rotate(
                    animationView, 
                    animationView.rotation, 
                    animationView.rotation + 90, 
                    Duration.millis(500.0)
                )
                startAnimation(anim, object : AnimationListener {
                    override fun onAnimationStart(animation: Animation) {
                        statusText.text = "Rotate Started"
                    }
                    override fun onAnimationEnd(animation: Animation) {
                        statusText.text = "Rotate Completed"
                    }
                    override fun onAnimationRepeat(animation: Animation) {}
                })
            }
        }

        val resetButton = Button(this, text = "Reset").apply {
            layoutParams =LayoutParams(width = LayoutParams.WRAP_CONTENT, height = LayoutParams.WRAP_CONTENT)
            setOnClicked {
                animationView.alpha = 1.0
                animationView.translateX = 0.0
                animationView.translateY = 0.0
                animationView.scaleX = 1.0
                animationView.scaleY = 1.0
                animationView.rotation = 0.0
                statusText.text = "Reset Completed"
            }
        }

       layout.addView(animationView)
        layout.addView(statusText)
        layout.addView(fadeButton)
       layout.addView(translateButton)
        layout.addView(scaleButton)
        layout.addView(rotateButton)
       layout.addView(resetButton)

        setContentView(layout)
    }
}
