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

import dev.answer.material.content.Activity
import dev.answer.material.content.Context
import dev.answer.material.content.ContextImpl
import java.util.*

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 20:23
 * @description ActivityManager
 */
object ActivityManager {

    private val activityStack = Stack<Activity>()

    private fun startActivity(activity: Activity) {
        if (activityStack.isNotEmpty()) {
            activityStack.peek().performStop()
        }

        activityStack.push(activity)
        activity.performCreate()
        activity.performStart()
    }

     fun finishActivity() {
        if (activityStack.isEmpty()) return

        val current = activityStack.pop()
        current.performDestroy()

        if (activityStack.isNotEmpty()) {
            activityStack.peek().performStart()
        }
    }

    fun back() {
        finishActivity()
    }

    fun <T : Activity> startActivity(
        context: Context,
        clazz: Class<T>,
    ) {
        if (activityStack.isNotEmpty()) {
            activityStack.peek().performStop()
        }

        // 统一由系统创建 Activity
        val activity = createActivity(context, clazz)

        activityStack.push(activity)
        activity.performCreate()
        activity.performStart()
    }

    fun finishActivity(activity: Activity? = null) {
        if (activityStack.isEmpty()) return

        val current = activityStack.peek()

        // 只允许当前 Activity finish 自己
        if (activity != null && current !== activity) return

        activityStack.pop().performDestroy()

        if (activityStack.isNotEmpty()) {
            activityStack.peek().performStart()
        }
    }

    private fun <T : Activity> createActivity(
        context: Context,
        clazz: Class<T>,
    ): T {
        val ctor = clazz.getDeclaredConstructor()
        ctor.isAccessible = true
        val  obj =  ctor.newInstance();

        val method = ContextImpl::class.java.getDeclaredMethod("setBaseContext", Context::class.java)
        method.isAccessible = true;
        method.invoke(obj, context);
        return obj;
    }

}
