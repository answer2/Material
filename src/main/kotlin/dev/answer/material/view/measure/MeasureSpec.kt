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

    private const val MODE_SHIFT = 30
    private const val MODE_MASK = 0x3 shl MODE_SHIFT

    /**
     * 创建测量规格
     * @param size 大小
     * @param mode 模式
     * @return 测量规格
     */
    fun makeMeasureSpec(size: Int, mode: Int): Int {
        return size or (mode shl MODE_SHIFT)
    }

    /**
     * 从测量规格中获取模式
     * @param spec 测量规格
     * @return 模式
     */
    fun getMode(spec: Int): Int {
        return spec and MODE_MASK shr MODE_SHIFT
    }

    /**
     * 从测量规格中获取大小
     * @param spec 测量规格
     * @return 大小
     */
    fun getSize(spec: Int): Int {
        return spec and MODE_MASK.inv()
    }
}