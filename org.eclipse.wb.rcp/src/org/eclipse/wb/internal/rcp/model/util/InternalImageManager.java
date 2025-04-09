/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.util;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing OS resources associated with SWT/JFace images. Used to manage images
 * which are loaded from the users {@code Activator}.
 *
 * @author lobas_av
 * @coverage rcp.util
 */
public final class InternalImageManager {
	private static final Map<String, Image> IMAGES = new HashMap<>();
	private static final Map<String, ImageDescriptor> DESCRIPTORS = new HashMap<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link ImageDescriptor} the image descriptor stored in the file at the specified path.
	 */
	public static ImageDescriptor getImageDescriptor(String projectPath, String imagePath) {
		// prepare descriptor
		imagePath = configurePath(imagePath);
		ImageDescriptor descriptor = DESCRIPTORS.get(imagePath);
		// check create descriptor
		if (descriptor == null) {
			// prepare file path
			String fullDescriptorPath = findFile(projectPath, imagePath);
			if (fullDescriptorPath != null) {
				// create descriptor
				descriptor = ImageDescriptor.createFromFile(null, fullDescriptorPath);
				DESCRIPTORS.put(imagePath, descriptor);
			}
		}
		return descriptor;
	}

	/**
	 * @return {@link Image} the image stored in the file at the specified path.
	 */
	public static Image getImage(String projectPath, String imagePath) {
		// prepare image
		imagePath = configurePath(imagePath);
		Image image = IMAGES.get(imagePath);
		// check create image
		if (image == null) {
			// prepare file path
			String fullImagePath = findFile(projectPath, imagePath);
			if (fullImagePath != null) {
				// create image
				try {
					image = new Image(null, fullImagePath);
				} catch (Throwable e) {
					return null;
				}
				IMAGES.put(imagePath, image);
			}
		}
		return image;
	}

	/**
	 * Dispose of cached objects and their underlying OS resources.
	 */
	public static void dispose() {
		for (Image image : IMAGES.values()) {
			try {
				if (!image.isDisposed()) {
					image.dispose();
				}
			} catch (Throwable e) {
			}
		}
		IMAGES.clear();
		DESCRIPTORS.clear();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the configured path.
	 */
	private static String configurePath(String path) {
		// check wrong path
		if (path == null || path.length() == 0) {
			return "";
		}
		// replace slashes
		path = path.replace('\\', '/');
		if (path.charAt(0) == '/') {
			path = path.substring(1);
		}
		return path;
	}

	/**
	 * @return the full path to file or {@code null} if file is not exist.
	 */
	private static String findFile(String projectPath, String imagePath) {
		// direct link
		String fullPath = projectPath + "/" + imagePath;
		if (new File(fullPath).exists()) {
			return fullPath;
		}
		// with popular prefix 'icons'
		fullPath = projectPath + "/icons/" + imagePath;
		if (new File(fullPath).exists()) {
			return fullPath;
		}
		// with popular prefix 'images'
		fullPath = projectPath + "/images/" + imagePath;
		if (new File(fullPath).exists()) {
			return fullPath;
		}
		// not found
		return null;
	}
}