/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.internal.core.model.description.rules;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionPropertiesHelper;
import org.eclipse.wb.internal.core.model.property.accessor.FieldAccessor;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

import org.apache.commons.lang3.function.FailableBiConsumer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * The {@link FailableBiConsumer} that adds standard properties for
 * <code>.xxxx</code> public fields.
 *
 * @author lobas_av
 * @coverage core.model.description
 */
public final class PublicFieldPropertiesRule implements FailableBiConsumer<ComponentDescription, Object, Exception> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(ComponentDescription componentDescription, Object object) throws Exception {
		Class<?> componentClass = componentDescription.getComponentClass();
		for (Field field : componentClass.getFields()) {
			int modifiers = field.getModifiers();
			if (!Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)) {
				addSingleProperty(componentDescription, field);
			}
		}
	}

	/**
	 * Adds single {@link GenericPropertyDescription} for given field.
	 */
	private static void addSingleProperty(ComponentDescription componentDescription, Field field)
			throws Exception {
		String title = field.getName();
		String id = title;
		Class<?> type = field.getType();
		// prepare property parts
		PropertyEditor editor = DescriptionPropertiesHelper.getEditorForType(type);
		ExpressionConverter converter = DescriptionPropertiesHelper.getConverterForType(type);
		FieldAccessor accessor = new FieldAccessor(field);
		// create property
		GenericPropertyDescription propertyDescription =
				new GenericPropertyDescription(id, title, type);
		propertyDescription.addAccessor(accessor);
		propertyDescription.setConverter(converter);
		propertyDescription.setEditor(editor);
		// add property
		componentDescription.addProperty(propertyDescription);
	}
}