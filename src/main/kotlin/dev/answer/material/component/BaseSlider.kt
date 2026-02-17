/*
 * Copyright (C) 2026 AnswerDev
 * Licensed under the GNU General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.gnu.org/licenses/gpl-3.0.html
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
import dev.answer.material.theme.ColorPalette
import dev.answer.material.theme.Theme
import dev.answer.material.view.View
import dev.answer.material.view.measure.MeasureSpec
import javafx.animation.Interpolator
import javafx.animation.Transition
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.util.Duration
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/**
 *
 * @author AnswerDev
 * @date 2026/2/17 03:01
 * @description MaterialSlider - Optimized for Focus and Touch
 */

class BaseSlider(
    context: Context,
    private var minValue: Double = 0.0,
    private var maxValue: Double = 100.0,
    private var step: Double = 1.0,
    value: Double = minValue,
) : View(context) {

    companion object {
        private const val ANIMATION_DURATION = 200.0

        // ── M3 handle (thumb) ──────────────────────────────────────────────
        // In M3 the active handle is a rounded rectangle (pill), not a circle.
        // Default token values (m3_comp_slider_active_handle_*):
        //   width  ≈ 4dp  (narrow pill when resting, wider on press)
        //   height ≈ 44dp (tall pill)
        // We model it as a rounded rect whose corner radius = width/2.
        /** Resting handle width (dp-equivalent pixels). */
        private const val HANDLE_WIDTH  = 4.0
        /** Handle height (dp-equivalent pixels). */
        private const val HANDLE_HEIGHT = 44.0
        /** Handle width when pressed — matches THUMB_WIDTH_PRESSED_RATIO = 0.5 × full width in AOSP. */
        private const val HANDLE_WIDTH_PRESSED = 8.0

        // ── M3 halo ────────────────────────────────────────────────────────
        // M3 sets haloColor = #00FFFFFF (fully transparent).
        // We keep the constant for focus ring calculations but never paint it.
        private const val HALO_RADIUS = 20.0

        // ── Track geometry ─────────────────────────────────────────────────
        // Reference screenshot: track height ~37 % of handle height (44 px) = 16 px.
        // Matches M3 token m3_comp_slider_inactive_track_height = 16dp.
        /** Height of the inactive track segment — full-round capsule. */
        private const val TRACK_HEIGHT_INACTIVE = 16.0
        /** Height of the active track segment — same capsule height. */
        private const val TRACK_HEIGHT_ACTIVE   = 16.0
        /** Corner radius on outer track edges: full-round = height/2 = 8. */
        private const val TRACK_CORNER_SIZE = TRACK_HEIGHT_INACTIVE / 2
        /**
         * Inner corner radius visible through the gap (trackInsideCornerSize = 2dp in M3).
         */
        private const val TRACK_INSIDE_CORNER_SIZE = 2.0
        /**
         * Gap between handle edge and track cut-end.
         * Proportionally tighter against the 16 px track: 4 px.
         */
        private const val THUMB_TRACK_GAP_SIZE = 4.0

        // ── Stop indicator ─────────────────────────────────────────────────
        /**
         * Diameter of the stop-indicator dot at the track boundary.
         * Set to TRACK_CORNER_SIZE (= 8 px) so it fills the rounded cap of the track.
         */
        private const val STOP_INDICATOR_SIZE = TRACK_CORNER_SIZE

        // ── Tick marks ─────────────────────────────────────────────────────
        /** Radius of tick marks on the active portion of the track. */
        private const val TICK_RADIUS_ACTIVE   = 2.0
        /** Radius of tick marks on the inactive portion of the track. */
        private const val TICK_RADIUS_INACTIVE = 2.0

        // ── Value bubble ───────────────────────────────────────────────────
        private const val BUBBLE_WIDTH  = 48.0
        private const val BUBBLE_HEIGHT = 28.0
        /** Gap between the bottom of the bubble tail and the top of the handle. */
        private const val BUBBLE_OFFSET = 8.0
    }

    // ── State ──────────────────────────────────────────────────────────────
    private var isDragging = false

    /** Logical progress in [0, 1]. */
    private var progress = normalizeValue(value)
    /** Animated progress used for rendering. */
    private var visualProgress = progress
    /** Bubble animation opacity [0, 1]. */
    private var bubbleOpacity = 0.0

    // ── Animations ────────────────────────────────────────────────────────
    private var progressTransition: Transition? = null
    private var bubbleTransition: Transition? = null

    // ── Public callbacks ──────────────────────────────────────────────────
    var onValueChangeListener: ((Double) -> Unit)? = null

    // ── M3 colours ────────────────────────────────────────────────────────
    var trackInactiveColor: Color? = null
    var trackActiveColor: Color? = null
    var haloColor: Color? = null
    var thumbColor: Color? = null
    var tickActiveColor: Color? = null
    var tickInactiveColor: Color? = null


    /** Stop-indicator dot colour (same as inactive tick by convention). */
    var stopIndicatorColor: Color? get() = tickInactiveColor; set(_) {}
    /** Bubble background colour — follows active track colour. */
    var bubbleColor: Color? get() = trackActiveColor; set(_) {}

    init {
        clickable = true
        focusable  = true
        minimumWidth  = 120
        // Handle is 44 px tall; add 8 px (4 top + 4 bottom) so it is never clipped.
        minimumHeight = (HANDLE_HEIGHT + 8).toInt()
        applyDynamicColors(context.colorScheme);
    }


    fun applyDynamicColors(
        palette: ColorPalette,
        enabled: Boolean = true
    ) {
        // ── Active Track & Thumb ─────────────────────
        thumbColor = palette.primary
        trackActiveColor = palette.primary

        // ── Inactive Track ───────────────────────────
        trackInactiveColor = if (palette.isDarkMode) {
            palette.surfaceContainerLow
        } else {
            palette.surfaceContainerHighest
        }

        // ── Tick Marks ───────────────────────────────
        tickActiveColor =
            palette.onPrimary.deriveColor(0.0, 1.0, 1.0, 0.38)

        tickInactiveColor =
            palette.onSurfaceVariant.deriveColor(0.0, 1.0, 1.0, 0.38)

        // ── Halo (M3 = 12% primary) ──────────────────
        haloColor =
            palette.primary.deriveColor(0.0, 1.0, 1.0, 0.12)

        // ── Disabled state ───────────────────────────
        if (!enabled) {
            val disabledAlpha = 0.38

            thumbColor = thumbColor?.deriveColor(0.0, 1.0, 1.0, disabledAlpha)
            trackActiveColor = trackActiveColor?.deriveColor(0.0, 1.0, 1.0, disabledAlpha)
            trackInactiveColor = trackInactiveColor?.deriveColor(0.0, 1.0, 1.0, disabledAlpha)
            tickActiveColor = tickActiveColor?.deriveColor(0.0, 1.0, 1.0, disabledAlpha)
            tickInactiveColor = tickInactiveColor?.deriveColor(0.0, 1.0, 1.0, disabledAlpha)
        }
    }


    override fun onThemeChanged(theme: Theme) {
        super.onThemeChanged(theme)
        applyDynamicColors(theme.colorScheme, enabled)
        invalidate()

    }

    // =====================================================================
    // Public API
    // =====================================================================

    fun setValue(newValue: Double, animate: Boolean = true) {
        val clamped     = newValue.coerceIn(minValue, maxValue)
        val newProgress = normalizeValue(clamped)
        if (progress != newProgress) {
            progress = newProgress
            if (animate && !isDragging) startProgressAnimation(newProgress)
            else { visualProgress = newProgress; invalidate() }
            notifyValueChange()
        }
    }

    fun getValue(): Double = denormalizeValue(progress)

    // =====================================================================
    // Drawing — M3 style
    // =====================================================================

    override fun onDraw(gc: GraphicsContext) {
        // ── Coordinate system (Android-standard) ──────────────────────────
        // trackStartX / trackEndX mark where the handle *centre* sits at
        // progress=0 and progress=1. They are flush with the padding edges —
        // no extra inset. The handle pill visually extends beyond the track
        // ends, which is exactly what Android's BaseSlider does.
        // Inset by handleHalfW so the pill never overflows when paddingLeft=0,
        // matching Android's trackSidePadding >= thumbRadius convention.
        val handleW      = if (isDragging) HANDLE_WIDTH_PRESSED else HANDLE_WIDTH
        val handleHalfW  = handleW / 2.0
        val trackStartX  = paddingLeft  + handleHalfW
        val trackEndX    = width        - paddingRight - handleHalfW
        val trackLength  = (trackEndX - trackStartX).coerceAtLeast(1.0)
        val trackCenterY = height / 2.0

        // Handle centre X — maps [0,1] onto [trackStartX, trackEndX]
        val handleCenterX = trackStartX + trackLength * visualProgress

        val handleH = HANDLE_HEIGHT
        val handleLeft   = handleCenterX - handleW / 2.0
        val handleTop    = trackCenterY  - handleH / 2.0
        val handleCorner = handleW / 2.0   // full-round pill

        // Gap between handle edge and track end (M3: thumbTrackGapSize)
        val gap = THUMB_TRACK_GAP_SIZE

        // ── 1. Inactive track — left segment ─────────────────────────────
        val inactiveLeftEnd = handleLeft - gap
        if (inactiveLeftEnd > trackStartX) {
            gc.fill = trackInactiveColor
            fillRoundedRect(
                gc,
                trackStartX, trackCenterY - TRACK_HEIGHT_INACTIVE / 2,
                inactiveLeftEnd - trackStartX, TRACK_HEIGHT_INACTIVE,
                outerLeft = TRACK_CORNER_SIZE, outerRight = TRACK_INSIDE_CORNER_SIZE
            )
        }

        // ── 2. Active track ───────────────────────────────────────────────
        val activeStart = trackStartX
        val activeEnd   = handleLeft - gap
        if (activeEnd > activeStart) {
            gc.fill = trackActiveColor
            fillRoundedRect(
                gc,
                activeStart, trackCenterY - TRACK_HEIGHT_ACTIVE / 2,
                activeEnd - activeStart, TRACK_HEIGHT_ACTIVE,
                outerLeft = TRACK_CORNER_SIZE, outerRight = TRACK_INSIDE_CORNER_SIZE
            )
        }

        // ── 3. Inactive track — right segment ────────────────────────────
        val inactiveRightStart = handleCenterX + handleW / 2.0 + gap
        // trackEndX is the rightmost point the handle centre can reach,
        // which equals the right end of the track rectangle.
        val inactiveRightEnd   = trackEndX
        if (inactiveRightEnd > inactiveRightStart) {
            gc.fill = trackInactiveColor
            fillRoundedRect(
                gc,
                inactiveRightStart, trackCenterY - TRACK_HEIGHT_INACTIVE / 2,
                inactiveRightEnd - inactiveRightStart, TRACK_HEIGHT_INACTIVE,
                outerLeft = TRACK_INSIDE_CORNER_SIZE, outerRight = TRACK_CORNER_SIZE
            )
        }

        // ── 4. Stop indicators ────────────────────────────────────────────
        if (STOP_INDICATOR_SIZE > 0) {
            val r = STOP_INDICATOR_SIZE / 2.0
            gc.fill = stopIndicatorColor
            // Left stop: centre inside the left track cap
            if (visualProgress > 0.001) {
                gc.fillOval(trackStartX + r, trackCenterY - r, STOP_INDICATOR_SIZE, STOP_INDICATOR_SIZE)
            }
            // Right stop: centre inside the right track cap
            if (visualProgress < 0.999) {
                gc.fillOval(trackEndX - r - STOP_INDICATOR_SIZE, trackCenterY - r, STOP_INDICATOR_SIZE, STOP_INDICATOR_SIZE)
            }
        }

        // ── 5. Tick marks ─────────────────────────────────────────────────
        if (step > 0 && (maxValue - minValue) / step < 50) {
            drawTicks(gc, trackStartX, trackCenterY, trackLength, handleCenterX, gap)
        }

        // ── 6. Halo (focus ring) ──────────────────────────────────────────
        // M3 sets haloColor = #00FFFFFF → fully transparent; we honour that default.
        // If the integrator explicitly sets a non-transparent haloColor, we draw it.
        if ((isDragging || hasFocus) && haloColor != Color.TRANSPARENT) {
            gc.fill = haloColor
            gc.fillOval(
                handleCenterX - HALO_RADIUS,
                trackCenterY  - HALO_RADIUS,
                HALO_RADIUS * 2,
                HALO_RADIUS * 2
            )
        }

        // ── 7. Handle (thumb) — M3 pill shape, no elevation ──────────────
        // M3: thumbElevation = 0dp → no shadow needed.
        gc.fill = thumbColor
        gc.fillRoundRect(handleLeft, handleTop, handleW, handleH, handleCorner * 2, handleCorner * 2)

        // ── 8. Value bubble ───────────────────────────────────────────────
        if (bubbleOpacity > 0.01) {
            drawBubble(gc, handleCenterX, handleTop - BUBBLE_OFFSET)
        }
    }



    /**
     * Fills a rectangle with independent left/right corner radii.
     *
     * The original arcTo implementation was wrong — JavaFX arcTo uses SVG-style
     * tangent arcs, not corner arcs, so the curves came out misshapen.
     *
     * This version uses cubic Bézier curves to approximate quarter-circle corners
     * (control-point ratio k ≈ 0.5523), which is the standard canvas approach.
     *
     *   tl = top-left radius,  tr = top-right radius
     *   br = bottom-right,     bl = bottom-left
     */
    private fun fillRoundedRect(
        gc: GraphicsContext,
        x: Double, y: Double, w: Double, h: Double,
        outerLeft: Double, outerRight: Double,
    ) {
        if (w <= 0 || h <= 0) return
        // Map our 2-param signature to 4 corners:
        // left edge  → tl & bl = outerLeft
        // right edge → tr & br = outerRight
        val tl = outerLeft.coerceAtMost(h / 2).coerceAtMost(w / 2)
        val bl = tl
        val tr = outerRight.coerceAtMost(h / 2).coerceAtMost(w / 2)
        val br = tr
        val k  = 0.5523          // cubic Bézier approximation of a quarter circle

        gc.beginPath()
        // top edge — left to right
        gc.moveTo(x + tl, y)
        gc.lineTo(x + w - tr, y)
        // top-right corner
        gc.bezierCurveTo(x + w - tr + tr * k, y,
            x + w, y + tr - tr * k,
            x + w, y + tr)
        // right edge
        gc.lineTo(x + w, y + h - br)
        // bottom-right corner
        gc.bezierCurveTo(x + w, y + h - br + br * k,
            x + w - br + br * k, y + h,
            x + w - br, y + h)
        // bottom edge — right to left
        gc.lineTo(x + bl, y + h)
        // bottom-left corner
        gc.bezierCurveTo(x + bl - bl * k, y + h,
            x, y + h - bl + bl * k,
            x, y + h - bl)
        // left edge
        gc.lineTo(x, y + tl)
        // top-left corner
        gc.bezierCurveTo(x, y + tl - tl * k,
            x + tl - tl * k, y,
            x + tl, y)
        gc.closePath()
        gc.fill()
    }

    private fun drawTicks(
        gc: GraphicsContext,
        startX: Double, centerY: Double,
        totalWidth: Double,
        handleCenterX: Double,
        gap: Double,
    ) {
        val count = ((maxValue - minValue) / step).toInt()
        for (i in 0..count) {
            val tickX = startX + (i.toDouble() / count) * totalWidth
            // Skip ticks obscured by the handle + gap zone
            if (abs(tickX - handleCenterX) < (HANDLE_WIDTH / 2.0 + gap)) continue

            val isActive = tickX <= handleCenterX
            if (isActive) {
                gc.fill = tickActiveColor
                val r = TICK_RADIUS_ACTIVE
                gc.fillOval(tickX - r, centerY - r, r * 2, r * 2)
            } else {
                gc.fill = tickInactiveColor
                val r = TICK_RADIUS_INACTIVE
                gc.fillOval(tickX - r, centerY - r, r * 2, r * 2)
            }
        }
    }

    private fun drawBubble(gc: GraphicsContext, x: Double, bottomY: Double) {
        gc.save()
        gc.globalAlpha = bubbleOpacity

        val bubbleW = BUBBLE_WIDTH
        val bubbleH = BUBBLE_HEIGHT
        val topY  = bottomY - bubbleH
        val leftX = x - bubbleW / 2

        // Bubble background (rounded rect + tail)
        gc.fill = bubbleColor
        gc.beginPath()
        gc.moveTo(leftX + 4, topY)
        gc.lineTo(leftX + bubbleW - 4, topY)
        gc.arcTo(leftX + bubbleW, topY, leftX + bubbleW, topY + 4, 4.0)
        gc.lineTo(leftX + bubbleW, topY + bubbleH - 4)
        gc.arcTo(leftX + bubbleW, topY + bubbleH, leftX + bubbleW - 4, topY + bubbleH, 4.0)
        gc.lineTo(x + 5, topY + bubbleH)
        gc.lineTo(x, topY + bubbleH + 5)
        gc.lineTo(x - 5, topY + bubbleH)
        gc.lineTo(leftX + 4, topY + bubbleH)
        gc.arcTo(leftX, topY + bubbleH, leftX, topY + bubbleH - 4, 4.0)
        gc.lineTo(leftX, topY + 4)
        gc.arcTo(leftX, topY, leftX + 4, topY, 4.0)
        gc.closePath()
        gc.fill()

        // Bubble text
        gc.fill = Color.WHITE
        val font = Font("Arial", 11.0)
        gc.font = font
        val text      = formatBubbleValue(getValue())
        val textWidth = getTextWidth(text, font)
        gc.fillText(text, x - textWidth / 2, topY + bubbleH / 2 + 4)

        gc.restore()
    }

    // =====================================================================
    // Input handling
    // =====================================================================

    override fun onTouchEvent(event: MouseEvent): Boolean {
        if (!enabled) return false

        // Mirror onDraw: always use resting HANDLE_WIDTH for the inset so the
        // coordinate origin is stable across PRESSED→DRAGGED state transition.
        val handleHalfW = HANDLE_WIDTH / 2.0
        val trackStartX = paddingLeft  + handleHalfW
        val trackEndX   = width        - paddingRight - handleHalfW
        val trackLength = (trackEndX - trackStartX).coerceAtLeast(1.0)
        val rawProgress = ((event.x - trackStartX) / trackLength).coerceIn(0.0, 1.0)

        when (event.eventType) {
            MouseEvent.MOUSE_PRESSED -> {
                requestFocus()
                isDragging = true
                progressTransition?.stop()
                animateBubble(true)
                updateProgressFromInput(rawProgress)
                return true
            }
            MouseEvent.MOUSE_DRAGGED -> {
                if (isDragging) updateProgressFromInput(rawProgress)
                return true
            }
            MouseEvent.MOUSE_RELEASED -> {
                if (isDragging) {
                    isDragging = false
                    animateBubble(false)
                    setValue(denormalizeValue(snapToStep(progress)), animate = true)
                    // 松手后主动清除焦点：拖拽结束即视为交互完成，
                    // 防止 halo 在点击空白区域时无法消除
//                    clearFocus()
                    invalidate()
                }
                return true
            }
            MouseEvent.MOUSE_EXITED -> {
                // 鼠标移出：如果正在拖拽则取消并清焦点
                if (isDragging) {
//                    isDragging = false
                    animateBubble(false)
                    setValue(denormalizeValue(snapToStep(progress)), animate = true)
                    clearFocus()
                    invalidate()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onKeyPressed(event: KeyEvent) {
        if (!enabled) return
        when (event.code) {
            KeyCode.LEFT, KeyCode.DOWN -> { setValue(getValue() - step, animate = true); event.consume() }
            KeyCode.RIGHT, KeyCode.UP  -> { setValue(getValue() + step, animate = true); event.consume() }
            KeyCode.HOME               -> { setValue(minValue,           animate = true); event.consume() }
            KeyCode.END                -> { setValue(maxValue,           animate = true); event.consume() }
            else -> super.onKeyPressed(event)
        }
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Any?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        invalidate()
    }

    // =====================================================================
    // Internal helpers
    // =====================================================================

    private fun updateProgressFromInput(rawProgress: Double) {
        val snapped = snapToStep(rawProgress)
        if (abs(snapped - progress) > 0.0001) {
            progress       = snapped
            visualProgress = snapped   // no animation lag during drag
            invalidate()
            notifyValueChange()
        }
    }

    private fun normalizeValue(v: Double) = (v - minValue) / (maxValue - minValue)

    private fun denormalizeValue(p: Double): Double {
        val v = p * (maxValue - minValue) + minValue
        return (v * 10000).roundToInt() / 10000.0
    }

    private fun snapToStep(p: Double): Double {
        if (step <= 0) return p
        val currentVal = denormalizeValue(p)
        val steps      = ((currentVal - minValue) / step).roundToInt()
        val snapped    = (minValue + steps * step).coerceIn(minValue, maxValue)
        return normalizeValue(snapped)
    }

    private fun notifyValueChange() {
        onValueChangeListener?.invoke(getValue())
    }

    private fun formatBubbleValue(v: Double): String =
        if (step >= 1.0 || (v.toLong().toDouble() == v)) v.toLong().toString()
        else "%.${decimalPlaces(step)}f".format(v)

    private fun decimalPlaces(d: Double): Int {
        val s = d.toBigDecimal().stripTrailingZeros().toPlainString()
        val dot = s.indexOf('.')
        return if (dot < 0) 0 else s.length - dot - 1
    }

    private fun getTextWidth(text: String, font: Font): Double {
        val t = Text(text); t.font = font; return t.layoutBounds.width
    }

    private fun startProgressAnimation(target: Double) {
        progressTransition?.stop()
        val start = visualProgress
        progressTransition = object : Transition() {
            init { cycleDuration = Duration.millis(ANIMATION_DURATION); interpolator = Interpolator.EASE_BOTH }
            override fun interpolate(frac: Double) { visualProgress = start + (target - start) * frac; invalidate() }
        }.also { it.play() }
    }

    private fun animateBubble(show: Boolean) {
        bubbleTransition?.stop()
        val target = if (show) 1.0 else 0.0
        if (abs(bubbleOpacity - target) < 0.01) return
        val start = bubbleOpacity
        bubbleTransition = object : Transition() {
            init { cycleDuration = Duration.millis(150.0); interpolator = Interpolator.EASE_OUT }
            override fun interpolate(frac: Double) { bubbleOpacity = start + (target - start) * frac; invalidate() }
        }.also { it.play() }
    }

    // =====================================================================
    // Measurement
    // =====================================================================

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val desiredWidth  = 200
        // Must be at least HANDLE_HEIGHT + vertical breathing room so the pill is never clipped.
        val desiredHeight = (HANDLE_HEIGHT + 8).toInt()

        val w = when (MeasureSpec.getMode(widthSpec)) {
            MeasureSpec.EXACTLY  -> MeasureSpec.getSize(widthSpec)
            // Width can shrink to fit parent, but respect desiredWidth as a hint.
            MeasureSpec.AT_MOST  -> min(MeasureSpec.getSize(widthSpec), desiredWidth)
            else                 -> desiredWidth
        }
        val h = when (MeasureSpec.getMode(heightSpec)) {
            MeasureSpec.EXACTLY  -> MeasureSpec.getSize(heightSpec)
            // Height must be at least desiredHeight — use max, not min.
            MeasureSpec.AT_MOST  -> desiredHeight  // always allocate full handle height
            else                 -> desiredHeight
        }
        super.onMeasure(w, h)
    }
}