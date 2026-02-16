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

package dev.answer.material.content

import dev.answer.material.desgin.hct.Hct
import dev.answer.material.manager.ActivityManager
import dev.answer.material.theme.ColorPalette
import dev.answer.material.theme.Shapes
import dev.answer.material.theme.Theme
import dev.answer.material.theme.ThemeManager
import dev.answer.material.theme.ThemeState
import dev.answer.material.theme.Typography

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 00:30
 * @description Context
 */
abstract class Context {

    abstract val resources: Resources

    val themeManager: ThemeManager by lazy {
        ThemeManager(
            ThemeState(
                hct = Hct.fromInt(0x6750A4),
                darkMode = false
            )
        )
    }

    val theme: Theme
        get() = themeManager.theme

    val colorScheme: ColorPalette
        get() = theme.colorScheme

    val typography: Typography
        get() = theme.typography

    val shapes: Shapes
        get() = theme.shapes

    fun <T : Activity> startActivity(activityClass: Class<T>) {
        ActivityManager.startActivity(this, activityClass)
    }

    open fun finish() {
        ActivityManager.finishActivity()
    }
}
