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
