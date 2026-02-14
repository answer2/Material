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
import dev.answer.material.graphics.ImageDrawable
import dev.answer.material.view.ImageView
import dev.answer.material.view.LinearLayoutParams
import dev.answer.material.view.LinearView
import dev.answer.material.view.Orientation
import dev.answer.material.view.TextView
import javafx.scene.paint.Color

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 20:52
 * @description MainActivity
 */
class MainActivity : Activity() {

    override fun onCreate() {
        super.onCreate()

        println("MainActivity onCreate")

        setTitle("This an activity")
        setWindowSize(300.0, 300.0)

        val layout = LinearView(this, Orientation.VERTICAL)
        layout.background = ColorDrawable(Color.web("#3498DB"))
        layout.setPadding(16.0)

        val path = mResource.load("logo.png");
        val icon = ImageDrawable.Companion.fromPath(path)
println(path)

       val image = ImageView(this, icon);

        val title = TextView(this, "Hello Material").apply {
            // 这里假设TextView有setTextSize、setBold、setGravity等方法
            // 如果这些方法不存在，我们需要根据实际情况进行调整
        }

        layout.addView(
            title,
            LinearLayoutParams(
                width = 900,
                height = 900
            )
        )

        layout.addView(image,
            LinearLayoutParams(
                width = 500,
                height = 500
            )
        )
        setContentView(layout)

    }


    override fun onStart() {
        super.onStart()
        println("MainActivity onStart")
    }

}