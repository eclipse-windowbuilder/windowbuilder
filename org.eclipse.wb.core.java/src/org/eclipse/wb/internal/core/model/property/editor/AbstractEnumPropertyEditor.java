/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.model.property.editor;

import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.custom.CCombo;

/**
 * The base {@link PropertyEditor} for selecting single value of type {@link Enum}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage core.model.property.editor
 */
public abstract class AbstractEnumPropertyEditor extends AbstractComboPropertyEditor
implements
IValueSourcePropertyEditor,
ITextValuePropertyEditor,
IClipboardSourceProvider {
	////////////////////////////////////////////////////////////////////////////
	//
	// TextDisplayPropertyEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getText(Property property) throws Exception {
		Object value = property.getValue();
		// return title for value
		if (value instanceof Enum<?> element) {
			return element.toString();
		}
		// unknown value
		return null;
	}

	@Override
	public void setText(Property property, String text) throws Exception {
		for (Enum<?> element : getElements(property)) {
			if (element.toString().equals(text)) {
				setPropertyValue(property, element);
				break;
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Combo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addItems(Property property, CCombo combo) throws Exception {
		Enum<?>[] elements = getElements(property);
		for (Enum<?> element : elements) {
			combo.add(element.toString());
		}
	}

	@Override
	protected void selectItem(Property property, CCombo combo) throws Exception {
		combo.setText(getText(property));
	}

	@Override
	protected void toPropertyEx(Property property, CCombo combo, int index) throws Exception {
		Enum<?>[] elements = getElements(property);
		Enum<?> element = elements[index];
		setPropertyValue(property, element);
	}

	/**
	 * @return array of available values.
	 */
	abstract protected Enum<?>[] getElements(Property property) throws Exception;

	/**
	 * Apply new selected value to {@link Property}.
	 */
	protected void setPropertyValue(Property property, Enum<?> element) throws Exception {
		if (property instanceof GenericProperty genericProperty) {
			String source = getValueSource(element);
			genericProperty.setExpression(source, element);
		} else {
			property.setValue(element);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IValueSourcePropertyEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getValueSource(Object value) throws Exception {
		if (value instanceof Enum<?> element) {
			return ReflectionUtils.getFullyQualifiedName(element.getDeclaringClass(), false)
					+ "."
					+ element.name();
		}
		// unknown value
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IClipboardSourceProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getClipboardSource(GenericProperty property) throws Exception {
		Object value = property.getValue();
		return getValueSource(value);
	}
}