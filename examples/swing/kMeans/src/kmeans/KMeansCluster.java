/*******************************************************************************
 * Copyright (c) 2023 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package kmeans;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class implements the k-means clustering algorithm in order to reduce the
 * color palette of a given image.
 */
public class KMeansCluster {
	/**
	 * Performs the k-means algorithm and creates a reduced variant of the source
	 * image.
	 * 
	 * @param source         The source image to be reduced.
	 * @param numberOfColors The number of palette colors. A higher number usually
	 *                       results in a better image quality.
	 * @param iterations     The number of times the k-means algorithm is applied. A
	 *                       higher number usually results in a more accurate
	 *                       palette and thus better image quality.
	 * @return A color-reduced container containing both the created image and its
	 *         palette.
	 */
	public static ColorReducedImage createColorReducedImage(BufferedImage source, int numberOfColors, int iterations) {
		List<Color> palette = getInitialMeans(source, numberOfColors);
		//
		for (int i = 0; i < iterations; ++i) {
			palette = updatePalette(source, palette);
		}
		//
		BufferedImage target = createColorReducedImage(source, palette);
		return new ColorReducedImage(target, palette);
	}

	/**
	 * Creates a compressed variant of the given image where all pixel colors are
	 * replaced by their closest palette color. This method doesn't modify the
	 * source image but instead creates a new image with the same width and height.
	 * 
	 * @param source  The original source image.
	 * @param palette All colors that may be used in the reduced image.
	 * @return A new, reduced image using the source image and palette as reference.
	 */
	private static BufferedImage createColorReducedImage(BufferedImage source, List<Color> palette) {
		BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
		//
		modifyPixel(source, (x, y) -> {
			Color pixelColor = new Color(source.getRGB(x, y));
			Map.Entry<Double, Color> closestColor = getClosestColor(pixelColor, palette);
			target.setRGB(x, y, closestColor.getValue().getRGB());
			return pixelColor;
		});
		//
		return target;
	}

	/**
	 * Performs a single iteration of the k-means clustering algorithm. This method
	 * first groups all pixel colors of the given image by the palette color they
	 * are the closest to. Using this mapping, we are then able to calculate new
	 * palette colors, using the average of all colors associated with it. Note that
	 * if a palette color isn't close to any pixel color, its original value is
	 * reused, in order to ensure that the palette remains the same size.
	 * 
	 * @param source  The original source image.
	 * @param palette The old palette based on which the new palette is calculated.
	 * @return An <b>unmodifiable</b> list of new palette colors.
	 */
	private static List<Color> updatePalette(BufferedImage source, List<Color> palette) {
		Map<Color, Color> globalNewPalette = new ConcurrentHashMap<>();
		//
		Map<Color, List<Color>> colorsByCluster = groupByCluster(source, palette);
		colorsByCluster.entrySet() //
				.parallelStream() //
				.forEach((entry) -> globalNewPalette.put(entry.getKey(), getAverage(entry.getValue())));
		//
		List<Color> newPalette = new ArrayList<>(palette.size());
		for (int i = 0; i < palette.size(); ++i) {
			// There might be some colors in the palette which aren't close to any of the
			// colors in the source image. In such a case, the new palette would have less
			// colors than desired. To avoid this problem, we simply pick the color from the
			// old palette again.
			newPalette.add(globalNewPalette.getOrDefault(palette.get(i), palette.get(i)));
		}
		newPalette.sort(Comparator.comparing(Color::getRGB));
		return Collections.unmodifiableList(newPalette);
	}

	/**
	 * Calculates and returns the average of all given colors. The average is
	 * determined by individually calculating the average of all given {@code red},
	 * {@code green} and {@code blue} values.
	 * 
	 * @param colors A list of arbitrary colors.
	 * @return The average color as described above.
	 */
	private static Color getAverage(List<Color> colors) {
		BigInteger r = BigInteger.ZERO;
		BigInteger g = BigInteger.ZERO;
		BigInteger b = BigInteger.ZERO;
		//
		for (Color color : colors) {
			r = r.add(BigInteger.valueOf(color.getRed()));
			g = g.add(BigInteger.valueOf(color.getGreen()));
			b = b.add(BigInteger.valueOf(color.getBlue()));
		}
		//
		return new Color(r.intValue() / colors.size(), g.intValue() / colors.size(), b.intValue() / colors.size());
	}

	/**
	 * For each color in the given image, calculates and returns the closest palette
	 * color, i.e. the color with the smallest Euclidean distance.
	 * 
	 * @param source  The image for which the palette colors are calculated.
	 * @param palette All palette colors which are used for the calculation.
	 * @return An unmodifiable list containing the closest palette colors for each
	 *         color that is used in the given image.
	 * @see #getClosestColor(Color, List)
	 */
	private static List<PaletteColorAssociation> getClosestColors(BufferedImage source, List<Color> palette) {
		return modifyPixel(source, (x, y) -> {
			Color pixelColor = new Color(source.getRGB(x, y));
			Map.Entry<Double, Color> closestColor = getClosestColor(pixelColor, palette);
			return new PaletteColorAssociation(pixelColor, closestColor.getValue(), closestColor.getKey());
		});
	}

	/**
	 * Calculates and returns the closest palette color to the given color, i.e. the
	 * color with the smallest Euclidean distance.
	 * 
	 * @param c       An arbitrary color.
	 * @param palette All colors the given color is compared to.
	 * @return The color in {@code palette} that is the closest to {@code c}.
	 */
	private static Map.Entry<Double, Color> getClosestColor(Color c, List<Color> palette) {
		// Calculate the Euclidean distance of each color. If multiple colors have the
		// same distance, the last entry is used.
		TreeMap<Double, Color> distances = new TreeMap<>();
		for (Color color : palette) {
			distances.put(getEuclideanDistance(color, c), color);
		}
		// Find and return the color with the smallest Euclidean distance
		return distances.firstEntry();
	}

	/**
	 * Calculates and returns the distance between the given colors using
	 * {@code sqrt(r²+g²+b²)} (their Euclidean distance}.
	 * 
	 * @param c1 First color to compare.
	 * @param c2 Second color to compare.
	 * @return The Euclidean distance between {@code c1} and {@code c2}.
	 */
	private static double getEuclideanDistance(Color c1, Color c2) {
		int r = c1.getRed() - c2.getRed();
		int g = c1.getGreen() - c2.getGreen();
		int b = c1.getBlue() - c2.getBlue();
		return Math.sqrt(r * r + g * g + b * b);
	}

	/**
	 * Calculates an array of {@code length} distinct colors. These colors are used
	 * as the initial means for the color quantization. Only the {@code red},
	 * {@color green} and {@code blue} value of a color is considered, but not its
	 * {@code alpha} value. The initial means are calculated using the k-means++
	 * algorithm:
	 * <ol>
	 * <li>Choose one center uniformly at random among the data points.</li>
	 * <li>For each data point x not chosen yet, compute D(x), the distance between
	 * x and the nearest center that has already been chosen.</li>
	 * <li>Choose one new data point at random as a new center, using a weighted
	 * probability distribution where a point x is chosen with probability
	 * proportional to D(x)².</li>
	 * <li>Repeat Steps 2 and 3 until k centers have been chosen.</li>
	 * <li>Now that the initial centers have been chosen, proceed using standard
	 * k-means clustering.</li>
	 * </ol>
	 * 
	 * @param source The image on which the k-means algorithm is applied on.
	 * @param length The total number of colors to generated. Used as the initial
	 *               argument for {@link #updatePalette(BufferedImage, Color[])}.
	 * @return An unmodifiable list of {@code length} distinct colors.
	 */
	private static List<Color> getInitialMeans(BufferedImage source, int length) {
		Random rng = new Random();
		// Choose one center uniformly at random among the data points.
		// (i.e. random RGB values)
		Color initialColor = new Color(rng.nextInt(0x01000000));
		List<Color> palette = new ArrayList<>(List.of(initialColor));
		//
		for (int i = 1; i < length; ++i) {
			// Calculates the squared distance for each color. The color is skipped if its
			// already in the palette (as we mustn't pick the same color twice) or if it has
			// already been processed in an earlier iteration (as it would have the same
			// distance).
			Map<Color, BigDecimal> colorByDistanceSquared = new ConcurrentHashMap<>();
			getClosestColors(source, palette) //
					.parallelStream() //
					.forEach(association -> {
						Color color = association.color();
						if (palette.contains(color) || colorByDistanceSquared.containsKey(color)) {
							return;
						}
						BigDecimal distance = BigDecimal.valueOf(association.distance());
						BigDecimal distanceSquared = distance.pow(2);
						colorByDistanceSquared.put(color, distanceSquared);
					});
			// Probability for each color := distanceSquared / totalDistanceSquared
			BigDecimal totalDistanceSquared = colorByDistanceSquared.values() //
					.stream() //
					.reduce((u, v) -> u.add(v)) //
					.orElse(BigDecimal.ZERO);
			// Sum up the individual probabilities and store them in a map. This is later
			// used to determine the "range" between [0, 1] that is covered by each color. A
			// random value is generated and the range in which this value lies then
			// determines the new color that is added to the palette.
			double probabilitySum = 0.0;
			Map<Double, Color> colorByProbabilitySum = new TreeMap<>();
			for (Map.Entry<Color, BigDecimal> entry : colorByDistanceSquared.entrySet()) {
				Color color = entry.getKey();
				BigDecimal distanceSquared = entry.getValue();
				//
				double probability = distanceSquared.divide(totalDistanceSquared, RoundingMode.HALF_EVEN).doubleValue();
				probabilitySum += probability;
				colorByProbabilitySum.put(probabilitySum, color);
			}
			// Because we use a tree-map, the probabilities are sorted from lowest to
			// highest. e.g. [0.0, 0.2],(0.2, 0.5],(0.5, 1.0]. We then iterate over those
			// ranges until we find the first one that is equal or larger than the randomly
			// generated value.
			double selection = rng.nextDouble();
			Iterator<Map.Entry<Double, Color>> iterator = colorByProbabilitySum.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Double, Color> entry = iterator.next();
				//
				// due to rounding errors, the total probability may not add up to 1.0. In this
				// case, simply select the last color
				if (!iterator.hasNext() || entry.getKey() >= selection) {
					palette.add(entry.getValue());
					break;
				}
			}
		}
		//
		palette.sort(Comparator.comparing(Color::getRGB));
		return Collections.unmodifiableList(palette);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utility methods for parallel, distributed operations
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * This algorithm partitions all colors that occur in the given image and groups
	 * them by the palette color they are the closest to. The process itself is
	 * split into the following parts: At first, the closest palette color is
	 * calculated for each color in the image. All colors mapped to the same palette
	 * color are then grouped in a map, using the palette color as key and a list of
	 * pixel colors as value. The algorithm is executed in parallel using
	 * {@link ForkJoinPool#commonPool()}.
	 * 
	 * @param source  The source image whose pixel colors are grouped by the palette
	 *                colors.
	 * @param palette The list of palette colors the pixel colors are grouped by.
	 * @return A map of all colors in {@code source}, grouped by the colors in
	 *         {@code palette}.
	 * @see #getClosestColors(BufferedImage, List)
	 */
	private static Map<Color, List<Color>> groupByCluster(BufferedImage source, List<Color> palette) {
		// Assignment step: Assign each observation to the cluster with the nearest
		// mean: that with the least squared Euclidean distance.
		List<PaletteColorAssociation> paletteColorAssociations = getClosestColors(source, palette);
		// Update step: Recalculate means (centroids) for observations assigned to each
		// cluster.
		Map<Color, List<Color>> globalClusters = new HashMap<>();
		// Execute within a thread pool to ensure that the number of parallel threads
		// are known.
		ForkJoinPool threadPool = ForkJoinPool.commonPool();
		threadPool.submit(() -> {
			IntStream.range(0, threadPool.getParallelism()) //
					.parallel() //
					.forEach(i -> {
						Map<Color, List<Color>> localClusters = new HashMap<>();
						while (i < paletteColorAssociations.size()) {
							PaletteColorAssociation colorAssociation = paletteColorAssociations.get(i);
							Color color = colorAssociation.color();
							Color paletteColor = colorAssociation.paletteColor();
							localClusters.computeIfAbsent(paletteColor, key -> new ArrayList<>()).add(color);
							i += threadPool.getParallelism();
						}
						synchronized (globalClusters) {
							localClusters.forEach((key, values) -> {
								globalClusters.computeIfAbsent(key, k -> new ArrayList<>()).addAll(values);
							});
						}
					});
		}).join();
		return globalClusters;
	}

	/**
	 * Iterates over all pixels of the given image (via its {@code x} and {@code y}
	 * coordinates} and performs the transformation function defined by
	 * {@code mapper}. This method doesn't modify the original image directly, but
	 * might do so as part of the mapper function. The mapper function must
	 * <b>not</b> return {@code null}. The mapper function is executed in parallel
	 * using {@link ForkJoinPool#commonPool()}.
	 * 
	 * @param <T>    The new object created by {@code mapper}.
	 * @param source The source image whose pixels are traversed.
	 * @param mapper Transformation function for each pixel in the given image :=
	 *               (x, y) → value
	 * @return An <b>unmodifiable</b> list of all modified pixels in the given image
	 *         in arbitrary order.
	 */
	private static <T> List<T> modifyPixel(BufferedImage source, BiFunction<Integer, Integer, T> mapper) {
		return IntStream.range(0, source.getWidth() * source.getHeight()) //
				.parallel() //
				.mapToObj(i -> {
					int x = i % source.getWidth();
					int y = i / source.getWidth();
					return mapper.apply(x, y);
				}) //
				.collect(Collectors.toUnmodifiableList());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Helper Classes
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Utility class that contains the association between a color and its closest
	 * match in the given palette. The inverse of this association represents all
	 * colors that match the same palette color. Those colors determine the means
	 * for the next iteration of the k-means algorithm.
	 */
	private static record PaletteColorAssociation(Color color, Color paletteColor, double distance) {
	}

	/**
	 * Utility class representing a image only using the colors contained by the
	 * given palette. Each instance of this class corresponds to a single iteration
	 * of the k-means algorithm.
	 */
	public static record ColorReducedImage(BufferedImage image, List<Color> palette) {
	}
}
