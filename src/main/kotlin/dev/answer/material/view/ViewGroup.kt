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
import javafx.scene.canvas.GraphicsContext

/**
 *
 * @author AnswerDev
 * @date 2026/2/14 00:48
 * @description ViewGroup
 */
open class ViewGroup(context: Context) : View(context) {

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        // 测量所有子视图
        measureChildren(widthSpec, heightSpec)

        // 计算当前 ViewGroup 的测量大小
        val width = getDefaultSize(suggestedMinimumWidth, widthSpec)
        val height = getDefaultSize(suggestedMinimumHeight, heightSpec)

        measuredWidth = width
        measuredHeight = height
    }

    override fun onLayout(l: Double, t: Double, r: Double, b: Double) {
        super.onLayout(l, t, r, b)
        // 布局所有子视图（由子类实现具体逻辑）
        layoutChildren(l, t, r, b)
    }

    /**
     * 测量所有子视图
     */
    protected open fun measureChildren(widthSpec: Int, heightSpec: Int) {
        for (child in children) {
            measureChild(child, widthSpec, heightSpec)
        }
    }

    /**
     * 测量单个子视图
     */
    protected open fun measureChild(child: View, parentWidthSpec: Int, parentHeightSpec: Int) {
        val lp = child.layoutParams
        val childWidthSpec = getChildMeasureSpec(
            parentWidthSpec,
            paddingLeft + paddingRight + lp.marginLeft + lp.marginRight,
            lp.width
        )
        val childHeightSpec = getChildMeasureSpec(
            parentHeightSpec,
            paddingTop + paddingBottom + lp.marginTop + lp.marginBottom,
            lp.height
        )
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

    /**
     * 布局所有子视图（由子类实现）
     */
    protected open fun layoutChildren(l: Double, t: Double, r: Double, b: Double) {
        // 子类覆盖
    }

    override val suggestedMinimumWidth: Int
        get() {
            var minWidth = super.suggestedMinimumWidth
            for (child in children) {
                minWidth = Math.max(minWidth, (child.measuredWidth + child.layoutParams.marginLeft + child.layoutParams.marginRight).toInt())
            }
            return minWidth
        }

    override val suggestedMinimumHeight: Int
        get() {
            var minHeight = super.suggestedMinimumHeight
            for (child in children) {
                minHeight = Math.max(minHeight, (child.measuredHeight + child.layoutParams.marginTop + child.layoutParams.marginBottom).toInt())
            }
            return minHeight
        }

    /**
     * 添加子视图并指定布局参数
     */
    open fun addView(view: View, params: LayoutParams) {
        view.layoutParams = params
        addView(view)
    }

    override fun addView(child: View) {
        super.addView(child)
        child.onAttach()
    }

    override fun removeView(child: View) {
        child.onDetach()
        super.removeView(child)
    }

    override fun removeAllViews() {
        for (child in children) {
            child.onDetach()
        }
        super.removeAllViews()
    }

    /**
     * 获取子视图数量
     */
    fun getChildCount(): Int = children.size

    /**
     * 获取指定位置的子视图
     */
    fun getChildAt(index: Int): View? = children.getOrNull(index)
}