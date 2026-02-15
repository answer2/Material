package dev.answer.material.desgin;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import dev.answer.material.desgin.dynamiccolor.DynamicScheme;
import dev.answer.material.desgin.hct.Hct;
import dev.answer.material.desgin.quantize.QuantizerCelebi;
import dev.answer.material.desgin.scheme.SchemeTonalSpot;
import dev.answer.material.desgin.score.Score;
import dev.answer.material.utils.ImageUtils;

public class Material3 {

	public static DynamicScheme getDynamicScheme(Hct hct, boolean dark, double contrast) {
		return new SchemeTonalSpot(hct, dark, contrast);
	}

	public static Hct getImageHct(Image image) {
		if (image == null) {
			return Hct.from(0, 0, 0);
		}

		// Convert JavaFX Image to pixel array
		int[] pixels = imageToPixels(image);

		Map<Integer, Integer> quantizerResult = QuantizerCelebi.quantize(pixels, 128);
		List<Integer> colors = Score.score(quantizerResult);

		return Hct.fromInt(colors.get(0));
	}

	public static Hct getImageHct(File imageFile) {
		try (FileInputStream fis = new FileInputStream(imageFile)) {
			Image image = new Image(fis);
			return getImageHct(image);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Hct.from(0, 0, 0);
	}

	// Helper method to convert JavaFX Image to pixel array
	private static int[] imageToPixels(Image image) {
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		int[] pixels = new int[width * height];

		PixelReader pixelReader = image.getPixelReader();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pixels[y * width + x] = pixelReader.getArgb(x, y);
			}
		}

		return pixels;
	}

	// Alternative method that uses the ImageUtils class you provided earlier
	public static Hct getImageHctUsingUtils(Image image) {
		if (image == null) {
			return Hct.from(0, 0, 0);
		}

		int[] pixels = dev.answer.material.utils.ImageUtils.imageToPixels(image);

		Map<Integer, Integer> quantizerResult = QuantizerCelebi.quantize(pixels, 128);
		List<Integer> colors = Score.score(quantizerResult);

		return Hct.fromInt(colors.get(0));
	}

	// Utility method to load Image from file with error handling
	public static Image loadImageSafely(File imageFile) {
		try (FileInputStream fis = new FileInputStream(imageFile)) {
			return new Image(fis);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	// Overloaded method for loading from path
	public static Image loadImageSafely(String imagePath) {
		return loadImageSafely(new File(imagePath));
	}
}