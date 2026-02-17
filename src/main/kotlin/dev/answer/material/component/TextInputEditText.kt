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
import javafx.animation.Interpolator
import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.animation.Transition
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.InputMethodEvent
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.util.Duration

/**
 *
 * @author AnswerDev
 * @date 2026/2/17 03:01
 * @description TextInputEditText
 */
class TextInputEditText(
    context: Context,
    var hint: String = "",
    val style: Style = Style.FILLED,
) : View(context) {

    // ── Style variants ────────────────────────────────────────────────────
    enum class Style { FILLED, OUTLINED }

    // ── Text state ────────────────────────────────────────────────────────
    var text: String = ""
        private set

    var maxLength: Int = -1          // -1 = unlimited
    var passwordMode: Boolean = false
    var prefixText: String = ""
    var suffixText: String = ""
    var supportingText: String = ""
    var errorText: String = ""       // non-empty → error state
    var showCharCounter: Boolean = false

    val isError: Boolean get() = errorText.isNotEmpty()

    // ── Animation progress [0, 1] ─────────────────────────────────────────
    /** 0 = label at rest (inside field), 1 = label floated (above field) */
    private var labelProgress = if (text.isNotEmpty()) 1.0 else 0.0
    /** 0 = unfocused, 1 = focused */
    private var focusProgress  = 0.0

    // Running transitions (kept so we can cancel on re-trigger)
    private var labelTransition: Transition? = null
    private var focusTransition: Transition? = null

    // Cursor blink state
    private var cursorVisible = true
    private var cursorTimeline: Timeline? = null

    // ── Geometry constants ────────────────────────────────────────────────
    private val cornerRadius      = 4.0
    private val fieldHeight       = 56.0
    private val labelFontSizeIdle = 16.0
    private val labelFontSizeUp   = 12.0
    private val textFontSize      = 16.0
    private val supportFontSize   = 12.0
    private val horizontalPad     = 16.0
    private val labelIdleY        = fieldHeight / 2.0 + labelFontSizeIdle * 0.35  // baseline
    private val labelUpY          = labelFontSizeUp + 4.0                          // baseline when floated
    private val textBaselineY     = fieldHeight - 10.0

    // ── Callbacks ─────────────────────────────────────────────────────────
    var onTextChanged: ((String) -> Unit)? = null
    // 光标位置（插入点 index）
    private var cursorIndex: Int = 0
    // 正在输入的组合文本（拼音/假名等）
    private var composedText: String = ""
    // 组合文本光标（IME内部）
    private var composedCaret: Int = 0


    // ── init ──────────────────────────────────────────────────────────────
    init {
        clickable = true
        focusable  = true
    }

    // =====================================================================
    // Public API
    // =====================================================================



    fun setText(value: String) {
        text = if (maxLength >= 0) value.take(maxLength) else value
        cursorIndex = text.length
        updateLabelFloat(animate = false)
        onTextChanged?.invoke(text)
        invalidate()
    }

    fun setError(message: String) {
        errorText = message
        invalidate()
    }

    fun clearError() {
        errorText = ""
        invalidate()
    }

    // =====================================================================
    // Measurement
    // =====================================================================

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val supportLineHeight = if (supportingText.isNotEmpty() || isError || showCharCounter)
            supportFontSize + 8.0 else 0.0
        val desiredHeight = (fieldHeight + supportLineHeight).toInt()
        minimumWidth  = 200
        minimumHeight = desiredHeight
        super.onMeasure(widthSpec, heightSpec)
    }

    // =====================================================================
    // Drawing
    // =====================================================================

    override fun onDraw(gc: GraphicsContext) {
        val palette   = context.colorScheme
        val w         = width
        val h         = fieldHeight

        val primaryColor   = palette.primary
        val errorColor     = Color.web("#B3261E")
        val onSurface      = palette.onSurface
        val surfaceVariant = palette.surfaceVariant ?: Color.web("#E7E0EC")
        val outline        = palette.outline ?: Color.web("#79747E")

        val activeColor    = if (isError) errorColor else primaryColor
        val labelIdleColor = onSurface.deriveColor(0.0, 1.0, 1.0, if (enabled) 0.6 else 0.38)
        val labelUpColor   = if (!enabled) labelIdleColor else
            activeColor.deriveColor(0.0, 1.0, 1.0, 0.6 + 0.4 * focusProgress)

        val textColor = if (enabled) onSurface
        else onSurface.deriveColor(0.0, 1.0, 1.0, 0.38)

        val supportColor =
            if (isError) errorColor
            else onSurface.deriveColor(0.0, 1.0, 1.0, if (enabled) 0.6 else 0.38)

        when (style) {
            Style.FILLED -> {
                val containerColor = if (enabled)
                    surfaceVariant else
                    onSurface.deriveColor(0.0, 1.0, 1.0, 0.04)

                gc.fill = containerColor
                gc.fillRoundRect(0.0, 0.0, w, h, cornerRadius * 2, cornerRadius * 2)

                val lineH = 1.0 + focusProgress
                val lineColor = if (!enabled)
                    onSurface.deriveColor(0.0, 1.0, 1.0, 0.38)
                else lerp(outline, activeColor, focusProgress.toFloat())

                gc.fill = lineColor
                gc.fillRect(0.0, h - lineH, w, lineH)
            }

            Style.OUTLINED -> {
                val borderWidth = 1.0 + focusProgress
                val borderColor = if (!enabled)
                    onSurface.deriveColor(0.0, 1.0, 1.0, 0.38)
                else lerp(outline, activeColor, focusProgress.toFloat())

                gc.stroke = borderColor
                gc.lineWidth = borderWidth
                gc.strokeRoundRect(
                    borderWidth / 2,
                    borderWidth / 2,
                    w - borderWidth,
                    h - borderWidth,
                    cornerRadius * 2,
                    cornerRadius * 2
                )
            }
        }

        val labelSize = lerp(
            labelFontSizeIdle.toFloat(),
            labelFontSizeUp.toFloat(),
            labelProgress.toFloat()
        ).toDouble()

        val labelY = lerp(
            labelIdleY.toFloat(),
            labelUpY.toFloat(),
            labelProgress.toFloat()
        ).toDouble()

        val labelColor = lerp(labelIdleColor, labelUpColor, labelProgress.toFloat())

        gc.fill = labelColor
        gc.font = Font.font(labelSize)
        gc.fillText(hint, horizontalPad, labelY)

        var textOffsetX = horizontalPad

        if (prefixText.isNotEmpty() && (hasFocus || text.isNotEmpty())) {
            gc.fill = textColor.deriveColor(0.0, 1.0, 1.0, 0.6)
            gc.font = Font.font(textFontSize)
            gc.fillText(prefixText, textOffsetX, textBaselineY)
            textOffsetX += getTextWidth(prefixText, Font.font(textFontSize)) + 4.0
        }


        gc.fill = textColor
        val inputFont = Font.font(textFontSize)
        gc.font = inputFont

        val realText = if (passwordMode)
            "•".repeat(text.length)
        else text

        val safeCursor = cursorIndex.coerceIn(0, text.length)

        val before = realText.substring(0, safeCursor)
        val after  = realText.substring(safeCursor)

        // 绘制光标前文本
        gc.fillText(before, textOffsetX, textBaselineY)

        val beforeWidth = getTextWidth(before, inputFont)

        var composedWidth = 0.0


        if (composedText.isNotEmpty() && !passwordMode) {

            gc.fillText(composedText, textOffsetX + beforeWidth, textBaselineY)

            composedWidth = getTextWidth(composedText, inputFont)

            gc.stroke = activeColor
            gc.lineWidth = 1.0
            gc.strokeLine(
                textOffsetX + beforeWidth,
                textBaselineY + 2,
                textOffsetX + beforeWidth + composedWidth,
                textBaselineY + 2
            )
        }

        // 绘制光标后文本
        gc.fillText(
            after,
            textOffsetX + beforeWidth + composedWidth,
            textBaselineY
        )

        // ───── 光标绘制 ─────
        if (hasFocus && enabled && cursorVisible) {
            val cursorX = textOffsetX + beforeWidth
            gc.fill = activeColor
            gc.fillRect(
                cursorX,
                textBaselineY - textFontSize,
                1.5,
                textFontSize + 2
            )
        }

        if (suffixText.isNotEmpty() && (hasFocus || text.isNotEmpty())) {
            gc.fill = textColor.deriveColor(0.0, 1.0, 1.0, 0.6)
            gc.font = inputFont
            val sufW = getTextWidth(suffixText, inputFont)
            gc.fillText(
                suffixText,
                w - horizontalPad - sufW,
                textBaselineY
            )
        }

        val hasSupportRow =
            supportingText.isNotEmpty() || isError || showCharCounter

        if (hasSupportRow) {
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
                val counterW = getTextWidth(counterStr, Font.font(supportFontSize))
                gc.fillText(counterStr, w - horizontalPad - counterW, rowY)
            }
        }
    }


    // =====================================================================
    // Input handling
    // =====================================================================

    override fun onTouchEvent(event: MouseEvent): Boolean {
        if (!enabled) return false

        if (event.eventType == MouseEvent.MOUSE_PRESSED) {
            requestFocus()
            val inputFont = Font.font(textFontSize)
            val textStartX = horizontalPad + 8
            val clickX = event.x - textStartX

            var accumulatedWidth = 0.0
            cursorIndex = text.length

            for (i in text.indices) {
                val charWidth = getTextWidth(text[i].toString(), inputFont)
                if (clickX < accumulatedWidth + charWidth / 2) {
                    cursorIndex = i
                    break
                }
                accumulatedWidth += charWidth
            }


            invalidate()
            return true
        }
        return false
    }


    override fun onKeyPressed(event: KeyEvent) {
        if (!enabled || !hasFocus) return

        when (event.code) {

            KeyCode.LEFT -> {
                if (cursorIndex > 0) {
                    cursorIndex--
                    invalidate()
                }
            }

            KeyCode.RIGHT -> {
                if (cursorIndex < text.length) {
                    cursorIndex++
                    invalidate()
                }
            }

            KeyCode.BACK_SPACE -> {
                if (cursorIndex > 0) {
                    text = text.removeRange(cursorIndex - 1, cursorIndex)
                    cursorIndex--
                    invalidate()
                }
            }

            KeyCode.DELETE -> {
                if (cursorIndex < text.length) {
                    text = text.removeRange(cursorIndex, cursorIndex + 1)
                    invalidate()
                }
            }

            KeyCode.ESCAPE -> clearFocus()
            else -> {}
        }
    }


    override fun onKeyTypeInput(event: KeyEvent) {
        if (!enabled || !hasFocus) return

        val ch = event.character
        if (ch.isNotEmpty() && !ch[0].isISOControl()) {
            if (maxLength < 0 || text.length < maxLength) {
                text = text.substring(0, cursorIndex) +
                        ch +
                        text.substring(cursorIndex)
                cursorIndex += ch.length
                invalidate()
            }
        }
    }

    override fun onInputMethodEvent(event: InputMethodEvent) {
        if (!enabled || !hasFocus) return

        val committed = event.committed
        if (committed.isNotEmpty()) {
            text = text.substring(0, cursorIndex) +
                    committed +
                    text.substring(cursorIndex)
            cursorIndex += committed.length
            composedText = ""
            invalidate()
        }

        composedText = event.composed.joinToString("") { it.text }
        composedCaret = event.caretPosition
        invalidate()
    }



    override fun onKeyReleased(event: KeyEvent) {
        super.onKeyReleased(event)
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Any?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        animateFocus(gainFocus)
        updateLabelFloat(animate = true)
    }

    // =====================================================================
    // Animations
    // =====================================================================

    // ── Cursor blink ─────────────────────────────────────────────────────
    private fun startCursorBlink() {
        cursorVisible = true
        cursorTimeline?.stop()
        cursorTimeline = Timeline(
            KeyFrame(Duration.millis(500.0), { cursorVisible = false; invalidate() }),
            KeyFrame(Duration.millis(1000.0), { cursorVisible = true;  invalidate() })
        ).also {
            it.cycleCount = Animation.INDEFINITE
            it.play()
        }
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
            init {
                cycleDuration = Duration.millis(150.0)
                interpolator  = Interpolator.EASE_BOTH
            }
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

        if (!animate) {
            labelProgress = target
            invalidate()
            return
        }

        labelTransition?.stop()
        val start = labelProgress
        labelTransition = object : Transition() {
            init {
                cycleDuration = Duration.millis(150.0)
                interpolator  = Interpolator.EASE_BOTH
            }
            override fun interpolate(frac: Double) {
                labelProgress = start + (target - start) * frac
                invalidate()
            }
        }.also { it.play() }
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

    private fun lerp(a: Color, b: Color, t: Float): Color {
        val tt = t.toDouble().coerceIn(0.0, 1.0)
        return Color(
            a.red   + (b.red   - a.red)   * tt,
            a.green + (b.green - a.green) * tt,
            a.blue  + (b.blue  - a.blue)  * tt,
            a.opacity + (b.opacity - a.opacity) * tt
        )
    }

    private fun getTextWidth(str: String, font: Font): Double {
        val t = Text(str)
        t.font = font
        return t.layoutBounds.width
    }


}