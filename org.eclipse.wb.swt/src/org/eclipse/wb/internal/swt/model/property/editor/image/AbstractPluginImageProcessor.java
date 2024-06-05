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
package org.eclipse.wb.internal.swt.model.property.editor.image;

import org.eclipse.wb.core.model.IGenericProperty;
import org.eclipse.wb.core.model.IImageProcessor;
import org.eclipse.wb.internal.swt.model.property.editor.image.plugin.PluginFileImagePage;

/**
 * Abstract base class for all image processors that access files via the OSGi
 * bundles.
 */
public abstract class AbstractPluginImageProcessor implements IImageProcessor {
	private static final String PREFIX = "Plugin: ";

	@Override
	public String getPageId() {
		return PluginFileImagePage.ID;
	}

	@Override
	public boolean process(IGenericProperty property, String[] value) {
		String[] imageValue = ImageEvaluator.getPluginImageValue(property);
		if (imageValue != null) {
			value[0] = PREFIX + imageValue[0] + " " + imageValue[1];
			return true;
		}
		return false;
	}

	@Override
	public boolean preOpen(IGenericProperty property, String text, Object[] value) {
		String[] values = ImageEvaluator.getPluginImageValue(property);
		if (values != null) {
			value[0] = values;
			return true;
		}
		return false;
	}
}
