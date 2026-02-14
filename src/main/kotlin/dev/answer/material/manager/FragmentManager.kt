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

import dev.answer.material.view.FragmentContainer
import java.util.Stack
import javafx.scene.layout.StackPane
import javafx.scene.layout.Pane
/**
 *
 * @author AnswerDev
 * @date 2026/2/9 20:26
 * @description FragmentManager
 */
abstract class Fragment : Pane() {

    // 生命周期状态
    enum class State {
        CREATED,      // 已创建但未初始化
        INITIALIZED,  // 已初始化
        STARTED,      // 已启动（即将显示）
        RESUMED,      // 已恢复（正在显示）
        PAUSED,       // 已暂停（即将隐藏）
        STOPPED,      // 已停止（已隐藏）
        DESTROYED     // 已销毁
    }

    private var state: State = State.CREATED
    private var isViewCreated = false

    /**
     * 获取当前生命周期状态
     */
    fun getState(): State = state

    /**
     * Fragment 被创建时调用（仅一次）
     * 在这里做初始化工作，比如读取 arguments
     */
    open fun onCreate() {
        state = State.INITIALIZED
    }

    /**
     * 创建 Fragment 的视图 - 必须实现
     * @return 该 Fragment 的根视图
     */
    abstract fun onCreateView(): Pane

    /**
     * 视图创建后调用
     * 在这里做视图相关的初始化，比如绑定事件
     */
    open fun onViewCreated() {
        state = State.STARTED
    }

    /**
     * Fragment 变得可见时调用
     * 在这里做恢复工作，比如启动动画、刷新数据
     */
    open fun onResume() {
        state = State.RESUMED
    }

    /**
     * Fragment 即将隐藏时调用
     * 在这里做暂停工作，比如停止动画、保存状态
     */
    open fun onPause() {
        state = State.PAUSED
    }

    /**
     * Fragment 已隐藏时调用
     * 在这里做停止工作，比如停止网络请求
     */
    open fun onStop() {
        state = State.STOPPED
    }

    /**
     * Fragment 被销毁时调用
     * 在这里做清理工作，比如释放资源
     */
    open fun onDestroy() {
        state = State.DESTROYED
    }

    /**
     * 视图销毁时调用
     * 在这里清理视图相关资源
     */
    open fun onDestroyView() {
        isViewCreated = false
    }

    // ============ 内部生命周期方法（由 FragmentManager 调用）============

    /**
     * 内部：执行创建视图的完整流程
     */
    internal fun performCreateView(): Pane {
        if (!isViewCreated) {
            // 第一次创建时调用 onCreate
            if (state == State.CREATED) {
                onCreate()
            }

            // 创建视图
            val view = onCreateView()
            children.clear()
            children += view

            // 标记视图已创建
            isViewCreated = true

            // 调用视图创建后的回调
            onViewCreated()
        }

        // 恢复视图
        if (state == State.STOPPED || state == State.PAUSED) {
            onResume()
        }

        return this
    }

    /**
     * 内部：执行销毁视图的完整流程
     */
    internal fun performDestroyView() {
        if (isViewCreated) {
            // 暂停
            if (state == State.RESUMED) {
                onPause()
            }

            // 停止
            if (state != State.STOPPED) {
                onStop()
            }

            // 销毁视图
            onDestroyView()
            children.clear()
        }
    }

    /**
     * 内部：完全销毁 Fragment
     */
    internal fun performDestroy() {
        performDestroyView()
        if (state != State.DESTROYED) {
            onDestroy()
        }
    }

    /**
     * 向 Fragment 传递参数
     */
    fun setArguments(block: Arguments.() -> Unit) {
        Arguments.apply(block)
    }

    /**
     * 获取传递给 Fragment 的参数
     */
    fun getArguments(): Arguments = Arguments
}

/**
 * Fragment 参数容器
 */
object Arguments {
    private val data = mutableMapOf<String, Any?>()

    fun put(key: String, value: Any?) {
        data[key] = value
    }

    fun get(key: String): Any? = data[key]
    fun getString(key: String): String? = get(key) as? String
    fun getInt(key: String): Int? = get(key) as? Int
    fun getBoolean(key: String): Boolean? = get(key) as? Boolean

    fun clear() {
        data.clear()
    }
}

// ============ Fragment Container ============

/**
 * Fragment 容器 - 用来承载 Fragment
 */
class FragmentContainer : StackPane()

// ============ Fragment Manager ============

/**
 * Fragment 管理器 - 管理 Fragment 的生命周期和栈
 *
 * 支持的操作：
 * - replace(fragment): 替换当前 Fragment（返回时销毁前一个）
 * - push(fragment): 压入新 Fragment（返回时恢复前一个）
 * - pop(): 弹出当前 Fragment（返回到上一个）
 * - backstack: 获取当前栈信息
 */
class FragmentManager(
    private val container: FragmentContainer
) {

    // Fragment 栈
    private val fragmentStack = Stack<Fragment>()

    // 栈操作监听器
    private val listeners = mutableListOf<StackChangeListener>()

    interface StackChangeListener {
        fun onFragmentAdded(fragment: Fragment)
        fun onFragmentRemoved(fragment: Fragment)
        fun onStackChanged(size: Int)
    }

    fun addStackChangeListener(listener: StackChangeListener) {
        listeners += listener
    }

    fun removeStackChangeListener(listener: StackChangeListener) {
        listeners -= listener
    }

    /**
     * 替换当前 Fragment
     *
     * 行为：
     * - 销毁当前 Fragment
     * - 压入新 Fragment
     * - 返回时不会恢复之前的 Fragment
     *
     * 适用场景：登录后切换到主页、跳转到新的一级页面
     */
    fun replace(fragment: Fragment) {
        // 1. 销毁旧 Fragment
        if (fragmentStack.isNotEmpty()) {
            val old = fragmentStack.pop()
            old.performDestroy()  // ← 完全销毁，不是仅销毁视图
        }

        // 2. 压入新 Fragment
        fragmentStack.push(fragment)

        // 3. 创建新 Fragment 的视图
        val view = fragment.performCreateView()
        container.children.clear()
        container.children += view

        // 4. 通知监听器
        listeners.forEach { it.onFragmentAdded(fragment) }
        listeners.forEach { it.onStackChanged(fragmentStack.size) }
    }

    /**
     * 压入新 Fragment
     *
     * 行为：
     * - 暂停当前 Fragment（不销毁）
     * - 压入新 Fragment
     * - 返回时会恢复当前 Fragment
     *
     * 适用场景：打开详情页、显示弹窗、进入子界面
     */
    fun push(fragment: Fragment) {
        // 1. 暂停当前 Fragment
        if (fragmentStack.isNotEmpty()) {
            val current = fragmentStack.peek()
            current.onPause()
            current.onStop()
            // ← 注意：不销毁视图，只是暂停
            container.children.clear()
        }

        // 2. 压入新 Fragment
        fragmentStack.push(fragment)

        // 3. 创建新 Fragment 的视图
        val view = fragment.performCreateView()
        container.children += view

        // 4. 通知监听器
        listeners.forEach { it.onFragmentAdded(fragment) }
        listeners.forEach { it.onStackChanged(fragmentStack.size) }
    }

    /**
     * 弹出当前 Fragment
     *
     * 行为：
     * - 销毁当前 Fragment
     * - 恢复上一个 Fragment
     * - 如果栈中只有一个 Fragment，不进行操作
     *
     * 适用场景：返回上一页、关闭弹窗
     */
    fun pop() {
        if (fragmentStack.size <= 1) {
            println("Warning: Cannot pop the only fragment in stack")
            return
        }

        // 1. 销毁当前 Fragment
        val current = fragmentStack.pop()
        current.performDestroyView()
        container.children.clear()

        // 2. 恢复上一个 Fragment
        val previous = fragmentStack.peek()
        val view = previous.performCreateView()
        container.children += view

        // 3. 通知监听器
        listeners.forEach { it.onFragmentRemoved(current) }
        listeners.forEach { it.onStackChanged(fragmentStack.size) }
    }

    /**
     * 弹出到指定 Fragment
     * 找到栈中最上面的指定 Fragment，弹出其上面的所有 Fragment
     */
    fun popTo(fragmentClass: Class<out Fragment>, inclusive: Boolean = false) {
        while (fragmentStack.isNotEmpty()) {
            val top = fragmentStack.peek()
            if (top.javaClass == fragmentClass) {
                if (!inclusive) {
                    // 保留这个 Fragment
                    val view = top.performCreateView()
                    container.children.clear()
                    container.children += view
                    return
                } else {
                    // 也弹出这个 Fragment
                    pop()
                    return
                }
            }

            if (fragmentStack.size > 1) {
                pop()
            } else {
                println("Warning: Target fragment not found in stack")
                return
            }
        }
    }

    /**
     * 清空栈中的所有 Fragment（除了最后一个）
     */
    fun popToRoot() {
        while (fragmentStack.size > 1) {
            val removed = fragmentStack.removeAt(fragmentStack.size - 2)
            removed.performDestroy()
        }
        listeners.forEach { it.onStackChanged(fragmentStack.size) }
    }

    /**
     * 获取栈顶 Fragment
     */
    fun peek(): Fragment? = if (fragmentStack.isNotEmpty()) fragmentStack.peek() else null

    /**
     * 获取栈的大小
     */
    fun getStackSize(): Int = fragmentStack.size

    /**
     * 检查是否可以返回
     */
    fun canGoBack(): Boolean = fragmentStack.size > 1

    /**
     * 获取当前显示的 Fragment
     */
    fun getCurrentFragment(): Fragment? = peek()

    /**
     * 清空所有 Fragment
     */
    fun clear() {
        while (fragmentStack.isNotEmpty()) {
            val fragment = fragmentStack.pop()
            fragment.performDestroy()
        }
        container.children.clear()
        listeners.forEach { it.onStackChanged(0) }
    }

    /**
     * 打印栈信息（调试用）
     */
    fun printStack() {
        println("=== Fragment Stack ===")
        fragmentStack.forEachIndexed { index, fragment ->
            println("[$index] ${fragment.javaClass.simpleName} - ${fragment.getState()}")
        }
        println("======================")
    }
}

// ============ Back Press Handler ============

/**
 * 处理返回按钮
 */
class BackPressHandler(
    private val fragmentManager: FragmentManager
) {

    fun onBackPressed(): Boolean {
        val current = fragmentManager.getCurrentFragment()

        // 1. 询问当前 Fragment 是否处理返回
        if (current is BackPressable) {
            if (current.onBackPressed()) {
                return true
            }
        }

        // 2. 如果 Fragment 没处理，由 FragmentManager 处理
        return if (fragmentManager.canGoBack()) {
            fragmentManager.pop()
            true
        } else {
            false
        }
    }
}

/**
 * Fragment 可选接口 - 处理返回按钮
 */
interface BackPressable {
    /**
     * 处理返回按钮
     * @return true 表示已处理，false 表示未处理
     */
    fun onBackPressed(): Boolean
}

// ============ Fragment Transaction ============

/**
 * Fragment 事务 - 支持多个操作的原子性
 */
class FragmentTransaction(
    private val fragmentManager: FragmentManager
) {

    private val operations = mutableListOf<() -> Unit>()

    fun replace(fragment: Fragment): FragmentTransaction {
        operations += { fragmentManager.replace(fragment) }
        return this
    }

    fun push(fragment: Fragment): FragmentTransaction {
        operations += { fragmentManager.push(fragment) }
        return this
    }

    fun pop(): FragmentTransaction {
        operations += { fragmentManager.pop() }
        return this
    }

    fun commit() {
        operations.forEach { it() }
    }
}

// ============ Helper Extension ============

/**
 * 在 FragmentManager 上创建事务
 */
fun FragmentManager.transaction(block: FragmentTransaction.() -> Unit) {
    val tx = FragmentTransaction(this)
    tx.apply(block)
    tx.commit()
}
