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

/**
 *
 * @author AnswerDev
 * @date 2026/2/17 02:11
 * @description Shapes
 */

data class Shapes(
    val extraSmall: Shape,
    val small: Shape,
    val medium: Shape,
    val large: Shape,
    val extraLarge: Shape,
    val full: Shape
) {

    companion object {

        fun default(): Shapes {
            return Shapes(
                extraSmall = Shape.rounded(4.0),
                small = Shape.rounded(8.0),
                medium = Shape.rounded(12.0),
                large = Shape.rounded(16.0),
                extraLarge = Shape.rounded(28.0),
                full = Shape.full()
            )
        }
    }
}
