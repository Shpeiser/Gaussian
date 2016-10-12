package com.artbrains.test.rozhak.blurer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

public class GaussianBlur {

	public static void processImage(File file, int kernelsize) {

		BufferedImage img = blur(3, kernelsize, readImage(file));
		writeImage(file, img);

	}

	private static BufferedImage readImage(File file) {

		try {
			return ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static BufferedImage blur(double sigma, int kernelsize, BufferedImage img) {
		double[] kernel = createKernel(sigma, kernelsize);
		int numberOfColumns = 4;
		int imageHeight = img.getHeight();
		int imageWidth = img.getWidth();
		int blockImageHeight = imageHeight / numberOfColumns;
		int blockImageWigth = imageWidth / numberOfColumns;
		BufferedImage imageClone = deepCopy(img);

		ExecutorService es = Executors.newFixedThreadPool(numberOfColumns * numberOfColumns );

		for (int i = 0; i < numberOfColumns; i++) {
			for (int j = 0; j < numberOfColumns; j++) {
				int startX = blockImageWigth * j;
				int startY = blockImageHeight * i;
				int endX = blockImageWigth * (j + 1) < imageWidth ? blockImageWigth * (j + 1) : imageWidth;
				int endY = blockImageHeight * (i + 1) < imageHeight ? blockImageHeight * (i + 1) : imageHeight;
				es.submit(new Runnable() {

					@Override
					public void run() {

						processPicture(imageClone, img, kernelsize, kernel, startX, startY, endX, endY);

					}
				});

			}
		}
		es.shutdown();
		try {
			es.awaitTermination(Byte.MAX_VALUE, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return imageClone;
	}

	private static BufferedImage processPicture(BufferedImage img, BufferedImage imageClone, int kernelsize,
			double[] kernel, int startX, int startY, int endX, int endY) {

		int imgHeight = img.getWidth();
		int imgWidth = img.getHeight();
		for (int i = startX; i < endX; i++) {
			for (int j = startY; j < endY; j++) {
				double overflow = 0;
				int counter = 0;
				int kernelhalf = (kernelsize - 1) / 2;
				double red = 0;
				double green = 0;
				double blue = 0;
				for (int k = i - kernelhalf; k < i + kernelhalf; k++) {
					for (int l = j - kernelhalf; l < j + kernelhalf; l++) {
						if (k < 0 || k >= imgHeight || l < 0 || l >= imgWidth) {
							counter++;
							overflow += kernel[counter];
							continue;
						}

						Color c = new Color(imageClone.getRGB(k, l));
						red += c.getRed() * kernel[counter];
						green += c.getGreen() * kernel[counter];
						blue += c.getBlue() * kernel[counter];
						counter++;
					}
					counter++;
				}

				if (overflow > 0) {
					red = 0;
					green = 0;
					blue = 0;
					counter = 0;
					for (int k = i - kernelhalf; k < i + kernelhalf; k++) {
						for (int l = j - kernelhalf; l < j + kernelhalf; l++) {
							if (k < 0 || k >= imgHeight || l < 0 || l >= imgWidth) {
								counter++;
								continue;
							}

							Color c = new Color(imageClone.getRGB(k, l));
							red += c.getRed() * kernel[counter] * (1 / (1 - overflow));
							green += c.getGreen() * kernel[counter] * (1 / (1 - overflow));
							blue += c.getBlue() * kernel[counter] * (1 / (1 - overflow));
							counter++;
						}
						counter++;
					}
				}

				img.setRGB(i, j, new Color((int) red, (int) green, (int) blue).getRGB());
			}
		}

		return img;

	}

	private static double[] createKernel(double sigma, int kernelsize) {
		double[] kernel = new double[kernelsize * kernelsize];
		for (int i = 0; i < kernelsize; i++) {
			double x = i - (kernelsize - 1) / 2;
			for (int j = 0; j < kernelsize; j++) {
				double y = j - (kernelsize - 1) / 2;
				kernel[j + i * kernelsize] = 1 / (2 * Math.PI * sigma * sigma)
						* Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
			}
		}
		float sum = 0;
		for (int i = 0; i < kernelsize; i++) {
			for (int j = 0; j < kernelsize; j++) {
				sum += kernel[j + i * kernelsize];
			}
		}
		for (int i = 0; i < kernelsize; i++) {
			for (int j = 0; j < kernelsize; j++) {
				kernel[j + i * kernelsize] /= sum;
			}
		}
		return kernel;
	}

	private static void writeImage(File output, BufferedImage img) {
		try {
			ImageIO.write(img, "png", output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
}
