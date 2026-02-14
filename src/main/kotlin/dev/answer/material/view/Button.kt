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
import dev.answer.material.graphics.StateDrawable
import javafx.scene.input.MouseEvent

/**
 *
 * @author AnswerDev
 * @date 2026/2/10 12:21
 * @description Button
 */

class Button(
    context: Context,
    private val drawable: Drawable? = null,
    private val text: String = ""
) : View(context) {

    private val stateDrawable: StateDrawable? = if (drawable is StateDrawable) drawable else null

    init {
        if (drawable != null && drawable !is StateDrawable) {
            background = drawable
        }

        // 设置点击监听器
        onClickListener = object : View.OnClickListener {
            override fun onClick(v: View) {
                // 处理点击事件
            }
        }
    }

    private fun updateButtonState() {
        stateDrawable?.let {
            background = it
        }
    }

    fun setOnClicked(action: () -> Unit) {
        onClickListener = object : View.OnClickListener {
            override fun onClick(v: View) {
                action()
            }
        }
    }

    override fun onTouchEvent(event: MouseEvent): Boolean {
        when (event.eventType) {
            MouseEvent.MOUSE_ENTERED -> {
                stateDrawable?.setState(StateDrawable.State.HOVER)
                updateButtonState()
                return true
            }
            MouseEvent.MOUSE_EXITED -> {
                stateDrawable?.setState(StateDrawable.State.NORMAL)
                updateButtonState()
                return true
            }
            MouseEvent.MOUSE_PRESSED -> {
                stateDrawable?.setState(StateDrawable.State.PRESSED)
                updateButtonState()
                return true
            }
            MouseEvent.MOUSE_RELEASED -> {
                stateDrawable?.setState(StateDrawable.State.NORMAL)
                updateButtonState()
                return true
            }
            else -> {}
        }
        return super.onTouchEvent(event)
    }
}