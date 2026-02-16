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
import javafx.scene.shape.*

/**
 *
 * @author AnswerDev
 * @date 2026/2/17 02:10
 * @description Shape
 */

data class Shape(
    val topStart: CornerSize,
    val topEnd: CornerSize,
    val bottomEnd: CornerSize,
    val bottomStart: CornerSize
) {

    companion object {

        fun all(corner: CornerSize): Shape {
            return Shape(corner, corner, corner, corner)
        }

        fun rounded(radius: Double): Shape {
            return all(FixedCornerSize(radius))
        }

        fun full(): Shape {
            return all(PercentCornerSize(0.5))
        }
    }
}

fun Shape.toPath(
    x: Double,
    y: Double,
    width: Double,
    height: Double
): Path {

    val tl = topStart.toPx(width, height)
    val tr = topEnd.toPx(width, height)
    val br = bottomEnd.toPx(width, height)
    val bl = bottomStart.toPx(width, height)

    val path = Path()

    path.elements.add(MoveTo(x + tl, y))

    // Top line
    path.elements.add(LineTo(x + width - tr, y))
    if (tr > 0)
        path.elements.add(ArcTo(tr, tr, 0.0, x + width, y + tr, false, true))

    // Right line
    path.elements.add(LineTo(x + width, y + height - br))
    if (br > 0)
        path.elements.add(ArcTo(br, br, 0.0, x + width - br, y + height, false, true))

    // Bottom line
    path.elements.add(LineTo(x + bl, y + height))
    if (bl > 0)
        path.elements.add(ArcTo(bl, bl, 0.0, x, y + height - bl, false, true))

    // Left line
    path.elements.add(LineTo(x, y + tl))
    if (tl > 0)
        path.elements.add(ArcTo(tl, tl, 0.0, x + tl, y, false, true))

    path.elements.add(ClosePath())

    return path
}


fun Shape.createClip(width: Double, height: Double): Path {
    return toPath(0.0, 0.0, width, height)
}
