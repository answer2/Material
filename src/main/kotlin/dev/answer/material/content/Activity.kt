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

package dev.answer.material.content

import dev.answer.material.manager.ActivityManager
import dev.answer.material.view.View
import dev.answer.material.view.Window

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 00:45
 * @description Activity
 */
 abstract class Activity : ContextImpl() {

    private val mWindow = Window()

    protected open fun onCreate() {}
    protected open fun onStart() {}
    protected open fun onStop() {}
    protected open fun onDestroy() {}

    protected fun setTitle(title : String){
        mWindow.title = title
    }

    protected fun setContentView(view: View) {
        mWindow.setContent(view)
    }

    protected fun setWindowSize(w: Double, h: Double) {
        mWindow.width = w
        mWindow.height = h
    }

    internal fun performCreate() = onCreate()

    internal fun performStart() {
        onStart()
        mWindow.show()
    }

    internal fun performStop() {
        onStop()
    }

    internal fun performDestroy() {
        onDestroy()
        mWindow.close()
    }

    override fun finish() {
        ActivityManager.finishActivity(this)
    }
}