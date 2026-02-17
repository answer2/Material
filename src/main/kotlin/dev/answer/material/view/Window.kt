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

import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.TextField
import javafx.scene.input.InputMethodEvent
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Pane
import javafx.stage.Stage

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 01:01
 * @description Window
 */
class Window {
    private lateinit var viewRoot: ViewRoot
    val stage = Stage()

    var width: Double = 0.0
    var height: Double = 0.0
    var title : String = "Title"
    lateinit var baseBridge : TextField;

    fun setContent(view: View) {
        viewRoot = ViewRoot(view)
        val hybridRoot = HybridRoot(viewRoot)
         baseBridge = hybridRoot.imeBridge;
        val scene = Scene(hybridRoot)
        stage.scene = scene

        hybridRoot.imeBridge.onInputMethodTextChanged =   EventHandler<InputMethodEvent> { event ->
           viewRoot.rootView.dispatchInputMethodEvent(event)
        }

        val onKeyEvent =  EventHandler<KeyEvent> { event ->
            viewRoot.rootView.dispatchKeyEvent(event)
        }

        hybridRoot.imeBridge.onKeyTyped = onKeyEvent
        hybridRoot.imeBridge.onKeyPressed = onKeyEvent
        hybridRoot.imeBridge.onKeyReleased = onKeyEvent

    }

    fun show() {
        if (width > 0) stage.width = width
        if (height > 0) stage.height = height
        stage.title = title;
        stage.show()
    }

    fun close() {
        if (::viewRoot.isInitialized) {
            viewRoot.destroy()
        }

        stage.close()
    }
}
