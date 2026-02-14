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

import java.io.InputStream
import java.net.URL

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 21:26
 * @description Resources
 */
open class Resources( val clazz: Class<*>) {

    fun loadURL(path: String): URL {
        return requireNotNull(clazz.getResource(path)) {
            "Resource not found: $path"
        }
    }

    fun load(path: String): String =
        loadURL(path).toString()

    fun loadStream(path: String): InputStream {
        return requireNotNull(clazz.getResourceAsStream(path)) {
            "Resource not found: $path"
        }
    }
}
