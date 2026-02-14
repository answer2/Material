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

/**
 *
 * @author AnswerDev
 * @date 2026/2/14 00:48
 * @description GroupView
 */

open class GroupView(context: Context) : View(context) {

    protected val childrenViews: MutableList<View> = mutableListOf()

    open fun addView(view: View, params: LayoutParams) {
        view.layoutParams = params
        childrenViews.add(view)
        addView(view)
        view.onAttach()
    }

    override fun addView(child: View) {
        super.addView(child)
        childrenViews.add(child)
    }

    override fun removeView(child: View) {
        super.removeView(child)
        childrenViews.remove(child)
        child.onDetach()
    }

    override fun removeAllViews() {
        for (child in childrenViews) {
            child.onDetach()
        }
        childrenViews.clear()
        super.removeAllViews()
    }

    open fun getChildCount(): Int {
        return childrenViews.size
    }

    open fun getChildAt(index: Int): View? {
        return if (index >= 0 && index < childrenViews.size) {
            childrenViews[index]
        } else {
            null
        }
    }
}
