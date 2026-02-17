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

import dev.answer.material.content.Activity
import dev.answer.material.content.Context
import dev.answer.material.view.View
import javafx.animation.Animation
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.animation.Transition
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.InputMethodEvent
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.util.Duration

/**
 * @author AnswerDev
 * @date   2026/2/17
 * @description TextInputEditText
 */
class TextInputEditText(
    context: Context,
    var hint: String = "",
    val style: Style = Style.FILLED,
) : View(context) {

    // ── Style variants ─────────────────────────────────────────────────────
    enum class Style { FILLED, OUTLINED }

    // ── Text state ─────────────────────────────────────────────────────────
    var text: String = ""
        private set

    var maxLength: Int = -1
    var passwordMode: Boolean = false
    var prefixText: String = ""
    var suffixText: String = ""
    var supportingText: String = ""
    var errorText: String = ""
    var showCharCounter: Boolean = false

    val isError: Boolean get() = errorText.isNotEmpty()

    // ── Animation progress ─────────────────────────────────────────────────
    private var labelProgress = if (text.isNotEmpty()) 1.0 else 0.0
    private var focusProgress = 0.0

    private var labelTransition: Transition? = null
    private var focusTransition: Transition? = null

    // ── Cursor blink ───────────────────────────────────────────────────────
    private var cursorVisible = true
    private var cursorTimeline: Timeline? = null

    // ── Cursor & IME ───────────────────────────────────────────────────────
    private var cursorIndex: Int = 0
    private var composedText: String = ""
    private var composedCaret: Int = 0

    // ── Undo / Redo ────────────────────────────────────────────────────────

    /**
     * 一条历史快照。
     * @param text        操作完成后的文字内容
     * @param cursor      操作完成后的光标位置
     * @param kind        操作类型，用于相邻合并判断
     */
    private data class Snapshot(
        val text:   String,
        val cursor: Int,
        val kind:   EditKind,
    )

    private enum class EditKind {
        /** 单字符输入（连续打字合并） */
        TYPE,
        /** 单字符退格（连续退格合并） */
        BACKSPACE,
        /** 单字符 Delete（连续 Delete 合并） */
        DELETE,
        /** 其他不可合并操作：粘贴、剪切、IME 提交、选区替换等 */
        ATOMIC,
    }

    private val undoStack: ArrayDeque<Snapshot> = ArrayDeque()
    private val redoStack: ArrayDeque<Snapshot> = ArrayDeque()

    /** undo 栈最大容量，防止无限增长 */
    private val maxUndoSize = 200

    /** 上一次 push 的操作类型，用于合并判断 */
    private var lastEditKind: EditKind? = null

    /**
     * 在修改 text 之前调用：把修改前的状态存入 undo 栈。
     * [kind] 决定是否与上一条合并。
     */
    private fun saveUndo(kind: EditKind) {
        val snap = Snapshot(text, cursorIndex, kind)

        val shouldMerge = kind != EditKind.ATOMIC
                && lastEditKind == kind
                && undoStack.isNotEmpty()

        if (!shouldMerge) {
            undoStack.addLast(snap)
            if (undoStack.size > maxUndoSize) undoStack.removeFirst()
            redoStack.clear()
        }
        // 合并时：不改变栈顶（栈顶保存的是本次合并区间的起始状态），只更新 lastEditKind
        lastEditKind = kind
    }

    /** 执行 undo，返回是否成功 */
    private fun undo(): Boolean {
        if (undoStack.isEmpty()) return false

        // 把当前状态推入 redo 栈
        redoStack.addLast(Snapshot(text, cursorIndex, lastEditKind ?: EditKind.ATOMIC))

        val snap = undoStack.removeLast()
        applySnapshot(snap)
        lastEditKind = null   // 打断合并链
        return true
    }

    /** 执行 redo，返回是否成功 */
    private fun redo(): Boolean {
        if (redoStack.isEmpty()) return false

        // 把当前状态推入 undo 栈
        undoStack.addLast(Snapshot(text, cursorIndex, lastEditKind ?: EditKind.ATOMIC))

        val snap = redoStack.removeLast()
        applySnapshot(snap)
        lastEditKind = null
        return true
    }

    private fun applySnapshot(snap: Snapshot) {
        text        = snap.text
        cursorIndex = snap.cursor.coerceIn(0, snap.text.length)
        clearSelection()
        ensureCursorVisible()
        updateLabelFloat(animate = false)
        onTextChanged?.invoke(text)
        invalidate()
    }

    // ── Selection ─────────────────────────────────────────────────────────
    /**
     * 选区范围 [selectionStart, selectionEnd)，均为 text 的字符索引。
     * selectionStart == -1 表示无选区。
     * 始终满足 selectionStart <= selectionEnd。
     */
    private var selectionStart: Int = -1
    private var selectionEnd:   Int = -1

    /** 是否有非空选区 */
    private val hasSelection: Boolean
        get() = selectionStart >= 0 && selectionStart < selectionEnd

    /** 当前选中的文字（密码模式返回原始字符，不是 •） */
    private val selectedText: String
        get() = if (hasSelection) text.substring(selectionStart, selectionEnd) else ""

    /** 清除选区 */
    private fun clearSelection() { selectionStart = -1; selectionEnd = -1 }

    /**
     * 删除选区内容，光标移动到 selectionStart，清除选区。
     * 不调用 invalidate()，由调用方负责。
     */
    private fun deleteSelection() {
        if (!hasSelection) return
        saveUndo(EditKind.ATOMIC)
        text = text.removeRange(selectionStart, selectionEnd)
        cursorIndex = selectionStart
        clearSelection()
        ensureCursorVisible()
        onTextChanged?.invoke(text)
        updateLabelFloat(animate = true)
    }

    /**
     * 用 replacement 替换选区（或在光标处插入）。
     * [kind] 决定 undo 合并策略。
     */
    private fun replaceSelection(replacement: String, kind: EditKind = EditKind.ATOMIC) {
        if (hasSelection) {
            saveUndo(EditKind.ATOMIC)
            text = text.removeRange(selectionStart, selectionEnd)
            cursorIndex = selectionStart
            clearSelection()
        }
        // 检查 maxLength
        val insertable = if (maxLength >= 0)
            replacement.take((maxLength - text.length).coerceAtLeast(0))
        else replacement
        if (insertable.isEmpty()) return
        saveUndo(kind)
        text = text.substring(0, cursorIndex) + insertable + text.substring(cursorIndex)
        cursorIndex += insertable.length
        ensureCursorVisible()
        onTextChanged?.invoke(text)
        updateLabelFloat(animate = true)
    }

    // ── Horizontal scroll ─────────────────────────────────────────────────
    /**
     * 文字区域的横向滚动偏移（>0 = 内容左移，右侧超出部分进入视野）。
     * 由 ensureCursorVisible() 自动维护。
     */
    private var textScrollOffset: Double = 0.0

    // ── Geometry constants ─────────────────────────────────────────────────
    private val cornerRadius      = 4.0
    private val fieldHeight       = 56.0
    private val labelFontSizeIdle = 16.0
    private val labelFontSizeUp   = 12.0
    private val textFontSize      = 16.0
    private val supportFontSize   = 12.0
    private val horizontalPad     = 16.0
    private val labelIdleY        = fieldHeight / 2.0 + labelFontSizeIdle * 0.35
    private val labelUpY          = labelFontSizeUp + 4.0
    private val textBaselineY     = fieldHeight - 10.0

    // ── Callbacks ──────────────────────────────────────────────────────────
    var onTextChanged: ((String) -> Unit)? = null

    // ── init ───────────────────────────────────────────────────────────────
    init {
        clickable = true
        focusable = true
    }

    // =====================================================================
    // Public API
    // =====================================================================

    fun setText(value: String) {
        text = if (maxLength >= 0) value.take(maxLength) else value
        cursorIndex      = text.length
        textScrollOffset = 0.0
        clearSelection()
        ensureCursorVisible()
        updateLabelFloat(animate = false)
        onTextChanged?.invoke(text)
        invalidate()
    }

    fun setError(message: String) { errorText = message; invalidate() }
    fun clearError()              { errorText = "";      invalidate() }

    // =====================================================================
    // Measurement
    // =====================================================================

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val supportLineHeight = if (supportingText.isNotEmpty() || isError || showCharCounter)
            supportFontSize + 8.0 else 0.0
        minimumWidth  = 200
        minimumHeight = (fieldHeight + supportLineHeight).toInt()
        super.onMeasure(widthSpec, heightSpec)
    }

    // =====================================================================
    // Drawing
    // =====================================================================

    override fun onDraw(gc: GraphicsContext) {
        val palette = context.colorScheme
        val w = width
        val h = fieldHeight

        // ── Colors ──────────────────────────────────────────────────────
        val primaryColor   = palette.primary
        val errorColor     = Color.web("#B3261E")
        val onSurface      = palette.onSurface
        val surfaceVariant = palette.surfaceVariant ?: Color.web("#E7E0EC")
        val outline        = palette.outline        ?: Color.web("#79747E")

        val activeColor    = if (isError) errorColor else primaryColor
        val labelIdleColor = onSurface.deriveColor(0.0, 1.0, 1.0, if (enabled) 0.6 else 0.38)
        val labelUpColor   = if (!enabled) labelIdleColor else
            activeColor.deriveColor(0.0, 1.0, 1.0, 0.6 + 0.4 * focusProgress)
        val textColor      = if (enabled) onSurface
        else onSurface.deriveColor(0.0, 1.0, 1.0, 0.38)
        val supportColor   = if (isError) errorColor
        else onSurface.deriveColor(0.0, 1.0, 1.0, if (enabled) 0.6 else 0.38)

        // ── 1. Container ────────────────────────────────────────────────
        when (style) {
            Style.FILLED -> {
                val containerColor = if (enabled) surfaceVariant
                else onSurface.deriveColor(0.0, 1.0, 1.0, 0.04)
                gc.fill = containerColor
                gc.fillRoundRect(0.0, 0.0, w, h, cornerRadius * 2, cornerRadius * 2)

                val lineH     = 1.0 + focusProgress
                val lineColor = if (!enabled) onSurface.deriveColor(0.0, 1.0, 1.0, 0.38)
                else lerp(outline, activeColor, focusProgress.toFloat())
                gc.fill = lineColor
                gc.fillRect(0.0, h - lineH, w, lineH)
            }
            Style.OUTLINED -> {
                val borderWidth = 1.0 + focusProgress
                val borderColor = if (!enabled) onSurface.deriveColor(0.0, 1.0, 1.0, 0.38)
                else lerp(outline, activeColor, focusProgress.toFloat())
                gc.stroke    = borderColor
                gc.lineWidth = borderWidth
                gc.strokeRoundRect(
                    borderWidth / 2, borderWidth / 2,
                    w - borderWidth, h - borderWidth,
                    cornerRadius * 2, cornerRadius * 2
                )
            }
        }

        // ── 2. Floating label ────────────────────────────────────────────
        val labelSize  = lerp(labelFontSizeIdle.toFloat(), labelFontSizeUp.toFloat(), labelProgress.toFloat()).toDouble()
        val labelY     = lerp(labelIdleY.toFloat(), labelUpY.toFloat(), labelProgress.toFloat()).toDouble()
        val labelColor = lerp(labelIdleColor, labelUpColor, labelProgress.toFloat())
        gc.fill = labelColor
        gc.font = Font.font(labelSize)
        gc.fillText(hint, horizontalPad, labelY)

        // ── 3. Prefix（不随滚动移动，固定在左侧）────────────────────────
        val inputFont = Font.font(textFontSize)
        var textAreaStartX = horizontalPad

        if (prefixText.isNotEmpty() && (hasFocus || text.isNotEmpty())) {
            gc.fill = textColor.deriveColor(0.0, 1.0, 1.0, 0.6)
            gc.font = inputFont
            gc.fillText(prefixText, horizontalPad, textBaselineY)
            textAreaStartX += getTextWidth(prefixText, inputFont) + 4.0
        }

        // 后缀占据右侧空间
        val suffixReserved = if (suffixText.isNotEmpty() && (hasFocus || text.isNotEmpty()))
            getTextWidth(suffixText, inputFont) + horizontalPad
        else horizontalPad
        val textAreaWidth = (w - textAreaStartX - suffixReserved).coerceAtLeast(0.0)

        // ── 4. 文字区域：clip → translate → draw → restore ──────────────
        gc.save()

        // 裁剪：只在文字可视区域内渲染
        gc.beginPath()
        gc.rect(textAreaStartX, 0.0, textAreaWidth, h)
        gc.clip()

        // 横向滚动平移（向左偏移使超出右侧的文字进入视野）
        gc.translate(-textScrollOffset, 0.0)

        val displayText = if (passwordMode) "•".repeat(text.length) else text
        val safeCursor  = cursorIndex.coerceIn(0, text.length)
        val before      = displayText.substring(0, safeCursor)
        val after       = displayText.substring(safeCursor)
        val beforeWidth = getTextWidth(before, inputFont)

        gc.fill = textColor
        gc.font = inputFont

        // ── 选区高亮（在文字下层绘制）────────────────────────────────────
        if (hasSelection && !passwordMode) {
            val selStart = selectionStart.coerceIn(0, displayText.length)
            val selEnd   = selectionEnd.coerceIn(0, displayText.length)
            val selX     = textAreaStartX + getTextWidth(displayText.substring(0, selStart), inputFont)
            val selW     = getTextWidth(displayText.substring(selStart, selEnd), inputFont)
            gc.fill = activeColor.deriveColor(0.0, 1.0, 1.0, 0.25)
            gc.fillRect(selX, textBaselineY - textFontSize, selW.coerceAtLeast(2.0), textFontSize + 4.0)
            gc.fill = textColor   // 恢复文字颜色
        } else if (hasSelection && passwordMode) {
            // 密码模式：• 号的选区宽度
            val dotW = getTextWidth("•", inputFont)
            val selX = textAreaStartX + dotW * selectionStart
            val selW = dotW * (selectionEnd - selectionStart)
            gc.fill = activeColor.deriveColor(0.0, 1.0, 1.0, 0.25)
            gc.fillRect(selX, textBaselineY - textFontSize, selW.coerceAtLeast(2.0), textFontSize + 4.0)
            gc.fill = textColor
        }

        // 光标前文字
        gc.fillText(before, textAreaStartX, textBaselineY)

        // 组合文字（输入法拼写中）
        var composedWidth = 0.0
        if (composedText.isNotEmpty() && !passwordMode) {
            val composedX = textAreaStartX + beforeWidth
            gc.fillText(composedText, composedX, textBaselineY)
            composedWidth = getTextWidth(composedText, inputFont)
            gc.stroke    = activeColor
            gc.lineWidth = 1.0
            gc.strokeLine(composedX, textBaselineY + 2.0,
                composedX + composedWidth, textBaselineY + 2.0)
        }

        // 光标后文字
        gc.fillText(after, textAreaStartX + beforeWidth + composedWidth, textBaselineY)

        // 光标（闪烁）
        if (hasFocus && enabled && cursorVisible) {
            gc.fill = activeColor
            gc.fillRect(
                textAreaStartX + beforeWidth + composedWidth,
                textBaselineY - textFontSize,
                1.5, textFontSize + 2.0
            )
        }

        gc.restore()   // ← 恢复 clip 和 translate

        // ── 5. Suffix（不随滚动，固定在右侧）────────────────────────────
        if (suffixText.isNotEmpty() && (hasFocus || text.isNotEmpty())) {
            gc.fill = textColor.deriveColor(0.0, 1.0, 1.0, 0.6)
            gc.font = inputFont
            val sufW = getTextWidth(suffixText, inputFont)
            gc.fillText(suffixText, w - horizontalPad - sufW, textBaselineY)
        }

        // ── 6. Supporting text & counter ─────────────────────────────────
        if (supportingText.isNotEmpty() || isError || showCharCounter) {
            val rowY = h + supportFontSize + 4.0
            gc.font = Font.font(supportFontSize)
            val leftMsg = if (isError) errorText else supportingText
            if (leftMsg.isNotEmpty()) {
                gc.fill = supportColor
                gc.fillText(leftMsg, horizontalPad, rowY)
            }
            if (showCharCounter && maxLength > 0) {
                gc.fill = supportColor
                val counterStr = "${text.length} / $maxLength"
                val counterW   = getTextWidth(counterStr, Font.font(supportFontSize))
                gc.fillText(counterStr, w - horizontalPad - counterW, rowY)
            }
        }
    }

    // =====================================================================
    // Horizontal scroll — keep cursor inside the visible text area
    // =====================================================================

    /**
     * 计算当前光标在文字内容中的绝对像素 X（相对于 textAreaStartX 的偏移，不含 scroll）。
     * 同时返回可视区域宽度，供 ensureCursorVisible 使用。
     */
    private fun measureCursorAndArea(): Pair<Double, Double> {
        val inputFont      = Font.font(textFontSize)
        val displayText    = if (passwordMode) "•".repeat(text.length) else text
        val safeCursor     = cursorIndex.coerceIn(0, text.length)
        val beforeWidth    = getTextWidth(displayText.substring(0, safeCursor), inputFont)

        val prefixW        = if (prefixText.isNotEmpty() && (hasFocus || text.isNotEmpty()))
            getTextWidth(prefixText, inputFont) + 4.0 else 0.0
        val textAreaStartX = horizontalPad + prefixW
        val suffixReserved = if (suffixText.isNotEmpty() && (hasFocus || text.isNotEmpty()))
            getTextWidth(suffixText, inputFont) + horizontalPad else horizontalPad
        val textAreaWidth  = (width - textAreaStartX - suffixReserved).coerceAtLeast(0.0)

        return beforeWidth to textAreaWidth
    }

    /**
     * 根据光标位置调整 textScrollOffset，保证光标始终在可视区域内。
     * 光标靠近边缘时保留 8px 边距。
     */
    private fun ensureCursorVisible() {
        if (width <= 0) return
        val margin = 8.0
        val (cursorAbsX, textAreaWidth) = measureCursorAndArea()
        if (textAreaWidth <= 0) return

        val cursorInView = cursorAbsX - textScrollOffset

        when {
            // 光标在左侧不可见 → 向左滚动（减小 offset）
            cursorInView < margin ->
                textScrollOffset = (cursorAbsX - margin).coerceAtLeast(0.0)
            // 光标在右侧不可见 → 向右滚动（增大 offset）
            cursorInView > textAreaWidth - margin ->
                textScrollOffset = cursorAbsX - (textAreaWidth - margin)
        }

        // 全文比可视区域短 → 不需要滚动，回到起点
        val inputFont   = Font.font(textFontSize)
        val displayText = if (passwordMode) "•".repeat(text.length) else text
        val totalW      = getTextWidth(displayText + composedText, inputFont)
        if (totalW <= textAreaWidth) {
            textScrollOffset = 0.0
        }
    }

    // =====================================================================
    // Focus & IME
    // =====================================================================

    override fun requestFocus(): Boolean {
        val gained = super.requestFocus()
        if (gained) {
            val baseBridge = (context as Activity).getWindow().baseBridge
            val (absX, absY) = getLocationInWindow()
            baseBridge.layoutX   = absX
            baseBridge.layoutY   = absY + height / 2
            baseBridge.isVisible = true
            baseBridge.requestFocus()
        }
        return gained
    }

    // =====================================================================
    // Touch & Key
    // =====================================================================

    // ── 长按 / 拖拽选择 内部状态 ─────────────────────────────────────────
    /** 长按定时器，500 ms 后触发选词 */
    private var longPressTimer: javafx.animation.PauseTransition? = null
    /** 是否已触发长按（进入拖拽选择模式） */
    private var inDragSelect = false
    /** 拖拽选择时的锚点字符索引（长按落点） */
    private var dragAnchor = 0
    /** 按下时的鼠标位置，用于判断是否发生了移动（小于 4px 不算拖拽） */
    private var pressX = 0.0

    override fun onTouchEvent(event: MouseEvent): Boolean {
        if (!enabled) return false

        when (event.eventType) {

            // ── 按下 ──────────────────────────────────────────────────────
            MouseEvent.MOUSE_PRESSED -> {
                requestFocus()
                pressX = event.x
                inDragSelect = false

                val idx = xToIndex(event.x)
                cursorIndex = idx
                clearSelection()
                ensureCursorVisible()
                invalidate()

                // 启动长按定时器
                longPressTimer?.stop()
                longPressTimer = javafx.animation.PauseTransition(
                    javafx.util.Duration.millis(500.0)
                ).also { timer ->
                    timer.setOnFinished {
                        // 长按：选中光标所在词，进入拖拽选择模式
                        val (ws, we) = wordRangeAt(cursorIndex)
                        selectionStart = ws
                        selectionEnd   = we
                        cursorIndex    = we
                        dragAnchor     = ws     // 锚点 = 词的左端
                        inDragSelect   = true
                        ensureCursorVisible()
                        invalidate()
                    }
                    timer.play()
                }
                return true
            }

            // ── 拖拽 ──────────────────────────────────────────────────────
            MouseEvent.MOUSE_DRAGGED -> {
                if (!hasFocus) return false

                // 移动超过 4px 就取消长按，改为拖拽选择
                if (!inDragSelect && Math.abs(event.x - pressX) > 4.0) {
                    longPressTimer?.stop()
                    longPressTimer = null
                    inDragSelect  = true
                    dragAnchor    = xToIndex(pressX)  // 锚点 = 最初按下的字符
                    clearSelection()
                }

                if (inDragSelect) {
                    val dragIdx = xToIndex(event.x)
                    val lo = minOf(dragAnchor, dragIdx)
                    val hi = maxOf(dragAnchor, dragIdx)
                    if (lo == hi) {
                        clearSelection()
                        cursorIndex = lo
                    } else {
                        selectionStart = lo
                        selectionEnd   = hi
                        // 光标跟随拖拽端（拖向右时在右，拖向左时在左）
                        cursorIndex = dragIdx
                    }
                    ensureCursorVisible()
                    invalidate()
                }
                return true
            }

            // ── 释放 ──────────────────────────────────────────────────────
            MouseEvent.MOUSE_RELEASED -> {
                longPressTimer?.stop()
                longPressTimer = null
                inDragSelect   = false
                return true
            }

            // ── 移出 ──────────────────────────────────────────────────────
            MouseEvent.MOUSE_EXITED -> {
                longPressTimer?.stop()
                longPressTimer = null
            }

            else -> {}
        }

        return false
    }

    // ── xToIndex：屏幕 X → 字符索引（复用于点击和拖拽）─────────────────
    private fun xToIndex(screenX: Double): Int {
        val inputFont      = Font.font(textFontSize)
        val prefixW        = if (prefixText.isNotEmpty() && (hasFocus || text.isNotEmpty()))
            getTextWidth(prefixText, inputFont) + 4.0 else 0.0
        val textAreaStartX = horizontalPad + prefixW
        val absX           = screenX - textAreaStartX + textScrollOffset

        val displayText = if (passwordMode) "•".repeat(text.length) else text
        var accumulated = 0.0
        for (i in displayText.indices) {
            val cw = getTextWidth(displayText[i].toString(), inputFont)
            if (absX < accumulated + cw / 2) return i
            accumulated += cw
        }
        return displayText.length
    }

    // ── wordRangeAt：找字符索引所在词的 [start, end) ────────────────────
    /**
     * 以 [index] 为中心，向两侧扩展找到完整词（连续非空白字符）。
     * 如果 index 落在空白上，返回 [index, index]（空选区）。
     */
    private fun wordRangeAt(index: Int): Pair<Int, Int> {
        val i = index.coerceIn(0, text.length)
        if (text.isEmpty() || i == text.length || text[i].isWhitespace())
            return i to i

        var start = i
        var end   = i
        while (start > 0 && !text[start - 1].isWhitespace()) start--
        while (end < text.length && !text[end].isWhitespace()) end++
        return start to end
    }

    override fun onKeyPressed(event: KeyEvent) {
        if (!enabled || !hasFocus) return

        val ctrl  = event.isShortcutDown   // macOS = Cmd, Windows/Linux = Ctrl
        val shift = event.isShiftDown

        // ── Ctrl 快捷键 ──────────────────────────────────────────────────
        if (ctrl) {
            when (event.code) {

                // Ctrl+A — 全选
                KeyCode.A -> {
                    selectionStart = 0
                    selectionEnd   = text.length
                    cursorIndex    = text.length
                    ensureCursorVisible()
                    invalidate()
                    return
                }

                // Ctrl+C — 复制（密码模式禁止）
                KeyCode.C -> {
                    if (!passwordMode && hasSelection) {
                        val cb = Clipboard.getSystemClipboard()
                        val cc = ClipboardContent()
                        cc.putString(selectedText)
                        cb.setContent(cc)
                    }
                    return
                }

                // Ctrl+X — 剪切（密码模式禁止）
                KeyCode.X -> {
                    if (!passwordMode && hasSelection) {
                        val cb = Clipboard.getSystemClipboard()
                        val cc = ClipboardContent()
                        cc.putString(selectedText)
                        cb.setContent(cc)
                        deleteSelection()
                        invalidate()
                    }
                    return
                }

                // Ctrl+V — 粘贴
                KeyCode.V -> {
                    val cb   = Clipboard.getSystemClipboard()
                    val clip = cb.string ?: return
                    // 过滤换行，单行输入框只取第一行
                    val paste = clip.lines().firstOrNull()?.ifEmpty { null } ?: return
                    replaceSelection(paste)
                    clearSelection()
                    invalidate()
                    return
                }

                // Ctrl+Z — 撤销
                KeyCode.Z -> {
                    if (shift) redo() else undo()   // Ctrl+Shift+Z = redo
                    return
                }

                // Ctrl+Y — 重做（Windows 习惯）
                KeyCode.Y -> {
                    redo()
                    return
                }

                // Ctrl+Left — 跳词左移
                KeyCode.LEFT -> {
                    val newIdx = prevWordBoundary(cursorIndex)
                    if (shift) extendSelectionTo(newIdx) else clearSelection()
                    cursorIndex = newIdx
                    ensureCursorVisible()
                    invalidate()
                    return
                }

                // Ctrl+Right — 跳词右移
                KeyCode.RIGHT -> {
                    val newIdx = nextWordBoundary(cursorIndex)
                    if (shift) extendSelectionTo(newIdx) else clearSelection()
                    cursorIndex = newIdx
                    ensureCursorVisible()
                    invalidate()
                    return
                }

                else -> {}
            }
        }

        // ── 方向键（含 Shift 选区扩展）──────────────────────────────────
        when (event.code) {
            KeyCode.LEFT -> {
                if (hasSelection && !shift) {
                    // 有选区且不按 Shift：光标跳到选区左端，清除选区
                    cursorIndex = selectionStart
                    clearSelection()
                } else if (cursorIndex > 0) {
                    val newIdx = cursorIndex - 1
                    if (shift) extendSelectionTo(newIdx) else clearSelection()
                    cursorIndex = newIdx
                }
                ensureCursorVisible(); invalidate()
            }
            KeyCode.RIGHT -> {
                if (hasSelection && !shift) {
                    cursorIndex = selectionEnd
                    clearSelection()
                } else if (cursorIndex < text.length) {
                    val newIdx = cursorIndex + 1
                    if (shift) extendSelectionTo(newIdx) else clearSelection()
                    cursorIndex = newIdx
                }
                ensureCursorVisible(); invalidate()
            }
            KeyCode.HOME -> {
                if (shift) extendSelectionTo(0) else clearSelection()
                cursorIndex = 0
                ensureCursorVisible(); invalidate()
            }
            KeyCode.END -> {
                if (shift) extendSelectionTo(text.length) else clearSelection()
                cursorIndex = text.length
                ensureCursorVisible(); invalidate()
            }

            // ── 删除 ──────────────────────────────────────────────────────
            KeyCode.BACK_SPACE -> {
                if (hasSelection) {
                    deleteSelection()
                } else if (cursorIndex > 0) {
                    saveUndo(EditKind.BACKSPACE)
                    text = text.removeRange(cursorIndex - 1, cursorIndex)
                    cursorIndex--
                    ensureCursorVisible()
                    onTextChanged?.invoke(text)
                    updateLabelFloat(animate = true)
                }
                invalidate()
            }
            KeyCode.DELETE -> {
                if (hasSelection) {
                    deleteSelection()
                } else if (cursorIndex < text.length) {
                    saveUndo(EditKind.DELETE)
                    text = text.removeRange(cursorIndex, cursorIndex + 1)
                    ensureCursorVisible()
                    onTextChanged?.invoke(text)
                    updateLabelFloat(animate = true)
                }
                invalidate()
            }

            KeyCode.ESCAPE -> { clearSelection(); clearFocus() }
            else -> {}
        }
    }

    override fun onKeyTypeInput(event: KeyEvent) {
        if (!enabled || !hasFocus) return
        val ch = event.character
        if (ch.isNotEmpty() && !ch[0].isISOControl()) {
            replaceSelection(ch, kind = EditKind.TYPE)
            clearSelection()
            invalidate()
        }
    }

    override fun onInputMethodEvent(event: InputMethodEvent) {
        if (!enabled || !hasFocus) return

        val committed = event.committed
        if (committed.isNotEmpty()) {
            if (hasSelection) deleteSelection()
            saveUndo(EditKind.ATOMIC)
            text = text.substring(0, cursorIndex) + committed + text.substring(cursorIndex)
            cursorIndex  += committed.length
            composedText  = ""
            clearSelection()
            onTextChanged?.invoke(text)
            updateLabelFloat(animate = true)
        }

        composedText  = event.composed.joinToString("") { it.text }
        composedCaret = event.caretPosition
        ensureCursorVisible()
        invalidate()
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Any?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        if (!gainFocus) {
            composedText     = ""
            textScrollOffset = 0.0
            clearSelection()
            lastEditKind   = null
            longPressTimer?.stop()
            longPressTimer = null
            inDragSelect   = false
        }
        animateFocus(gainFocus)
        updateLabelFloat(animate = true)
    }

    // =====================================================================
    // Animations
    // =====================================================================

    private fun startCursorBlink() {
        cursorVisible = true
        cursorTimeline?.stop()
        cursorTimeline = Timeline(
            KeyFrame(Duration.millis(500.0),  { cursorVisible = false; invalidate() }),
            KeyFrame(Duration.millis(1000.0), { cursorVisible = true;  invalidate() })
        ).also { it.cycleCount = Animation.INDEFINITE; it.play() }
    }

    private fun stopCursorBlink() {
        cursorTimeline?.stop()
        cursorTimeline = null
        cursorVisible  = false
        invalidate()
    }

    private fun animateFocus(focused: Boolean) {
        if (focused) startCursorBlink() else stopCursorBlink()
        focusTransition?.stop()
        val start  = focusProgress
        val target = if (focused) 1.0 else 0.0
        if (start == target) return

        focusTransition = object : Transition() {
            init { cycleDuration = Duration.millis(150.0); interpolator = Interpolator.EASE_BOTH }
            override fun interpolate(frac: Double) {
                focusProgress = start + (target - start) * frac
                invalidate()
            }
        }.also { it.play() }
    }

    private fun updateLabelFloat(animate: Boolean) {
        val shouldFloat = hasFocus || text.isNotEmpty()
        val target      = if (shouldFloat) 1.0 else 0.0
        if (labelProgress == target) return
        if (!animate) { labelProgress = target; invalidate(); return }

        labelTransition?.stop()
        val start = labelProgress
        labelTransition = object : Transition() {
            init { cycleDuration = Duration.millis(150.0); interpolator = Interpolator.EASE_BOTH }
            override fun interpolate(frac: Double) {
                labelProgress = start + (target - start) * frac
                invalidate()
            }
        }.also { it.play() }
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    // ── Selection helpers ─────────────────────────────────────────────────

    /**
     * 将选区从锚点扩展到 [newIndex]。
     * 第一次调用时以当前 cursorIndex 作为锚点。
     */
    private fun extendSelectionTo(newIndex: Int) {
        val anchor = when {
            !hasSelection         -> cursorIndex       // 首次：锚 = 当前光标
            cursorIndex == selectionStart -> selectionEnd   // 光标在选区左端 → 锚在右端
            else                  -> selectionStart    // 光标在选区右端 → 锚在左端
        }
        val lo = minOf(anchor, newIndex)
        val hi = maxOf(anchor, newIndex)
        if (lo == hi) { clearSelection() } else { selectionStart = lo; selectionEnd = hi }
    }

    /**
     * 向左找词边界（跳过当前词，停在前一个词的末尾或字符串开头）。
     * 规则与 macOS/Windows 对齐：连续字母数字为一个词，空格/标点分隔。
     */
    private fun prevWordBoundary(from: Int): Int {
        var i = (from - 1).coerceAtLeast(0)
        // 跳过左侧空格
        while (i > 0 && text[i - 1].isWhitespace()) i--
        // 跳过左侧词
        while (i > 0 && !text[i - 1].isWhitespace()) i--
        return i
    }

    /**
     * 向右找词边界。
     */
    private fun nextWordBoundary(from: Int): Int {
        var i = from
        // 跳过当前词
        while (i < text.length && !text[i].isWhitespace()) i++
        // 跳过右侧空格
        while (i < text.length && text[i].isWhitespace()) i++
        return i
    }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

    private fun lerp(a: Color, b: Color, t: Float): Color {
        val tt = t.toDouble().coerceIn(0.0, 1.0)
        return Color(
            a.red    + (b.red    - a.red)    * tt,
            a.green  + (b.green  - a.green)  * tt,
            a.blue   + (b.blue   - a.blue)   * tt,
            a.opacity + (b.opacity - a.opacity) * tt
        )
    }

    private fun getTextWidth(str: String, font: Font): Double {
        val t = Text(str)
        t.font = font
        return t.layoutBounds.width
    }
}