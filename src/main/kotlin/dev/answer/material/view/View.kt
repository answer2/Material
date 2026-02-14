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
import dev.answer.material.graphics.Drawable
import dev.answer.material.view.animation.AnimationListener
import dev.answer.material.view.animation.ViewAnimation
import dev.answer.material.view.measure.MeasureSpec
import javafx.animation.Animation
import javafx.animation.FadeTransition
import javafx.animation.ParallelTransition
import javafx.animation.RotateTransition
import javafx.animation.ScaleTransition
import javafx.animation.SequentialTransition
import javafx.animation.TranslateTransition
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseEvent
import javafx.util.Duration
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 00:48
 * @description View
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

    // 标签
    var tag: Any? = null

    // 视图树
    var parent: View? = null
    protected val children: MutableList<View> = mutableListOf()

    // 事件监听器
    var onClickListener: OnClickListener? = null
    var onLongClickListener: OnLongClickListener? = null
    var onTouchListener: OnTouchListener? = null
    var onFocusChangeListener: OnFocusChangeListener? = null

    // 长按事件检测相关
    private var longPressTimeout = 500L // 长按超时时间（毫秒）
    private var isLongPressDetected = false
    private var longPressTimer: ScheduledThreadPoolExecutor? = null

    // 触摸事件状态
    private var isPressed = false
    private var isHovered = false

    // 动画相关
    private val runningAnimations: MutableList<Animation> = mutableListOf()

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

    val width: Double
        get() = right - left

    val height: Double
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

        // 绘制背景
        background?.draw(
            gc,
            left,
            top,
            width,
            height
        )

        // 保存画布状态
        gc.save()
        gc.translate(
            left + paddingLeft - scrollX,
            top + paddingTop - scrollY
        )

        // 绘制内容
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
        for (child in children.reversed()) {
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
        // 先调用触摸监听器
        if (onTouchListener != null && onTouchListener!!.onTouch(this, event)) {
            return true
        }

        if (clickable || onLongClickListener != null) {
            when (event.eventType) {
                MouseEvent.MOUSE_PRESSED -> {
                    isPressed = true
                    isLongPressDetected = false
                    // 启动长按定时器
                    startLongPressTimer()
                    return true
                }

                MouseEvent.MOUSE_RELEASED -> {
                    isPressed = false
                    // 取消长按定时器
                    cancelLongPressTimer()
                    // 如果没有检测到长按，则触发点击事件
                    if (!isLongPressDetected) {
                        performClick()
                    }
                    return true
                }

                MouseEvent.MOUSE_CLICKED -> {
                    // 已经在MOUSE_RELEASED中处理了点击事件
                    return true
                }

                MouseEvent.MOUSE_ENTERED -> {
                    isHovered = true
                    return true
                }

                MouseEvent.MOUSE_EXITED -> {
                    isHovered = false
                    isPressed = false
                    // 取消长按定时器
                    cancelLongPressTimer()
                    return true
                }

                MouseEvent.MOUSE_DRAGGED -> {
                    // 拖动时取消长按
                    cancelLongPressTimer()
                    return true
                }

                else -> {}
            }
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
        cancelLongPressTimer() // 先取消之前的定时器
        longPressTimer = ScheduledThreadPoolExecutor(1)
        longPressTimer?.schedule({
            isLongPressDetected = true
            performLongClick()
        }, longPressTimeout, TimeUnit.MILLISECONDS)
    }

    // 取消长按定时器
    private fun cancelLongPressTimer() {
        longPressTimer?.shutdownNow()
        longPressTimer = null
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

    // 点击测试
    open fun hitTest(x: Double, y: Double): Boolean {
        return x >= left && x <= right && y >= top && y <= bottom
    }

    // 生命周期方法
    open fun onAttach() {}
    open fun onDetach() {
        // 当View分离时，取消所有运行中的动画
        cancelAllAnimations()
    }

    // 事件回调方法
    open fun onVisibilityChanged(changedView: View, visibility: Int) {}
    open fun onEnabledChanged(enabled: Boolean) {}
    open fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Any?) {
        // 通知焦点变化监听器
        onFocusChangeListener?.onFocusChange(this, gainFocus)
    }

    // 动画相关方法

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

    /**
     * 淡入动画
     */
    open fun fadeIn(duration: Duration = Duration.millis(300.0), listener: AnimationListener? = null): FadeTransition {
        val animation = ViewAnimation.fadeIn(this, duration)
        startAnimation(animation, listener)
        return animation
    }

    /**
     * 淡出动画
     */
    open fun fadeOut(duration: Duration = Duration.millis(300.0), listener: AnimationListener? = null): FadeTransition {
        val animation = ViewAnimation.fadeOut(this, duration)
        startAnimation(animation, listener)
        return animation
    }

    /**
     * 平移动画
     */
    open fun translate(
        fromX: Double,
        fromY: Double,
        toX: Double,
        toY: Double,
        duration: Duration = Duration.millis(300.0),
        listener: AnimationListener? = null
    ): TranslateTransition {
        val animation = ViewAnimation.translate(this, fromX, fromY, toX, toY, duration)
        startAnimation(animation, listener)
        return animation
    }

    /**
     * 缩放动画
     */
    open fun scale(
        fromX: Double,
        fromY: Double,
        toX: Double,
        toY: Double,
        duration: Duration = Duration.millis(300.0),
        listener: AnimationListener? = null
    ): ScaleTransition {
        val animation = ViewAnimation.scale(this, fromX, fromY, toX, toY, duration)
        startAnimation(animation, listener)
        return animation
    }

    /**
     * 旋转动画
     */
    open fun rotate(
        fromAngle: Double,
        toAngle: Double,
        duration: Duration = Duration.millis(300.0),
        listener: AnimationListener? = null
    ): RotateTransition {
        val animation = ViewAnimation.rotate(this, fromAngle, toAngle, duration)
        startAnimation(animation, listener)
        return animation
    }

    /**
     * 并行执行多个动画
     */
    open fun parallel(vararg animations: Animation, listener: AnimationListener? = null): ParallelTransition {
        val animation = ViewAnimation.parallel(*animations)
        startAnimation(animation, listener)
        return animation
    }

    /**
     * 顺序执行多个动画
     */
    open fun sequential(vararg animations: Animation, listener: AnimationListener? = null): SequentialTransition {
        val animation = ViewAnimation.sequential(*animations)
        startAnimation(animation, listener)
        return animation
    }

    /**
     * 检查是否有动画正在运行
     */
    open fun isAnimating(): Boolean {
        return runningAnimations.isNotEmpty()
    }

    // 静态常量和内部类
    companion object {
        // 可见性常量
        const val VISIBLE = 0
        const val INVISIBLE = 1
        const val GONE = 2

        // 焦点方向常量
        const val FOCUS_UP = 0
        const val FOCUS_DOWN = 1
        const val FOCUS_LEFT = 2
        const val FOCUS_RIGHT = 3
        const val FOCUS_FORWARD = 4
        const val FOCUS_BACKWARD = 5
    }

    // 点击监听器接口
    interface OnClickListener {
        fun onClick(v: View)
    }

    // 长按监听器接口
    interface OnLongClickListener {
        fun onLongClick(v: View): Boolean
    }

    // 触摸监听器接口
    interface OnTouchListener {
        fun onTouch(v: View, event: MouseEvent): Boolean
    }

    // 焦点变化监听器接口
    interface OnFocusChangeListener {
        fun onFocusChange(v: View, hasFocus: Boolean)
    }
}