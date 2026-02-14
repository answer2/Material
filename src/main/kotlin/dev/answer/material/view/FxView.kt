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

import dev.answer.material.content.Context
import javafx.scene.Node
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.Pane

/**
 *
 * @author AnswerDev
 * @date 2026/2/10 13:52
 * @description FxView
 */
class FxView(
    context: Context,
    val node: Node
) : View(context) {

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)

        val prefW = node.prefWidth(-1.0)
        val prefH = node.prefHeight(-1.0)

        measuredWidth = if (layoutParams.width >= 0)
            layoutParams.width.toDouble() else prefW

        measuredHeight = if (layoutParams.height >= 0)
            layoutParams.height.toDouble() else prefH
    }

    override fun onLayout(l: Double, t: Double, r: Double, b: Double) {
        super.onLayout(l, t, r, b)
        node.resizeRelocate(
            l,
            t,
            width,
            height
        )
    }

    override fun onDraw(gc: GraphicsContext) {
    }
}

class HybridRoot(
    private val viewRoot: ViewRoot
) : Pane() {

    init {
        children += viewRoot
        viewRoot.widthProperty().bind(widthProperty())
        viewRoot.heightProperty().bind(heightProperty())
    }

    fun addFxNode(node: Node) {
        children += node
    }
}

