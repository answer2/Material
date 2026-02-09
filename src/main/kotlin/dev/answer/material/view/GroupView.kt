package dev.answer.material.view

import javafx.scene.layout.StackPane

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 01:10
 * @description GroupView
 */

open class GroupView : View() {

    protected val childrenViews = mutableListOf<View>()

    open fun addView(view: View, params: LayoutParams = LayoutParams()) {
        view.layoutParams = params
        childrenViews += view
        children += view
        view.onAttach()
    }

    open fun removeView(view: View) {
        childrenViews -= view
        children -= view
        view.onDetach()
    }

    fun removeAllViews() {
        childrenViews.forEach { it.onDetach() }
        childrenViews.clear()
        children.clear()
    }

    protected fun applyMargin(view: View, params: LayoutParams) {
        StackPane.setMargin(view, params.margin)
    }

}
