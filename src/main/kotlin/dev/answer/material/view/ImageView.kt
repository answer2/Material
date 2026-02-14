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
import dev.answer.material.graphics.Drawable
import dev.answer.material.graphics.ImageDrawable

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 22:39
 * @description ImageView
 */


class ImageView(
    context: Context,
    drawable: Drawable? = null
) : View(context) {

    private var imageDrawable: Drawable? = null
    private var imageWidth: Double = 50.0
    private var imageHeight: Double = 50.0

    init {
        if (drawable != null) {
            setDrawable(drawable)
        }
    }

    fun setDrawable(drawable: Drawable) {
        imageDrawable = drawable
        requestLayout()
        invalidate()
    }

    fun setImageSize(width: Double, height: Double) {
        imageWidth = width
        imageHeight = height
        requestLayout()
        invalidate()
    }

    fun setImageDimension(width: Int, height: Int) {
        setImageSize(width.toDouble(), height.toDouble())
    }

    fun getDrawable(): Drawable? = imageDrawable

    override fun onDraw(gc: javafx.scene.canvas.GraphicsContext) {
        super.onDraw(gc)
        val drawable = imageDrawable ?: return
        drawable.draw(gc, left, top, width, height)
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        if (imageDrawable != null) {
            measuredWidth = imageWidth
            measuredHeight = imageHeight
        }
    }
}
