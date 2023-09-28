/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.property.editor.color;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorInfo;
import org.eclipse.wb.internal.swt.support.ColorSupport;
import org.eclipse.wb.internal.swt.support.SwtSupport;

import org.eclipse.swt.SWT;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lobas_av
 * @coverage swt.property.editor
 */
public final class SwtColors {
	private static ColorInfo[] m_systemColors;

	////////////////////////////////////////////////////////////////////////////
	//
	// System
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link ColorInfo}'s for colors from {@link SWT.COLOR_XXX}.
	 */
	public static ColorInfo[] getSystemColors(JavaInfo javaInfo) {
		if (m_systemColors == null) {
			List<ColorInfo> colors = new ArrayList<>();
			try {
				Field[] fields = SwtSupport.getSwtClass().getFields();
				for (int i = 0; i < fields.length; i++) {
					Field field = fields[i];
					String name = field.getName();
					if (name.startsWith("COLOR_")) {
						colors.add(ColorSupport.createInfo(field));
					}
				}
			} catch (Throwable e) {
				throw ReflectionUtils.propagate(e);
			}
			m_systemColors = colors.toArray(new ColorInfo[colors.size()]);
		}
		return m_systemColors;
	}
}