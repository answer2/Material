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
import dev.answer.material.view.measure.MeasureSpec
import javafx.geometry.Insets
import javafx.geometry.Pos

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 01:10
 * @description LinearView
 */

class LinearView(
    context: Context,
    orientation: Orientation = Orientation.VERTICAL
) : GroupView(context) {

    var orientation: Orientation = orientation
        set(value) {
            if (field == value) return
            field = value
            requestLayout()
        }

    var spacing: Double = 0.0
        set(value) {
            if (field == value) return
            field = value
            requestLayout()
        }

    /**
     * 测量所有子视图
     */
    protected open fun measureChildren(widthSpec: Int, heightSpec: Int) {
        for (child in childrenViews) {
            measureChild(child, widthSpec, heightSpec)
        }
    }

    /**
     * 测量单个子视图
     */
    protected open fun measureChild(child: View, parentWidthSpec: Int, parentHeightSpec: Int) {
        val lp = child.layoutParams
        val childWidthSpec = getChildMeasureSpec(parentWidthSpec, paddingLeft + paddingRight + lp.marginLeft + lp.marginRight, lp.width)
        val childHeightSpec = getChildMeasureSpec(parentHeightSpec, paddingTop + paddingBottom + lp.marginTop + lp.marginBottom, lp.height)
        child.measure(childWidthSpec, childHeightSpec)
    }

    /**
     * 计算子视图的测量规格
     */
    protected open fun getChildMeasureSpec(spec: Int, padding: Double, childDimension: Int): Int {
        val specMode = MeasureSpec.getMode(spec)
        val specSize = MeasureSpec.getSize(spec) - padding.toInt()

        return when {
            childDimension > 0 -> {
                // 子视图指定了具体大小
                MeasureSpec.makeMeasureSpec(childDimension, MeasureSpec.EXACTLY)
            }
            childDimension == LayoutSize.MATCH_PARENT -> {
                // 子视图匹配父视图
                when (specMode) {
                    MeasureSpec.EXACTLY -> MeasureSpec.makeMeasureSpec(specSize, MeasureSpec.EXACTLY)
                    MeasureSpec.AT_MOST -> MeasureSpec.makeMeasureSpec(specSize, MeasureSpec.AT_MOST)
                    else -> MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                }
            }
            childDimension == LayoutSize.WRAP_CONTENT -> {
                // 子视图包裹内容
                when (specMode) {
                    MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> MeasureSpec.makeMeasureSpec(specSize, MeasureSpec.AT_MOST)
                    else -> MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                }
            }
            else -> {
                // 其他情况
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            }
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        // 测量所有子视图
        measureChildren(widthSpec, heightSpec)

        // 计算总宽度和高度
        var totalWidth = paddingLeft + paddingRight
        var totalHeight = paddingTop + paddingBottom

        // 计算权重总和
        var totalWeight = 0.0
        childrenViews.forEach {
            val params = it.layoutParams as? LinearLayoutParams
            if (params != null && params.weight > 0) {
                totalWeight += params.weight
            }
        }

        // 计算固定大小的子视图占用的空间
        var fixedWidth = 0.0
        var fixedHeight = 0.0
        childrenViews.forEach {
            val lp = it.layoutParams
            val linearParams = lp as? LinearLayoutParams
            if (linearParams == null || linearParams.weight <= 0) {
                if (orientation == Orientation.HORIZONTAL) {
                    fixedWidth += it.measuredWidth + lp.marginLeft + lp.marginRight
                    fixedHeight = maxOf(fixedHeight, it.measuredHeight + lp.marginTop + lp.marginBottom)
                } else {
                    fixedWidth = maxOf(fixedWidth, it.measuredWidth + lp.marginLeft + lp.marginRight)
                    fixedHeight += it.measuredHeight + lp.marginTop + lp.marginBottom
                }
            }
        }

        // 计算可用空间
        val specWidth = MeasureSpec.getSize(widthSpec).toDouble()
        val specHeight = MeasureSpec.getSize(heightSpec).toDouble()
        val availableWidth = maxOf(0.0, specWidth - paddingLeft - paddingRight)
        val availableHeight = maxOf(0.0, specHeight - paddingTop - paddingBottom)

        // 计算权重分配的空间
        if (totalWeight > 0) {
            if (orientation == Orientation.HORIZONTAL) {
                val weightWidth = availableWidth - fixedWidth - (childrenViews.size - 1) * spacing
                childrenViews.forEach {
                    val params = it.layoutParams as? LinearLayoutParams
                    if (params != null && params.weight > 0) {
                        val childWidth = (params.weight / totalWeight) * weightWidth
                        val childHeightSpec = MeasureSpec.makeMeasureSpec(availableHeight.toInt(), MeasureSpec.AT_MOST)
                        it.measure(MeasureSpec.makeMeasureSpec(childWidth.toInt(), MeasureSpec.EXACTLY), childHeightSpec)
                    }
                }
            } else {
                val weightHeight = availableHeight - fixedHeight - (childrenViews.size - 1) * spacing
                childrenViews.forEach {
                    val params = it.layoutParams as? LinearLayoutParams
                    if (params != null && params.weight > 0) {
                        val childHeight = (params.weight / totalWeight) * weightHeight
                        val childWidthSpec = MeasureSpec.makeMeasureSpec(availableWidth.toInt(), MeasureSpec.AT_MOST)
                        it.measure(childWidthSpec, MeasureSpec.makeMeasureSpec(childHeight.toInt(), MeasureSpec.EXACTLY))
                    }
                }
            }
        }

        // 计算最终的测量大小
        if (orientation == Orientation.HORIZONTAL) {
            totalWidth = fixedWidth
            if (totalWeight > 0) {
                totalWidth += availableWidth - fixedWidth
            }
            totalHeight = maxOf(totalHeight, availableHeight)
        } else {
            totalWidth = maxOf(totalWidth, availableWidth)
            totalHeight = fixedHeight
            if (totalWeight > 0) {
                totalHeight += availableHeight - fixedHeight
            }
        }

        // 应用测量规格
        measuredWidth = getDefaultSize(totalWidth.toInt(), widthSpec)
        measuredHeight = getDefaultSize(totalHeight.toInt(), heightSpec)
    }

    override fun onLayout(l: Double, t: Double, r: Double, b: Double) {
        super.onLayout(l, t, r, b)

        val paddingLeft = this.paddingLeft
        val paddingTop = this.paddingTop
        val paddingRight = this.paddingRight
        val paddingBottom = this.paddingBottom

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        var currentX = paddingLeft
        var currentY = paddingTop

        childrenViews.forEachIndexed { index, view ->
            val lp = view.layoutParams
            val linearParams = lp as? LinearLayoutParams
            
            val marginLeft = lp.marginLeft
            val marginTop = lp.marginTop
            val marginRight = lp.marginRight
            val marginBottom = lp.marginBottom

            // 应用间距
            if (index > 0) {
                if (orientation == Orientation.HORIZONTAL) {
                    currentX += spacing
                } else {
                    currentY += spacing
                }
            }

            // 应用外边距
            currentX += marginLeft
            currentY += marginTop

            // 计算子视图的位置和大小
            var childWidth = view.measuredWidth
            var childHeight = view.measuredHeight

            // 应用重力（仅对LinearLayoutParams有效）
            if (linearParams?.gravity != null) {
                when (orientation) {
                    Orientation.HORIZONTAL -> {
                        // 在水平布局中，重力影响垂直位置
                        when (linearParams.gravity) {
                            Gravity.START -> {
                                // 顶部对齐
                            }
                            Gravity.CENTER -> {
                                currentY = paddingTop + (contentHeight - childHeight) / 2
                            }
                            Gravity.END -> {
                                currentY = paddingTop + contentHeight - childHeight
                            }
                            else -> {}
                        }
                    }
                    Orientation.VERTICAL -> {
                        // 在垂直布局中，重力影响水平位置
                        when (linearParams.gravity) {
                            Gravity.START -> {
                                // 左侧对齐
                            }
                            Gravity.CENTER -> {
                                currentX = paddingLeft + (contentWidth - childWidth) / 2
                            }
                            Gravity.END -> {
                                currentX = paddingLeft + contentWidth - childWidth
                            }
                            else -> {}
                        }
                    }
                }
            }

            // 布局子视图
            view.layout(currentX, currentY, currentX + childWidth, currentY + childHeight)

            // 更新当前位置
            if (orientation == Orientation.HORIZONTAL) {
                currentX += childWidth + marginRight
            } else {
                currentY += childHeight + marginBottom
            }
        }
    }

    /**
     * 应用布局参数
     */
    private fun applyLayoutParams(view: View, params: LinearLayoutParams) {
        // 布局参数在onMeasure和onLayout中应用
    }

    private fun Double?.orElse(default: Double): Double {
        return this ?: default
    }

    private fun maxOf(a: Double, b: Double): Double {
        return if (a > b) a else b
    }
}
