package dev.answer.material.view.measure

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 20:41
 * @description MeasureSpec
 */
object MeasureSpec {

    const val UNSPECIFIED = 0
    const val EXACTLY = 1
    const val AT_MOST = 2

    fun makeMeasureSpec(size: Double, mode: Int): Long {
        return (size.toLong() shl 2) or mode.toLong()
    }

    fun getMode(spec: Long): Int = (spec and 0x3).toInt()
    fun getSize(spec: Long): Double = (spec shr 2).toDouble()
}