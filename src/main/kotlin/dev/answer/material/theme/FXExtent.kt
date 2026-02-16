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

package dev.answer.material.theme

import javafx.scene.canvas.GraphicsContext
import javafx.scene.shape.ArcTo
import javafx.scene.shape.ClosePath
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo

/**
 *
 * @author AnswerDev
 * @date 2026/2/17 02:12
 * @description FXExtent
 */

fun GraphicsContext.drawRoundedRect(
    x: Double,
    y: Double,
    width: Double,
    height: Double,
    tl: Double,
    tr: Double,
    br: Double,
    bl: Double
) {
    beginPath()

    moveTo(x + tl, y)

    // Top
    lineTo(x + width - tr, y)
    if (tr > 0) quadraticCurveTo(x + width, y, x + width, y + tr)

    // Right
    lineTo(x + width, y + height - br)
    if (br > 0) quadraticCurveTo(x + width, y + height, x + width - br, y + height)

    // Bottom
    lineTo(x + bl, y + height)
    if (bl > 0) quadraticCurveTo(x, y + height, x, y + height - bl)

    // Left
    lineTo(x, y + tl)
    if (tl > 0) quadraticCurveTo(x, y, x + tl, y)

    closePath()
}

fun GraphicsContext.drawShape(
    shape: Shape,
    x: Double,
    y: Double,
    width: Double,
    height: Double
) {
    val tl = shape.topStart.toPx(width, height)
    val tr = shape.topEnd.toPx(width, height)
    val br = shape.bottomEnd.toPx(width, height)
    val bl = shape.bottomStart.toPx(width, height)

    beginPath()

    moveTo(x + tl, y)

    // Top
    lineTo(x + width - tr, y)
    if (tr > 0)
        quadraticCurveTo(x + width, y, x + width, y + tr)

    // Right
    lineTo(x + width, y + height - br)
    if (br > 0)
        quadraticCurveTo(x + width, y + height, x + width - br, y + height)

    // Bottom
    lineTo(x + bl, y + height)
    if (bl > 0)
        quadraticCurveTo(x, y + height, x, y + height - bl)

    // Left
    lineTo(x, y + tl)
    if (tl > 0)
        quadraticCurveTo(x, y, x + tl, y)

    closePath()
}
