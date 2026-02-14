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

package dev.answer.material.graphics

import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.paint.Color
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 22:37
 * @description Drawable
 */

abstract class Drawable {
    abstract fun getImage(): Image?
    open fun getImage(width: Double, height: Double): Image? = getImage()
    abstract fun draw(gc: GraphicsContext, x: Double, y: Double, w: Double, h: Double)
}

/**
 * Gravity类 - 用于控制绘制位置
 */
enum class Gravity {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    CENTER_LEFT,
    CENTER,
    CENTER_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT
}

class ColorDrawable(
    private val color: Color,
) : Drawable() {
    override fun getImage(): Image? = null

    fun getColor(): Color = color

    override fun draw(gc: GraphicsContext, x: Double, y: Double, w: Double, h: Double) {
        gc.fill = color
        gc.fillRect(x, y, w, h)
    }
}

class ImageDrawable private constructor(
    private val image: Image,
) : Drawable() {

    override fun getImage(): Image = image
    override fun draw(
        gc: GraphicsContext,
        x: Double,
        y: Double,
        w: Double,
        h: Double,
    ) {
        gc.drawImage(image, x, y, w, h)
    }

    fun getWidth() = image.width
    fun getHeight() = image.height

    companion object {
        private val cache = ConcurrentHashMap<String, ImageDrawable>()

        fun fromPath(path: String): ImageDrawable {
            return cache.getOrPut(path) {
                ImageDrawable(Image(path, true))
            }
        }

        fun fromImage(image: Image): ImageDrawable {
            return ImageDrawable(image)
        }

        fun clearCache() {
            cache.clear()
        }
    }
}

/**
 * 带有缩放和着色的Drawable包装器
 */
class ScaleDrawable(
    private val drawable: Drawable,
    private val scaleWidth: Double,
    private val scaleHeight: Double,
    private val gravity: Gravity = Gravity.CENTER,
) : Drawable() {
    override fun getImage(): Image? = drawable.getImage()
    override fun draw(
        gc: GraphicsContext,
        x: Double,
        y: Double,
        w: Double,
        h: Double,
    ) {
        // 计算缩放后的尺寸
        val scaledWidth = w * scaleWidth
        val scaledHeight = h * scaleHeight
        
        // 根据重力计算绘制位置
        val drawX = when (gravity) {
            Gravity.TOP_LEFT, Gravity.CENTER_LEFT, Gravity.BOTTOM_LEFT -> x
            Gravity.TOP_CENTER, Gravity.CENTER, Gravity.BOTTOM_CENTER -> x + (w - scaledWidth) / 2
            Gravity.TOP_RIGHT, Gravity.CENTER_RIGHT, Gravity.BOTTOM_RIGHT -> x + (w - scaledWidth)
        }
        
        val drawY = when (gravity) {
            Gravity.TOP_LEFT, Gravity.TOP_CENTER, Gravity.TOP_RIGHT -> y
            Gravity.CENTER_LEFT, Gravity.CENTER, Gravity.CENTER_RIGHT -> y + (h - scaledHeight) / 2
            Gravity.BOTTOM_LEFT, Gravity.BOTTOM_CENTER, Gravity.BOTTOM_RIGHT -> y + (h - scaledHeight)
        }
        
        // 绘制内部drawable
        drawable.draw(gc, drawX, drawY, scaledWidth, scaledHeight)
    }
}

/**
 * 状态选择器Drawable - 根据状态返回不同的Drawable
 */
class StateDrawable(
    private val normal: Drawable,
    private val hover: Drawable? = null,
    private val pressed: Drawable? = null,
    private val disabled: Drawable? = null,
) : Drawable() {

    enum class State {
        NORMAL, HOVER, PRESSED, DISABLED
    }

    private var state: State = State.NORMAL

    fun setState(state: State) {
        this.state = state
    }

    fun getState(): State = state

    override fun getImage(): Image? =
        when (state) {
            State.HOVER -> hover?.getImage() ?: normal.getImage()
            State.PRESSED -> pressed?.getImage() ?: normal.getImage()
            State.DISABLED -> disabled?.getImage() ?: normal.getImage()
            else -> normal.getImage()
        }

    override fun draw(
        gc: GraphicsContext,
        x: Double,
        y: Double,
        w: Double,
        h: Double,
    ) {
        // 根据当前状态选择相应的drawable进行绘制
        val currentDrawable = when (state) {
            State.HOVER -> hover ?: normal
            State.PRESSED -> pressed ?: normal
            State.DISABLED -> disabled ?: normal
            else -> normal
        }
        currentDrawable.draw(gc, x, y, w, h)
    }
}

/**
 * 九宫格Drawable - 用于可拉伸的背景
 */
class NinePatchDrawable(
    private val drawable: Drawable,
    private val left: Double,
    private val top: Double,
    private val right: Double,
    private val bottom: Double,
) : Drawable() {
    override fun getImage(): Image? = drawable.getImage()
    override fun draw(
        gc: GraphicsContext,
        x: Double,
        y: Double,
        w: Double,
        h: Double,
    ) {
        // 简单实现：这里假设drawable是一个ImageDrawable，实际应用中可能需要更复杂的处理
        // 计算九宫格的各个区域
        val image = drawable.getImage() ?: return
        val imageWidth = image.width
        val imageHeight = image.height
        
        // 确保边界值有效
        val safeLeft = minOf(left, imageWidth - 1)
        val safeTop = minOf(top, imageHeight - 1)
        val safeRight = minOf(right, imageWidth - safeLeft - 1)
        val safeBottom = minOf(bottom, imageHeight - safeTop - 1)
        
        // 计算拉伸区域
        val centerWidth = imageWidth - safeLeft - safeRight
        val centerHeight = imageHeight - safeTop - safeBottom
        
        // 计算目标区域的各个部分大小
        val targetLeft = safeLeft
        val targetTop = safeTop
        val targetRight = w - safeRight
        val targetBottom = h - safeBottom
        
        // 绘制四个角
        // 左上
        drawable.draw(gc, x, y, targetLeft, targetTop)
        // 右上
        drawable.draw(gc, x + targetRight, y, safeRight, targetTop)
        // 左下
        drawable.draw(gc, x, y + targetBottom, targetLeft, safeBottom)
        // 右下
        drawable.draw(gc, x + targetRight, y + targetBottom, safeRight, safeBottom)
        
        // 绘制四条边
        // 上边
        drawable.draw(gc, x + targetLeft, y, targetRight - targetLeft, targetTop)
        // 下边
        drawable.draw(gc, x + targetLeft, y + targetBottom, targetRight - targetLeft, safeBottom)
        // 左边
        drawable.draw(gc, x, y + targetTop, targetLeft, targetBottom - targetTop)
        // 右边
        drawable.draw(gc, x + targetRight, y + targetTop, safeRight, targetBottom - targetTop)
        
        // 绘制中心区域
        drawable.draw(gc, x + targetLeft, y + targetTop, targetRight - targetLeft, targetBottom - targetTop)
    }
    
    private fun minOf(a: Double, b: Double): Double = if (a < b) a else b
}