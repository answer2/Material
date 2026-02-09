package dev.answer.material.view

import dev.answer.material.content.ContextImpl
import dev.answer.material.manager.ActivityManager

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

    fun finish() {
        ActivityManager.finishActivity()
    }
}
