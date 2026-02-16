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

package dev.answer.material.manager

import dev.answer.material.content.Context
import dev.answer.material.desgin.hct.Hct
import dev.answer.material.theme.ColorPalette


/**
 *
 * @author AnswerDev
 * @date 2026/2/17 01:40
 * @description ColorManager
 */
class ColorManager(
    initialHct: Hct,
    initialDarkMode: Boolean
) {

    private var currentHct: Hct = initialHct
    private var currentDarkMode: Boolean = initialDarkMode

    var palette: ColorPalette = createPalette(initialHct, initialDarkMode)
        private set

    fun updateIfNeeded(hct: Hct, darkMode: Boolean) {
        if (hct != currentHct || darkMode != currentDarkMode) {
            currentHct = hct
            currentDarkMode = darkMode
            palette = createPalette(hct, darkMode)
        }
    }

    private fun createPalette(hct: Hct, darkMode: Boolean): ColorPalette {
        return ColorPalette(hct, darkMode)
    }
}