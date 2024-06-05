/*******************************************************************************
 * Copyright (c) 2024 DSA GmbH, Aachen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    DSA GmbH, Aachen - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.editor.icon;

import org.eclipse.wb.core.model.IImageProcessor;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Utility class for loading the image processors using the extension point.
 */
public final class ImageProcessorHelper {
	private static final String EXTENSION_POINT = "org.eclipse.wb.core.java.imageProcessor";
	private static final String NAME = "processor";

	private ImageProcessorHelper() {
		// Helper class should not be instantiated
	}

	/**
	 * @param type An arbitrary image class.
	 * @return An unmodifiable list of all image processors that support the given
	 *         type.
	 */
	private static List<IImageProcessor> getElements(Class<?> type) {
		Assert.isNotNull(type, "Type must not be null.");
		List<IImageProcessor> elements = new ArrayList<>();

		for (IConfigurationElement element : ExternalFactoriesHelper.getElements(EXTENSION_POINT, NAME)) {
			String typeName = element.getAttribute("type");
			Assert.isNotNull(type, "Type attribute not set.");

			if (typeName.equals(type.getName())) {
				IImageProcessor imageSource = ExternalFactoriesHelper.createExecutableExtension(element, "class");
				elements.add(imageSource);
			}
		}

		return Collections.unmodifiableList(elements);
	}

	/**
	 * This method is used to find the first processor that was able to handle a
	 * given request. Requests are usually calls to one of the methods of
	 * {@link IImageProcessor}.
	 *
	 * Example:
	 *
	 * <pre>
	 * ImageProcessorHelper.process(Image.class, p -> p.processText(...));
	 * </pre>
	 *
	 * @param type      An arbitrary image class.
	 * @param predicate A process request
	 * @return The first image processor that was handled the request.
	 */
	public static IImageProcessor process(Class<?> type, Predicate<IImageProcessor> predicate) {
		for (IImageProcessor element : getElements(type)) {
			// Stop if image has been processed
			if (predicate.test(element)) {
				return element;
			}
		}
		return null;
	}
}
