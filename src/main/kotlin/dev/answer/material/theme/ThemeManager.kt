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

package dev.answer.material.theme

import dev.answer.material.desgin.hct.Hct

/**
 *
 * @author AnswerDev
 * @date 2026/2/17 02:15
 * @description ThemeManager
 */
class ThemeManager(initialState: ThemeState) {

    private var state = initialState

    private val observers = mutableSetOf<ThemeObserver>()

    var theme: Theme = createTheme(state)
        private set

    fun addObserver(observer: ThemeObserver) {
        observers.add(observer)
        observer.onThemeChanged(theme)
    }

    fun removeObserver(observer: ThemeObserver) {
        observers.remove(observer)
    }

    fun update(newState: ThemeState) {
        if (newState == state) return
        state = newState
        theme = createTheme(state)
        notifyObservers()
    }

    fun toggleDarkMode() {
        update(state.copy(darkMode = !state.darkMode))
    }

    fun updatePrimary(hct: Hct) {
        update(state.copy(hct = hct))
    }

    private fun notifyObservers() {
        observers.forEach { it.onThemeChanged(theme) }
    }

    private fun createTheme(state: ThemeState): Theme {
        val palette = ColorPalette(state.hct, state.darkMode)
        return Theme(
            colorScheme = palette,
            shapes = Shapes.default(),
            typography = Typography.default()
        )
    }
}
