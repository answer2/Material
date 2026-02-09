package dev.answer.material.view

import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.layout.*

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 01:10
 * @description LinearView
 */
class LinearView(
    orientation: Orientation = Orientation.VERTICAL
) : GroupView() {

    private var container: Pane = createContainer(orientation)

    var orientation: Orientation = orientation
        set(value) {
            if (field == value) return
            field = value
            rebuild()
        }

    init {
        children += container
    }

    override fun addView(view: View, params: LayoutParams) {
        if (params !is LinearLayoutParams) {
            throw IllegalArgumentException("LinearView requires LinearLayoutParams")
        }

        view.layoutParams = params
        childrenViews += view
        container.children += view
        view.onAttach()

        applyLayoutParams(view, params)
    }

    override fun removeView(view: View) {
        childrenViews -= view
        container.children -= view
        view.onDetach()
    }

    private fun createContainer(orientation: Orientation): Pane {
        return when (orientation) {
            Orientation.HORIZONTAL -> HBox()
            Orientation.VERTICAL -> VBox()
        }
    }

    private fun rebuild() {
        val oldChildren = container.children.toList()
        children.clear()

        container = createContainer(orientation)
        container.children.addAll(oldChildren)

        children += container
    }

    private fun applyLayoutParams(view: View, params: LinearLayoutParams) {
        val isVertical = orientation == Orientation.VERTICAL

        // margin
        if (container is VBox) {
            VBox.setMargin(view, params.margin)
        } else {
            HBox.setMargin(view, params.margin)
        }

        // weight
        if (params.weight > 0) {
            if (container is VBox) {
                VBox.setVgrow(view, Priority.ALWAYS)
            } else {
                HBox.setHgrow(view, Priority.ALWAYS)
            }
        }

        // gravity
        if (container is VBox) {
            VBox.setVgrow(view, if (params.weight > 0) Priority.ALWAYS else Priority.NEVER)
            (container as VBox).alignment = params.gravity.toPos(true)
        } else if (container is HBox) {
            HBox.setHgrow(view, if (params.weight > 0) Priority.ALWAYS else Priority.NEVER)
            (container as HBox).alignment = params.gravity.toPos(false)
        }
    }
}
