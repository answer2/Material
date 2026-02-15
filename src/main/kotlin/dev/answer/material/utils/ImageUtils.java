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

package dev.answer.material.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * @author AnswerDev
 * @date 2026/2/15 20:34
 * @description ImageUtils
 */

public class ImageUtils {

    public static Color calculateAverageColor(Image image) {
        double totalRed = 0;
        double totalGreen = 0;
        double totalBlue = 0;
        double totalAlpha = 0;

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int totalPixels = width * height;

        PixelReader pixelReader = image.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixelColor = pixelReader.getColor(x, y);
                totalRed += pixelColor.getRed();
                totalGreen += pixelColor.getGreen();
                totalBlue += pixelColor.getBlue();
                totalAlpha += pixelColor.getOpacity();
            }
        }

        double averageRed = totalRed / totalPixels;
        double averageGreen = totalGreen / totalPixels;
        double averageBlue = totalBlue / totalPixels;
        double averageAlpha = totalAlpha / totalPixels;

        return new Color(averageRed, averageGreen, averageBlue, averageAlpha);
    }

    public static int[] imageToPixels(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int[] pixels = new int[width * height];

        PixelReader pixelReader = image.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);

                // Convert JavaFX Color to ARGB integer format
                int a = (int) Math.round(color.getOpacity() * 255);
                int r = (int) Math.round(color.getRed() * 255);
                int g = (int) Math.round(color.getGreen() * 255);
                int b = (int) Math.round(color.getBlue() * 255);

                // Pack into ARGB format (same as BufferedImage.getRGB())
                int argb = (a << 24) | (r << 16) | (g << 8) | b;
                pixels[y * width + x] = argb;
            }
        }

        return pixels;
    }

    // Alternative method that uses a more efficient approach for pixel extraction
    public static int[] imageToPixelsFast(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        // Create a WritableImage to access the pixel buffer directly
        WritableImage writableImage = new WritableImage(width, height);
        writableImage.getPixelWriter().setPixels(0, 0, width, height,
                image.getPixelReader(), 0, 0);

        // Use PixelReader to get all pixels at once
        PixelReader pixelReader = writableImage.getPixelReader();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = pixelReader.getArgb(x, y);
            }
        }

        return pixels;
    }

    // Utility method to convert ARGB int array back to JavaFX Image
    public static Image pixelsToImage(int[] pixels, int width, int height) {
        WritableImage image = new WritableImage(width, height);
        image.getPixelWriter().setPixels(0, 0, width, height,
                javafx.scene.image.PixelFormat.getIntArgbInstance(),
                pixels, 0, width);
        return image;
    }
}