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
import dev.answer.material.view.Orientation
import dev.answer.material.view.TextView
import dev.answer.material.view.View
import javafx.scene.paint.Color

/**
 *
 * @author AnswerDev
 * @date 2026/2/10 13:29
 * @description TestActivity
 */
class TestActivity : Activity() {
    override fun onCreate() {
        super.onCreate()

        setWindowSize(400.0, 600.0)

        val layout = LinearView(this, Orientation.VERTICAL)
        layout.layoutParams  = LayoutParams(width = 400, height = 600)
        layout.background = ColorDrawable(Color.web("#FF6B6B"))  // 红色，看看大小
        layout.spacing = 10.0
        layout.setPadding(10.0)

        val item1 = View(this).apply {
            background = ColorDrawable(Color.web("#3498DB"))
            layoutParams = LayoutParams(width = 100, height = 100)
        }

        val item2 = View(this).apply {
            background = ColorDrawable(Color.web("#2ECC71"))
            layoutParams = LayoutParams(width = 100, height = 100)
        }

        val textview = TextView(this).apply {
            text = "Hello"
            textSize =40.0
            textColor = (Color.web("#FFFFFF"))
            layoutParams = LayoutParams(width = LayoutParams.WRAP_CONTENT, height = LayoutParams.WRAP_CONTENT)
        }

        val button = Button(this, text = "Hello").apply {
            layoutParams = LayoutParams(width = LayoutParams.WRAP_CONTENT, height = LayoutParams.WRAP_CONTENT)
        }

        layout.addView(item1)
        layout.addView(item2)
        layout.addView(textview)
        layout.addView(button)


        setContentView(layout)
    }
}