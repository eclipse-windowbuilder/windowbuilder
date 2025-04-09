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

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * Dispose listener for clear images loaded from {@code InternalImageManager}.
 *
 * @author lobas_av
 * @author sablin_aa
 * @coverage rcp.util
 */
public final class ActivatorGetImagesDisposer implements IRootProcessor {
	////////////////////////////////////////////////////////////////////////////
	//
	// IRootProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void process(JavaInfo root, List<JavaInfo> components) throws Exception {
		processRoot(root);
	}

	private void processRoot(final JavaInfo root) {
		root.addBroadcastListener(new ObjectEventListener() {
			@Override
			public void refreshDispose() throws Exception {
				disposeResources(root);
			}

			@Override
			public void dispose() throws Exception {
				disposeResources(root);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Dispose
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Disposes all images, loaded from {@code InternalImageManager}.
	 */
	private static void disposeResources(JavaInfo javaInfo) {
		try {
			ClassLoader classLoader = JavaInfoUtils.getClassLoader(javaInfo);
			Class<?> internalImageManager =
					classLoader.loadClass("org.eclipse.wb.internal.rcp.model.util.InternalImageManager");
			ReflectionUtils.invokeMethod(internalImageManager, "dispose()");
		} catch (Throwable e) {
		}
	}
}