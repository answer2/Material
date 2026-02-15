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

/**
 * 线性布局，支持水平和垂直排列，以及权重分配。
 *
 * @param context 上下文
 * @param orientation 方向（默认为垂直）
 */
class LinearView(
    context: Context,
    orientation: Orientation = Orientation.VERTICAL
) : ViewGroup(context) {

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

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        // 先测量所有子视图（使用父视图的规格）
        measureChildren(widthSpec, heightSpec)

        // 计算权重总和及固定部分尺寸
        var totalWeight = 0.0
        children.forEach {
            val params = it.layoutParams as? LinearLayoutParams
            if (params != null && params.weight > 0) {
                totalWeight += params.weight
            }
        }

        val paddingLeft = this.paddingLeft
        val paddingTop = this.paddingTop
        val paddingRight = this.paddingRight
        val paddingBottom = this.paddingBottom

        // 父视图可用内容区域大小（减去内边距）
        val contentWidth = MeasureSpec.getSize(widthSpec).toDouble() - paddingLeft - paddingRight
        val contentHeight = MeasureSpec.getSize(heightSpec).toDouble() - paddingTop - paddingBottom

        // 计算固定大小子视图占用的空间（不含权重子视图）
        var fixedWidth = 0.0
        var fixedHeight = 0.0
        children.forEach { child ->
            val lp = child.layoutParams
            val params = lp as? LinearLayoutParams
            // 如果无权值或权重为0，视为固定大小
            if (params == null || params.weight <= 0) {
                if (orientation == Orientation.HORIZONTAL) {
                    fixedWidth += child.measuredWidth + lp.marginLeft + lp.marginRight
                    fixedHeight = maxOf(fixedHeight, child.measuredHeight + lp.marginTop + lp.marginBottom)
                } else {
                    fixedWidth = maxOf(fixedWidth, child.measuredWidth + lp.marginLeft + lp.marginRight)
                    fixedHeight += child.measuredHeight + lp.marginTop + lp.marginBottom
                }
            }
        }

        // 如果有权重子视图，需要重新测量它们以分配剩余空间
        if (totalWeight > 0) {
            val childCount = children.size
            val spacingTotal = (childCount - 1) * spacing

            if (orientation == Orientation.HORIZONTAL) {
                // 水平方向：宽度按权重分配
                val remainingWidth = maxOf(0.0, contentWidth - fixedWidth - spacingTotal)
                children.forEach { child ->
                    val params = child.layoutParams as? LinearLayoutParams
                    if (params != null && params.weight > 0) {
                        val childWidth = (params.weight / totalWeight) * remainingWidth
                        val widthSpecExact = MeasureSpec.makeMeasureSpec(childWidth.toInt(), MeasureSpec.EXACTLY)
                        // 高度可以自由伸缩（AT_MOST）
                        val heightSpecAtMost = MeasureSpec.makeMeasureSpec(contentHeight.toInt(), MeasureSpec.AT_MOST)
                        child.measure(widthSpecExact, heightSpecAtMost)
                    }
                }
            } else {
                // 垂直方向：高度按权重分配
                val remainingHeight = maxOf(0.0, contentHeight - fixedHeight - spacingTotal)
                children.forEach { child ->
                    val params = child.layoutParams as? LinearLayoutParams
                    if (params != null && params.weight > 0) {
                        val childHeight = (params.weight / totalWeight) * remainingHeight
                        val widthSpecAtMost = MeasureSpec.makeMeasureSpec(contentWidth.toInt(), MeasureSpec.AT_MOST)
                        val heightSpecExact = MeasureSpec.makeMeasureSpec(childHeight.toInt(), MeasureSpec.EXACTLY)
                        child.measure(widthSpecAtMost, heightSpecExact)
                    }
                }
            }
        }

        // 重新计算总尺寸（包括内边距和固定部分）
        var totalWidth = paddingLeft + paddingRight
        var totalHeight = paddingTop + paddingBottom

        if (orientation == Orientation.HORIZONTAL) {
            // 水平布局：宽度为固定部分 + 权重分配部分，高度为所有子视图的最大高度
            totalWidth += fixedWidth
            if (totalWeight > 0) {
                totalWidth += contentWidth - fixedWidth // 剩余宽度已全部分配给权重子视图
            }
            totalHeight += fixedHeight
        } else {
            // 垂直布局：高度为固定部分 + 权重分配部分，宽度为所有子视图的最大宽度
            totalWidth += fixedWidth
            totalHeight += fixedHeight
            if (totalWeight > 0) {
                totalHeight += contentHeight - fixedHeight
            }
        }

        // 应用测量规格（可能受父视图限制）
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

        children.forEachIndexed { index, child ->
            val lp = child.layoutParams
            val linearParams = lp as? LinearLayoutParams

            val marginLeft = lp.marginLeft
            val marginTop = lp.marginTop
            val marginRight = lp.marginRight
            val marginBottom = lp.marginBottom

            // 第一个子视图不加间距，后续每个子视图之前加上间距
            if (index > 0) {
                if (orientation == Orientation.HORIZONTAL) {
                    currentX += spacing
                } else {
                    currentY += spacing
                }
            }

            // 计算子视图的最终位置（考虑外边距）
            var childLeft = currentX + marginLeft
            var childTop = currentY + marginTop

            // 子视图测量宽高
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            // 根据重力调整位置（仅针对非主轴方向）
            if (linearParams?.gravity != null) {
                when (orientation) {
                    Orientation.HORIZONTAL -> {
                        // 水平布局：重力影响垂直位置
                        when (linearParams.gravity) {
                            Gravity.START -> {
                                // 顶部对齐，无需调整
                            }
                            Gravity.CENTER -> {
                                childTop = paddingTop + marginTop + (contentHeight - childHeight) / 2
                            }
                            Gravity.END -> {
                                childTop = paddingTop + marginTop + contentHeight - childHeight
                            }
                            else -> {}
                        }
                    }
                    Orientation.VERTICAL -> {
                        // 垂直布局：重力影响水平位置
                        when (linearParams.gravity) {
                            Gravity.START -> {
                                // 左侧对齐，无需调整
                            }
                            Gravity.CENTER -> {
                                childLeft = paddingLeft + marginLeft + (contentWidth - childWidth) / 2
                            }
                            Gravity.END -> {
                                childLeft = paddingLeft + marginLeft + contentWidth - childWidth
                            }
                            else -> {}
                        }
                    }
                }
            }

            // 布局子视图
            child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)

            // 更新累积位置（包含子视图所占用的空间及右/下外边距）
            if (orientation == Orientation.HORIZONTAL) {
                currentX += childWidth + marginLeft + marginRight
            } else {
                currentY += childHeight + marginTop + marginBottom
            }
        }
    }
}