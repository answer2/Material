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

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.effect.BoxBlur
import javafx.scene.effect.DropShadow
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
/**
 *
 * @author AnswerDev
 * @date 2026/2/15 21:07
 * @description FXRender
 */

object FXRender {

    private lateinit var gc: GraphicsContext
    private val imageCache = HashMap<String, Image>()

    fun init(canvas: Canvas) {
        gc = canvas.graphicsContext2D
    }

    fun context(): GraphicsContext {
        check(::gc.isInitialized) { "FXRender not initialized!" }
        return gc
    }

    fun drawRect(x: Double, y: Double, w: Double, h: Double, color: Color) {
        gc.fill = color
        gc.fillRect(x, y, w, h)
    }

    fun drawRoundedRect(
        x: Double,
        y: Double,
        w: Double,
        h: Double,
        arc: Double,
        color: Color
    ) {
        gc.fill = color
        gc.fillRoundRect(x, y, w, h, arc, arc)
    }

    fun drawCircle(x: Double, y: Double, radius: Double, color: Color) {
        gc.fill = color
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2)
    }

    fun drawLine(
        x1: Double,
        y1: Double,
        x2: Double,
        y2: Double,
        width: Double,
        color: Color
    ) {
        gc.stroke = color
        gc.lineWidth = width
        gc.strokeLine(x1, y1, x2, y2)
    }

    // =============================
    // 文本
    // =============================

    fun drawText(
        text: String,
        x: Double,
        y: Double,
        size: Double,
        color: Color
    ) {
        gc.fill = color
        gc.font = Font(size)
        gc.fillText(text, x, y)
    }

    fun drawCenteredText(
        text: String,
        x: Double,
        y: Double,
        size: Double,
        color: Color
    ) {
        gc.fill = color
        gc.font = Font(size)

        val oldAlign = gc.textAlign
        gc.textAlign = TextAlignment.CENTER
        gc.fillText(text, x, y)
        gc.textAlign = oldAlign
    }

    fun drawImage(
        path: String,
        x: Double,
        y: Double,
        w: Double,
        h: Double
    ) {
        val image = imageCache.getOrPut(path) {
            Image(FXRender::class.java.getResourceAsStream(path))
        }

        gc.drawImage(image, x, y, w, h)
    }

    fun applyBlur(width: Double, height: Double) {
        val blur = BoxBlur(width, height, 3)
        gc.applyEffect(blur)
    }

    fun applyShadow(radius: Double, color: Color) {
        val shadow = DropShadow().apply {
            this.radius = radius
            this.color = color
        }
        gc.applyEffect(shadow)
    }

    fun save() = gc.save()

    fun restore() = gc.restore()

    fun translate(x: Double, y: Double) = gc.translate(x, y)

    fun rotate(angle: Double) = gc.rotate(angle)

    fun scale(sx: Double, sy: Double) = gc.scale(sx, sy)

    fun clip(x: Double, y: Double, w: Double, h: Double) {
        gc.beginPath()
        gc.rect(x, y, w, h)
        gc.closePath()
        gc.clip()
    }
}
