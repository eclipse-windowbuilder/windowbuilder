/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.editor.icon;

import org.eclipse.wb.core.model.IGenericProperty;
import org.eclipse.wb.core.model.IImageInfo;
import org.eclipse.wb.core.model.IImageProcessor;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.DefaultImagePage;

/**
 * Default implementation when code no could should be generated.
 */
public final class DefaultImageProcessor implements IImageProcessor {
	public static final IImageProcessor INSTANCE = new DefaultImageProcessor();

	private DefaultImageProcessor() {
		// private due to singleton pattern
	}

	@Override
	public String getPageId() {
		return DefaultImagePage.ID;
	}

	@Override
	public boolean process(IGenericProperty property, String[] value) {
		return false;
	}

	@Override
	public boolean preOpen(IGenericProperty property, String text, Object[] value) {
		if (text == null) {
			value[0] = null;
			return true;
		}
		return false;
	}

	@Override
	public boolean postOpen(IGenericProperty property, IImageInfo imageInfo, String[] value) {
		if (getPageId().equals(imageInfo.getPageId())) {
			value[0] = null;
			return true;
		}
		return false;
	}
}
