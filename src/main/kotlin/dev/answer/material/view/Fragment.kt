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
import javafx.geometry.Insets
import javafx.geometry.Pos

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 20:25
 * @description FrameLayout
 */

class FrameLayout(context: Context) : GroupView(context) {

    override fun addView(view: View, params: LayoutParams) {
        val frameParams = when {
            params is FrameLayoutParams -> params
            else -> FrameLayoutParams(
                width = params.width,
                height = params.height
            )
        }

        view.layoutParams = frameParams
        super.addView(view, frameParams)
        applyLayoutParams(view, frameParams)
    }

    override fun removeView(view: View) {
        super.removeView(view)
    }

    override fun onLayout(l: Double, t: Double, r: Double, b: Double) {
        super.onLayout(l, t, r, b)

        val paddingLeft = this.paddingLeft
        val paddingTop = this.paddingTop
        val paddingRight = this.paddingRight
        val paddingBottom = this.paddingBottom

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        childrenViews.forEach { view ->
            val params = view.layoutParams as? FrameLayoutParams ?: return@forEach
            
            val marginLeft = params.marginLeft
            val marginTop = params.marginTop
            val marginRight = params.marginRight
            val marginBottom = params.marginBottom

            // 计算子视图的位置和大小
            var childLeft = paddingLeft + marginLeft
            var childTop = paddingTop + marginTop
            var childRight = childLeft + view.measuredWidth
            var childBottom = childTop + view.measuredHeight

            // 应用对齐方式
            when (params.alignment) {
                Pos.TOP_LEFT -> {
                    // 左上角对齐
                }
                Pos.TOP_CENTER -> {
                    childLeft = paddingLeft + (contentWidth - view.measuredWidth) / 2
                    childRight = childLeft + view.measuredWidth
                }
                Pos.TOP_RIGHT -> {
                    childLeft = paddingLeft + contentWidth - view.measuredWidth - marginRight
                    childRight = childLeft + view.measuredWidth
                }
                Pos.CENTER_LEFT -> {
                    childTop = paddingTop + (contentHeight - view.measuredHeight) / 2
                    childBottom = childTop + view.measuredHeight
                }
                Pos.CENTER -> {
                    childLeft = paddingLeft + (contentWidth - view.measuredWidth) / 2
                    childTop = paddingTop + (contentHeight - view.measuredHeight) / 2
                    childRight = childLeft + view.measuredWidth
                    childBottom = childTop + view.measuredHeight
                }
                Pos.CENTER_RIGHT -> {
                    childLeft = paddingLeft + contentWidth - view.measuredWidth - marginRight
                    childRight = childLeft + view.measuredWidth
                    childTop = paddingTop + (contentHeight - view.measuredHeight) / 2
                    childBottom = childTop + view.measuredHeight
                }
                Pos.BOTTOM_LEFT -> {
                    childTop = paddingTop + contentHeight - view.measuredHeight - marginBottom
                    childBottom = childTop + view.measuredHeight
                }
                Pos.BOTTOM_CENTER -> {
                    childLeft = paddingLeft + (contentWidth - view.measuredWidth) / 2
                    childRight = childLeft + view.measuredWidth
                    childTop = paddingTop + contentHeight - view.measuredHeight - marginBottom
                    childBottom = childTop + view.measuredHeight
                }
                Pos.BOTTOM_RIGHT -> {
                    childLeft = paddingLeft + contentWidth - view.measuredWidth - marginRight
                    childRight = childLeft + view.measuredWidth
                    childTop = paddingTop + contentHeight - view.measuredHeight - marginBottom
                    childBottom = childTop + view.measuredHeight
                }
                else -> {}
            }

            // 布局子视图
            view.layout(childLeft, childTop, childRight, childBottom)
        }
    }

    private fun applyLayoutParams(view: View, params: FrameLayoutParams) {
        // 布局参数在onLayout中应用
    }
}