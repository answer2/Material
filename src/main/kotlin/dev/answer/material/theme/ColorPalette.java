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

package dev.answer.material.theme;

/**
 * @author AnswerDev
 * @date 2026/2/15 20:56
 * @description ColorPalette
 */

import dev.answer.material.desgin.Material3;
import dev.answer.material.desgin.dynamiccolor.DynamicScheme;
import dev.answer.material.desgin.dynamiccolor.MaterialDynamicColors;
import dev.answer.material.desgin.hct.Hct;
import dev.answer.material.desgin.utils.ColorUtils;
import javafx.scene.paint.Color;

public class ColorPalette {

    private final DynamicScheme scheme;
    private final MaterialDynamicColors colors;
    private boolean dark;
    private Hct hct;

    public ColorPalette(Hct hct, boolean dark, float contrast) {
        this.hct = hct;
        this.dark = dark;
        scheme = Material3.getDynamicScheme(hct, dark, contrast);
        colors = new MaterialDynamicColors();
    }

    public ColorPalette(Hct hct, boolean dark) {
        this(hct, dark, 0);
    }

    public Color getPrimary() {
        return argbToColor(colors.primary().getArgb(scheme));
    }

    public Color getSecondary() {
        return argbToColor(colors.secondary().getArgb(scheme));
    }

    public Color getTertiary() {
        return argbToColor(colors.tertiary().getArgb(scheme));
    }

    public Color getPrimaryContainer() {
        return argbToColor(colors.primaryContainer().getArgb(scheme));
    }

    public Color getSecondaryContainer() {
        return argbToColor(colors.secondaryContainer().getArgb(scheme));
    }

    public Color getTertiaryContainer() {
        return argbToColor(colors.tertiaryContainer().getArgb(scheme));
    }

    public Color getOnPrimary() {
        return argbToColor(colors.onPrimary().getArgb(scheme));
    }

    public Color getOnSecondary() {
        return argbToColor(colors.onSecondary().getArgb(scheme));
    }

    public Color getOnTertiary() {
        return argbToColor(colors.onTertiary().getArgb(scheme));
    }

    public Color getOnPrimaryContainer() {
        return argbToColor(colors.onPrimaryContainer().getArgb(scheme));
    }

    public Color getOnSecondaryContainer() {
        return argbToColor(colors.onSecondaryContainer().getArgb(scheme));
    }

    public Color getOnTertiaryContainer() {
        return argbToColor(colors.onTertiaryContainer().getArgb(scheme));
    }

    public Color getError() {
        return argbToColor(colors.error().getArgb(scheme));
    }

    public Color getErrorContainer() {
        return argbToColor(colors.errorContainer().getArgb(scheme));
    }

    public Color getOnError() {
        return argbToColor(colors.onError().getArgb(scheme));
    }

    public Color getOnErrorContainer() {
        return argbToColor(colors.onErrorContainer().getArgb(scheme));
    }

    public Color getBackground() {
        return argbToColor(colors.background().getArgb(scheme));
    }

    public Color getOnBackground() {
        return argbToColor(colors.onBackground().getArgb(scheme));
    }

    public Color getSurface() {
        return argbToColor(colors.surface().getArgb(scheme));
    }

    public Color getOnSurface() {
        return argbToColor(colors.onSurface().getArgb(scheme));
    }

    public Color getSurfaceContainer() {
        return argbToColor(colors.surfaceContainer().getArgb(scheme));
    }

    public Color getSurfaceVariant() {
        return argbToColor(colors.surfaceVariant().getArgb(scheme));
    }

    public Color getOnSurfaceVariant() {
        return argbToColor(colors.onSurfaceVariant().getArgb(scheme));
    }

    public Color getSurfaceContainerHigh() {
        return argbToColor(colors.surfaceContainerHigh().getArgb(scheme));
    }

    public Color getSurfaceContainerHighest() {
        return argbToColor(colors.surfaceContainerHighest().getArgb(scheme));
    }

    public Color getSurfaceContainerLow() {
        return argbToColor(colors.surfaceContainerLow().getArgb(scheme));
    }

    public Color getSurfaceContainerLowest() {
        return argbToColor(colors.surfaceContainerLowest().getArgb(scheme));
    }

    public Color getSurfaceTint() {
        return argbToColor(colors.surfaceTint().getArgb(scheme));
    }

    public Color getOutline() {
        return argbToColor(colors.outline().getArgb(scheme));
    }

    public Color getOutlineVariant() {
        return argbToColor(colors.outlineVariant().getArgb(scheme));
    }

    private Color argbToColor(int argb) {

        int red = ColorUtils.redFromArgb(argb);
        int green = ColorUtils.greenFromArgb(argb);
        int blue = ColorUtils.blueFromArgb(argb);

        return Color.color(red, green, blue);
    }

    public boolean isDarkMode() {
        return dark;
    }

    public Hct getHct() {
        return hct;
    }
}