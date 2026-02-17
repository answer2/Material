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

package dev.answer.material.component

import dev.answer.material.content.Context
import dev.answer.material.view.View
import dev.answer.material.view.ViewGroup
import dev.answer.material.view.measure.MeasureSpec
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseEvent


/**
 *
 * @author AnswerDev
 * @date 2026/2/17 16:48
 * @description TextInputLayout
 */

class TextInputLayout(
    context: Context,
    hint: String = "",
    style: TextInputEditText.Style = TextInputEditText.Style.FILLED,
) : ViewGroup(context) {

    // ── 内部持有的真正输入控件 ────────────────────────────────────────────
    val editText: TextInputEditText = TextInputEditText(context, hint, style)

    // ── End icon 模式 ─────────────────────────────────────────────────────
    enum class EndIconMode { NONE, PASSWORD_TOGGLE, CLEAR_TEXT }

    var endIconMode: EndIconMode = EndIconMode.NONE
        set(value) {
            if (field == value) return
            field = value
            syncEndIcon()
            requestLayout()
            invalidate()
        }

    /** 浮动标签文字 */
    var hint: String
        get() = editText.hint
        set(value) { editText.hint = value; invalidate() }

    /** 当前输入内容（只读，修改请用 setText） */
    val text: String get() = editText.text

    fun setText(value: String) = editText.setText(value)

    /** 错误信息；设为空字符串表示清除 */
    var error: String
        get() = editText.errorText
        set(value) {
            if (value.isEmpty()) editText.clearError() else editText.setError(value)
        }

    /** 辅助文字（helper text） */
    var supportingText: String
        get() = editText.supportingText
        set(value) { editText.supportingText = value; invalidate() }

    /** 最大字符数；-1 = 无限制 */
    var maxLength: Int
        get() = editText.maxLength
        set(value) { editText.maxLength = value }

    /** 是否显示字数计数器 */
    var showCharCounter: Boolean
        get() = editText.showCharCounter
        set(value) { editText.showCharCounter = value; invalidate() }

    /** 密码模式 */
    var passwordMode: Boolean
        get() = editText.passwordMode
        set(value) { editText.passwordMode = value; invalidate() }

    /** 前缀文字 */
    var prefixText: String
        get() = editText.prefixText
        set(value) { editText.prefixText = value; invalidate() }

    /** 后缀文字 */
    var suffixText: String
        get() = editText.suffixText
        set(value) { editText.suffixText = value; invalidate() }

    /** 文字变化回调 */
    var onTextChanged: ((String) -> Unit)?
        get() = editText.onTextChanged
        set(value) { editText.onTextChanged = value }

    // ── End icon 内部图标按钮（用 View 的 Canvas 绘制）───────────────────
    private val iconButton: IconButton = IconButton(context)

    // ── init ──────────────────────────────────────────────────────────────
    init {
        addView(editText)
        addView(iconButton)
        iconButton.visibility = View.GONE    // 默认隐藏
        syncEndIcon()
    }

    // ── 同步 End icon 状态 ────────────────────────────────────────────────
    private fun syncEndIcon() {
        when (endIconMode) {
            EndIconMode.NONE -> {
                iconButton.visibility = View.GONE
                editText.suffixText   = ""
            }
            EndIconMode.PASSWORD_TOGGLE -> {
                iconButton.visibility  = View.VISIBLE
                iconButton.iconType    = IconButton.IconType.EYE
                iconButton.onClickListener = View.OnClickListener {
                    passwordMode           = !passwordMode
                    iconButton.iconType    = if (passwordMode)
                        IconButton.IconType.EYE else IconButton.IconType.EYE_OFF
                    invalidate()
                }
            }
            EndIconMode.CLEAR_TEXT -> {
                iconButton.visibility  = View.VISIBLE
                iconButton.iconType    = IconButton.IconType.CLEAR
                iconButton.onClickListener = View.OnClickListener {
                    setText("")
                }
            }
        }
    }

    // ── override enabled ─────────────────────────────────────────────────
    override var enabled: Boolean
        get() = super.enabled
        set(value) {
            super.enabled      = value
            editText.enabled   = value
            iconButton.enabled = value
        }

    // =====================================================================
    // Measure & Layout
    // =====================================================================

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val availW = MeasureSpec.getSize(widthSpec).toDouble()

        // 图标宽度（如果可见）
        val iconW = if (iconButton.visibility == View.VISIBLE) 40.0 else 0.0

        // editText 宽度 = 可用宽度 - icon 宽度
        val etWidthSpec = MeasureSpec.makeMeasureSpec(
            (availW - iconW).coerceAtLeast(0.0).toInt(), MeasureSpec.EXACTLY
        )
        val etHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        editText.measure(etWidthSpec, etHeightSpec)

        if (iconButton.visibility == View.VISIBLE) {
            val iconWSpec = MeasureSpec.makeMeasureSpec(iconW.toInt(), MeasureSpec.EXACTLY)
            val iconHSpec = MeasureSpec.makeMeasureSpec(40, MeasureSpec.EXACTLY)
            iconButton.measure(iconWSpec, iconHSpec)
        }

        measuredWidth  = resolveSize(availW, widthSpec)
        measuredHeight = resolveSize(editText.measuredHeight.toDouble(), heightSpec)
    }

    override fun onLayout(l: Double, t: Double, r: Double, b: Double) {
        super.onLayout(l, t, r, b)

        val iconW  = if (iconButton.visibility == View.VISIBLE) 40.0 else 0.0
        val etW    = width - iconW

        // editText 占左侧全部区域（含 supportingText 高度）
        editText.layout(0.0, 0.0, etW, editText.measuredHeight.toDouble())

        // icon 垂直居中对齐 field 高度（56px）
        if (iconButton.visibility == View.VISIBLE) {
            val iconTop = (56.0 - 40.0) / 2.0
            iconButton.layout(etW, iconTop, etW + iconW, iconTop + 40.0)
        }
    }

    // =====================================================================
    // 不需要自己画，子 View 会画自己
    // =====================================================================
    override fun onDraw(gc: GraphicsContext) { /* nothing */ }

    // =====================================================================
    // 便捷工厂方法
    // =====================================================================
    companion object {
        /** 快速创建 Filled 样式 */
        fun filled(context: Context, hint: String = "") =
            TextInputLayout(context, hint, TextInputEditText.Style.FILLED)

        /** 快速创建 Outlined 样式 */
        fun outlined(context: Context, hint: String = "") =
            TextInputLayout(context, hint, TextInputEditText.Style.OUTLINED)
    }
}

private class IconButton(context: Context) : View(context) {

    enum class IconType { EYE, EYE_OFF, CLEAR }

    var iconType: IconType = IconType.EYE
        set(value) { field = value; invalidate() }

    init {
        clickable = true
    }

    override fun onDraw(gc: GraphicsContext) {
        val cx = width  / 2.0
        val cy = height / 2.0
        val r  = 9.0

        val palette = context.colorScheme
        gc.stroke    = palette.onSurface.deriveColor(0.0, 1.0, 1.0, if (enabled) 0.6 else 0.38)
        gc.lineWidth = 1.5
        gc.fill      = palette.onSurface.deriveColor(0.0, 1.0, 1.0, if (enabled) 0.6 else 0.38)

        when (iconType) {
            IconType.EYE -> {
                // 眼睛外轮廓（椭圆弧）
                gc.beginPath()
                gc.moveTo(cx - r, cy)
                gc.bezierCurveTo(cx - r, cy - r * 0.6, cx + r, cy - r * 0.6, cx + r, cy)
                gc.bezierCurveTo(cx + r, cy + r * 0.6, cx - r, cy + r * 0.6, cx - r, cy)
                gc.stroke()
                // 瞳孔
                gc.fillOval(cx - 2.5, cy - 2.5, 5.0, 5.0)
            }
            IconType.EYE_OFF -> {
                // 眼睛外轮廓（同上）
                gc.beginPath()
                gc.moveTo(cx - r, cy)
                gc.bezierCurveTo(cx - r, cy - r * 0.6, cx + r, cy - r * 0.6, cx + r, cy)
                gc.bezierCurveTo(cx + r, cy + r * 0.6, cx - r, cy + r * 0.6, cx - r, cy)
                gc.stroke()
                // 斜线表示"隐藏"
                gc.strokeLine(cx - r * 0.85, cy - r * 0.85, cx + r * 0.85, cy + r * 0.85)
            }
            IconType.CLEAR -> {
                // × 号
                val d = r * 0.65
                gc.strokeLine(cx - d, cy - d, cx + d, cy + d)
                gc.strokeLine(cx + d, cy - d, cx - d, cy + d)
            }
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {

        minimumWidth = 40
        minimumHeight = 40

        super.onMeasure(widthSpec, heightSpec)
    }
}