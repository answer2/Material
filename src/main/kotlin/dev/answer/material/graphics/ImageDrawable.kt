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

import javafx.scene.image.Image
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 22:37
 * @description ImageDrawable
 */
class ImageDrawable private constructor(
    private val image: Image
) : Drawable() {

    override fun getImage(): Image = image

    companion object {

        private val cache = ConcurrentHashMap<String, ImageDrawable>()

        fun fromPath(path: String): ImageDrawable {
            println(path)
            return cache.getOrPut(path) {
                ImageDrawable(
                    Image(path, true)
                )
            }
        }
    }
}