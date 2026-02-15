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

package dev.answer.material.view

import dev.answer.material.view.measure.MeasureSpec
import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseEvent

/**
 *
 * @author AnswerDev
 * @date 2026/2/10 13:47
 * @description ViewRoot
 */

class ViewRoot(
    private val rootView: View
) : Canvas() {

    init {
        attachRoot()

        widthProperty().addListener { _, _, _ -> requestLayoutAndDraw() }
        heightProperty().addListener { _, _, _ -> requestLayoutAndDraw() }

        addEventHandler(MouseEvent.ANY) { event ->
            rootView.dispatchTouchEvent(event)
        }
    }

    /**
     * 绑定 invalidate 和 requestLayout 到 ViewRoot
     */
    private fun attachRoot() {
        rootView.parent = object : View(rootView.context) {

            override fun invalidate() {
                draw()
            }

            override fun requestLayout() {
                requestLayoutAndDraw()
            }
        }
    }

    private fun requestLayoutAndDraw() {
        if (width <= 0 || height <= 0) return

        val widthSpec =
            MeasureSpec.makeMeasureSpec(width.toInt(), MeasureSpec.EXACTLY)
        val heightSpec =
            MeasureSpec.makeMeasureSpec(height.toInt(), MeasureSpec.EXACTLY)

        rootView.measure(widthSpec, heightSpec)

        rootView.layout(
            0.0,
            0.0,
            rootView.measuredWidth,
            rootView.measuredHeight
        )

        draw()
    }

    private fun draw() {
        val gc = graphicsContext2D
        gc.clearRect(0.0, 0.0, width, height)
        rootView.draw(gc)
    }
}
