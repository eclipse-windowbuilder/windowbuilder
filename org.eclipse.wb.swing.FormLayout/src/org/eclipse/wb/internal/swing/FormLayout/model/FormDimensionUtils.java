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
package org.eclipse.wb.internal.swing.FormLayout.model;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.FormSpecs;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for {@link FormSpec} objects.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public class FormDimensionUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private FormDimensionUtils() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static Field[] m_templateFields;

	/**
	 * @return <code>true</code> if two {@link FormSpec} objects are equal.
	 */
	public static boolean equals(FormSpec a, FormSpec b) {
		if (a.getClass() == b.getClass()) {
			return a.getDefaultAlignment() == b.getDefaultAlignment()
					&& a.getResizeWeight() == b.getResizeWeight()
					&& a.getSize().equals(b.getSize());
		}
		return false;
	}

	/**
	 * @return the array of {@link Field}'s from {@link com.jgoodies.forms.layout.FormSpecs} with
	 *         {@link FormSpec} values.
	 */
	public static Field[] getTemplateFields() {
		if (m_templateFields == null) {
			List<Field> templateFieldsList = new ArrayList<>();
			Field[] fields = com.jgoodies.forms.layout.FormSpecs.class.getFields();
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (Modifier.isStatic(field.getModifiers())
						&& FormSpec.class.isAssignableFrom(field.getType())) {
					templateFieldsList.add(field);
				}
			}
			m_templateFields = templateFieldsList.toArray(new Field[templateFieldsList.size()]);
		}
		return m_templateFields;
	}

	/**
	 * @return the {@link FormSpec} {@link Field} from {@link FormSpecs} that has same value as given
	 *         or <code>null</code> if there are not such field.
	 */
	public static Field getFormFactoryTemplate(final FormSpec o) {
		return ExecutionUtils.runObject(() -> {
			Field[] templateFields = getTemplateFields();
			for (int i = 0; i < templateFields.length; i++) {
				Field field = templateFields[i];
				if (FormDimensionUtils.equals((FormSpec) field.get(null), o)) {
					return field;
				}
			}
			// no field found
			return null;
		});
	}
}
