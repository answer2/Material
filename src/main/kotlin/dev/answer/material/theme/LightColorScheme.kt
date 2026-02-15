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

import dev.answer.material.desgin.utils.ColorUtils
import javafx.scene.paint.Color

/**
 *
 * @author AnswerDev
 * @date 2026/2/15 02:35
 * @description LightColorScheme
 */
object LightColorScheme {

    fun create() = ColorScheme(
        primary = argbToColor(0x6750A4),
        onPrimary = Color.WHITE,
        primaryContainer = argbToColor(0xEADDFF),
        onPrimaryContainer = argbToColor(0x21005D),

        secondary = argbToColor(0x625B71),
        secondaryContainer = argbToColor(0xE8DEF8),
        onSecondaryContainer = argbToColor(0x1D192B),

        surface = argbToColor(0xFFFBFE),
        surfaceContainerLow = argbToColor(0xF7F2FA),
        surfaceContainerHigh = argbToColor(0xECE6F0),

        outline = argbToColor(0x79747E),
        error = argbToColor(0xB3261E)
    )

    private fun argbToColor(argb: Int): Color {
        val red = ColorUtils.redFromArgb(argb)
        val green = ColorUtils.greenFromArgb(argb)
        val blue = ColorUtils.blueFromArgb(argb)

        return Color.color(red.toDouble(), green.toDouble(), blue.toDouble())
    }

}
