/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.model.property.editor.color;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorInfo;

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
				for (Field field : SWT.class.getFields()) {
					if (field.getName().startsWith("COLOR_")) {
						colors.add(ColorPropertyEditor.createInfo(field));
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