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

package dev.answer.material.manager

import dev.answer.material.view.Fragment
import dev.answer.material.view.FragmentContainer
import java.util.Stack
/**
 *
 * @author AnswerDev
 * @date 2026/2/9 20:26
 * @description FragmentManager
 */


class FragmentManager(
    private val container: FragmentContainer
) {

    private val fragmentStack = Stack<Fragment>()

    fun replace(fragment: Fragment) {
        if (fragmentStack.isNotEmpty()) {
            val old = fragmentStack.pop()
            old.performDestroyView()
            container.children.clear()
        }

        fragmentStack.push(fragment)
        container.children += fragment.performCreateView()
    }

    fun push(fragment: Fragment) {
        if (fragmentStack.isNotEmpty()) {
            fragmentStack.peek().performDestroyView()
            container.children.clear()
        }

        fragmentStack.push(fragment)
        container.children += fragment.performCreateView()
    }

    fun pop() {
        if (fragmentStack.size <= 1) return

        val old = fragmentStack.pop()
        old.performDestroyView()
        container.children.clear()

        val top = fragmentStack.peek()
        container.children += top.performCreateView()
    }
}
