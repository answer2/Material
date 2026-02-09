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

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 22:38
 * @description StateDrawable
 */
class StateDrawable(
    private val normal: Drawable,
    private val hover: Drawable? = null,
    private val pressed: Drawable? = null,
    private val disabled: Drawable? = null
) : Drawable() {

    enum class State {
        NORMAL, HOVER, PRESSED, DISABLED
    }

    private var state: State = State.NORMAL

    fun setState(state: State) {
        this.state = state
    }

    override fun getImage() =
        when (state) {
            State.HOVER -> hover?.getImage() ?: normal.getImage()
            State.PRESSED -> pressed?.getImage() ?: normal.getImage()
            State.DISABLED -> disabled?.getImage() ?: normal.getImage()
            else -> normal.getImage()
        }
}