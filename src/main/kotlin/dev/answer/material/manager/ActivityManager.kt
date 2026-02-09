package dev.answer.material.manager

import dev.answer.material.view.Activity
import java.util.Stack
/**
 *
 * @author AnswerDev
 * @date 2026/2/9 20:23
 * @description ActivityManager
 */
object ActivityManager {

    private val activityStack = Stack<Activity>()

    fun startActivity(activity: Activity) {
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
}
