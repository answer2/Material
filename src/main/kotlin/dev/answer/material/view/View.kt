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
package dev.answer.material.view

import dev.answer.material.content.Context
import dev.answer.material.graphics.Drawable
import dev.answer.material.view.animation.AnimationListener
import dev.answer.material.view.animation.ViewAnimation
import dev.answer.material.view.measure.MeasureSpec
import javafx.animation.Animation
import javafx.animation.ParallelTransition
import javafx.animation.PauseTransition
import javafx.animation.SequentialTransition
import javafx.animation.Timeline
import javafx.event.EventHandler
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseEvent
import javafx.util.Duration

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 00:48
 * @description View - Fixed Version
 */
open class View(val context: Context) {

    // 布局参数
    var layoutParams: LayoutParams = LayoutParams()
    var background: Drawable? = null
        set(value) {
            field = value
            invalidate()
        }

    // 内边距
    var paddingLeft = 0.0
    var paddingTop = 0.0
    var paddingRight = 0.0
    var paddingBottom = 0.0

    // 可见性
    var visibility: Int = VISIBLE
        set(value) {
            if (field != value) {
                field = value
                onVisibilityChanged(this, value)
            }
        }

    // 状态
    var enabled: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                onEnabledChanged(value)
            }
        }

    var clickable: Boolean = false
    var focusable: Boolean = false
    var hasFocus: Boolean = false
    var selected: Boolean = false

    // 滚动
    var scrollX: Double = 0.0
    var scrollY: Double = 0.0

    // 动画属性
    var alpha: Double = 1.0
        set(value) {
            field = value
            invalidate()
        }

    var rotation: Double = 0.0
        set(value) {
            field = value
            invalidate()
        }

    var scaleX: Double = 1.0
        set(value) {
            field = value
            invalidate()
        }

    var scaleY: Double = 1.0
        set(value) {
            field = value
            invalidate()
        }

    var translateX: Double = 0.0
        set(value) {
            field = value
            invalidate()
        }

    var translateY: Double = 0.0
        set(value) {
            field = value
            invalidate()
        }

    // 标签
    var tag: Any? = null

    // 视图树
    var parent: View? = null
    protected val children: MutableList<View> = mutableListOf()


    var onClickListener: OnClickListener? = null
        set(value) {
            field = value
            if (value != null) {
                clickable = true
            }
        }

    var onLongClickListener: OnLongClickListener? = null
        set(value) {
            field = value
            if (value != null) {
                clickable = true
            }
        }

    var onTouchListener: OnTouchListener? = null
    var onFocusChangeListener: OnFocusChangeListener? = null

    // 长按事件检测相关
    private var longPressTimeout = 500L // 长按超时时间（毫秒）
    private var isLongPressDetected = false
    private val longPressPause = PauseTransition(Duration.millis(longPressTimeout.toDouble()))

    // 触摸事件状态
    protected var isPressed = false
        private set
    protected var isHovered = false
        private set

    // 动画相关
    private val runningAnimations: MutableList<Animation> = mutableListOf()


    init {
        longPressPause.onFinished = EventHandler {
            if (isPressed) {
                isLongPressDetected = true
                performLongClick()
            }
        }
    }

    // 内边距设置方法
    fun setPadding(left: Double, top: Double, right: Double, bottom: Double) {
        paddingLeft = left
        paddingTop = top
        paddingRight = right
        paddingBottom = bottom
    }

    fun setPadding(all: Double) {
        setPadding(all, all, all, all)
    }

    // 测量相关
    var measuredWidth: Double = 0.0
        protected set

    var measuredHeight: Double = 0.0
        protected set

    // 位置相关
    var left = 0.0
        protected set
    var top = 0.0
        protected set
    var right = 0.0
        protected set
    var bottom = 0.0
        protected set

    var width: Double = 0.0
        get() = right - left

    var height: Double = 0.0
        get() = bottom - top

    val x: Double
        get() = left

    val y: Double
        get() = top

    // 测量方法
    open fun onMeasure(widthSpec: Int, heightSpec: Int) {
        measuredWidth = getDefaultSize(suggestedMinimumWidth, widthSpec)
        measuredHeight = getDefaultSize(suggestedMinimumHeight, heightSpec)
    }

    fun measure(widthSpec: Int, heightSpec: Int) {
        onMeasure(widthSpec, heightSpec)
    }

    protected fun resolveSize(desired: Double, measureSpec: Int): Double {

        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec).toDouble()

        return when (mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> minOf(desired, size)
            MeasureSpec.UNSPECIFIED -> desired
            else -> desired
        }
    }


    // 布局方法
    open fun onLayout(l: Double, t: Double, r: Double, b: Double) {
        left = l
        top = t
        right = r
        bottom = b
    }

    fun layout(l: Double, t: Double, r: Double, b: Double) {
        onLayout(l, t, r, b)
    }

    // 绘制方法
    open fun onDraw(gc: GraphicsContext) {}

    open fun draw(gc: GraphicsContext) {
        if (visibility == GONE) return

        // 保存画布状态
        gc.save()

        // 应用透明度
        gc.globalAlpha = alpha

        // 应用变换
        gc.translate(left + translateX, top + translateY)
        gc.rotate(rotation)
        gc.scale(scaleX, scaleY)

        // 绘制背景
        background?.draw(
            gc,
            0.0,
            0.0,
            width,
            height
        )

        onDraw(gc)

        // 恢复画布状态
        gc.restore()

        // 绘制子视图
        drawChildren(gc)
    }

    // 绘制子视图
    protected open fun drawChildren(gc: GraphicsContext) {
        for (child in children) {
            child.draw(gc)
        }
    }

    // 事件处理
    open fun dispatchTouchEvent(event: MouseEvent): Boolean {
        if (onInterceptTouchEvent(event)) {
            return onTouchEvent(event)
        }
        // 倒序遍历（处理 Z-order，最上面的 View 先收到事件）
        for (child in children.reversed()) {
            // 使用全局坐标进行命中测试
            if (child.hitTest(event.x, event.y) && child.dispatchTouchEvent(event)) {
                return true
            }
        }
        return onTouchEvent(event)
    }

    open fun onInterceptTouchEvent(event: MouseEvent): Boolean {
        return false
    }

    open fun onTouchEvent(event: MouseEvent): Boolean {

        if (visibility != VISIBLE || !enabled) return false

        // 优先处理 TouchListener
        if (onTouchListener?.onTouch(this, event) == true) {
            return true
        }

        // 如果不可点击且没有长按监听，且没有其他交互需求，则不消耗事件
        if (!clickable && onLongClickListener == null) return false

        when (event.eventType) {

            MouseEvent.MOUSE_PRESSED -> {
                // 再次确认点击位置在当前 View 内
                if (!hitTest(event.x, event.y)) return false

                isPressed = true
                isLongPressDetected = false
                startLongPressTimer()

                // 可以添加 invalidate() 来触发重绘以显示按下状态
                return true
            }

            MouseEvent.MOUSE_RELEASED -> {
                cancelLongPressTimer()

                val inside = hitTest(event.x, event.y)

                // 修复：必须是 按下状态(isPressed) 且 仍在范围内(inside) 且 未触发长按
                if (isPressed && inside && !isLongPressDetected) {
                    performClick()
                }

                isPressed = false
                return true
            }

            MouseEvent.MOUSE_DRAGGED -> {
                val inside = hitTest(event.x, event.y)

                // 如果拖拽出了 View 的范围
                if (!inside) {
                    // 取消长按
                    cancelLongPressTimer()
                    // 视作取消点击意图，重置按下状态
                    isPressed = false
                }
                return true
            }

            MouseEvent.MOUSE_EXITED -> {
                // 作为一个额外的安全网，鼠标移出也取消状态
                isPressed = false
                cancelLongPressTimer()
            }

            else -> {}
        }

        return false
    }


    // 点击事件
    open fun performClick(): Boolean {
        onClickListener?.onClick(this)
        return true
    }

    // 长按事件
    open fun performLongClick(): Boolean {
        return onLongClickListener?.onLongClick(this) ?: false
    }

    // 启动长按定时器
    private fun startLongPressTimer() {
        longPressPause.playFromStart()
    }

    // 取消长按定时器
    private fun cancelLongPressTimer() {
        longPressPause.stop()
    }

    // 焦点管理
    open fun requestFocus(): Boolean {
        if (focusable && !hasFocus) {
            hasFocus = true
            onFocusChanged(true, 0, null)
            return true
        }
        return false
    }

    open fun clearFocus() {
        if (hasFocus) {
            hasFocus = false
            onFocusChanged(false, 0, null)
        }
    }

    // 滚动方法
    open fun scrollBy(x: Double, y: Double) {
        scrollX += x
        scrollY += y
        invalidate()
    }

    open fun scrollTo(x: Double, y: Double) {
        if (scrollX != x || scrollY != y) {
            scrollX = x
            scrollY = y
            invalidate()
        }
    }

    // 视图树操作
    open fun addView(child: View) {
        children.add(child)
        child.parent = this
        requestLayout()
        invalidate()
    }

    open fun removeView(child: View) {
        if (children.remove(child)) {
            child.parent = null
            requestLayout()
            invalidate()
        }
    }

    open fun removeAllViews() {
        for (child in children) {
            child.parent = null
        }
        children.clear()
        requestLayout()
        invalidate()
    }

    // 位置和大小工具方法
    open fun getDefaultSize(size: Int, measureSpec: Int): Double {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec).toDouble()

        return when (specMode) {
            MeasureSpec.EXACTLY -> specSize
            MeasureSpec.AT_MOST -> Math.min(size.toDouble(), specSize)
            else -> size.toDouble()
        }
    }

    open val suggestedMinimumWidth: Int
        get() = 0

    open val suggestedMinimumHeight: Int
        get() = 0

    // 无效化和请求布局
    open fun invalidate() {
        // 通知父视图需要重绘
        parent?.invalidate()
    }

    open fun requestLayout() {
        // 通知父视图需要重新布局
        parent?.requestLayout()
    }

    /**
     * 获取视图在窗口（场景）中的绝对位置（考虑平移变换）
     */
    open fun getLocationInWindow(): Pair<Double, Double> {
        var x = left + translateX
        var y = top + translateY
        var p = parent
        while (p != null) {
            x += p.left + p.translateX
            y += p.top + p.translateY
            p = p.parent
        }
        return x to y
    }

    // 点击测试（使用场景坐标）
    // 假设传入的 x,y 是全局/场景坐标
    open fun hitTest(x: Double, y: Double): Boolean {
        val (absX, absY) = getLocationInWindow()
        return x >= absX && x <= absX + width && y >= absY && y <= absY + height
    }

    // 生命周期方法
    open fun onAttach() {}
    open fun onDetach() {
        // 当View分离时，取消所有运行中的动画
        cancelAllAnimations()
        // 取消长按定时器
        cancelLongPressTimer()
    }

    // 事件回调方法
    open fun onVisibilityChanged(changedView: View, visibility: Int) {}
    open fun onEnabledChanged(enabled: Boolean) {}
    open fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Any?) {
        // 通知焦点变化监听器
        onFocusChangeListener?.onFocusChange(this, gainFocus)
    }

    // 动画相关方法 (保持原有逻辑)

    /**
     * 启动动画
     */
    open fun startAnimation(animation: Animation, listener: AnimationListener? = null) {
        // 设置动画监听器
        listener?.let { animationListener ->
            animation.setOnFinished {
                animationListener.onAnimationEnd(animation)
                removeAnimation(animation)
            }
        } ?: run {
            // 默认监听器，用于清理动画
            animation.setOnFinished { removeAnimation(animation) }
        }

        // 添加到运行中的动画列表
        runningAnimations.add(animation)
        // 启动动画
        animation.play()
    }

    /**
     * 取消动画
     */
    open fun cancelAnimation(animation: Animation) {
        animation.stop()
        removeAnimation(animation)
    }

    /**
     * 取消所有动画
     */
    open fun cancelAllAnimations() {
        val animationsToCancel = ArrayList(runningAnimations)
        for (animation in animationsToCancel) {
            cancelAnimation(animation)
        }
    }

    /**
     * 从运行中的动画列表中移除动画
     */
    private fun removeAnimation(animation: Animation) {
        runningAnimations.remove(animation)
    }

    open fun fadeIn(duration: Duration = Duration.millis(300.0), listener: AnimationListener? = null): Timeline {
        val animation = ViewAnimation.fadeIn(this, duration)
        startAnimation(animation, listener)
        return animation
    }

    open fun fadeOut(duration: Duration = Duration.millis(300.0), listener: AnimationListener? = null): Timeline {
        val animation = ViewAnimation.fadeOut(this, duration)
        startAnimation(animation, listener)
        return animation
    }

    open fun translate(
        fromX: Double,
        fromY: Double,
        toX: Double,
        toY: Double,
        duration: Duration = Duration.millis(300.0),
        listener: AnimationListener? = null
    ): Timeline {
        val animation = ViewAnimation.translate(this, fromX, fromY, toX, toY, duration)
        startAnimation(animation, listener)
        return animation
    }

    open fun scale(
        fromX: Double,
        fromY: Double,
        toX: Double,
        toY: Double,
        duration: Duration = Duration.millis(300.0),
        listener: AnimationListener? = null
    ): Timeline {
        val animation = ViewAnimation.scale(this, fromX, fromY, toX, toY, duration)
        startAnimation(animation, listener)
        return animation
    }

    open fun rotate(
        fromAngle: Double,
        toAngle: Double,
        duration: Duration = Duration.millis(300.0),
        listener: AnimationListener? = null
    ): Timeline {
        val animation = ViewAnimation.rotate(this, fromAngle, toAngle, duration)
        startAnimation(animation, listener)
        return animation
    }

    open fun parallel(vararg animations: Animation, listener: AnimationListener? = null): ParallelTransition {
        val animation = ViewAnimation.parallel(*animations)
        startAnimation(animation, listener)
        return animation
    }

    open fun sequential(vararg animations: Animation, listener: AnimationListener? = null): SequentialTransition {
        val animation = ViewAnimation.sequential(*animations)
        startAnimation(animation, listener)
        return animation
    }

    open fun isAnimating(): Boolean {
        return runningAnimations.isNotEmpty()
    }

    // 静态常量和内部类
    companion object {
        const val VISIBLE = 0
        const val INVISIBLE = 1
        const val GONE = 2

        const val FOCUS_UP = 0
        const val FOCUS_DOWN = 1
        const val FOCUS_LEFT = 2
        const val FOCUS_RIGHT = 3
        const val FOCUS_FORWARD = 4
        const val FOCUS_BACKWARD = 5
    }

    fun interface OnClickListener {
        fun onClick(v: View)
    }

    fun interface OnLongClickListener {
        fun onLongClick(v: View): Boolean
    }

    fun interface OnTouchListener {
        fun onTouch(v: View, event: MouseEvent): Boolean
    }

    fun interface OnFocusChangeListener {
        fun onFocusChange(v: View, hasFocus: Boolean)
    }
}