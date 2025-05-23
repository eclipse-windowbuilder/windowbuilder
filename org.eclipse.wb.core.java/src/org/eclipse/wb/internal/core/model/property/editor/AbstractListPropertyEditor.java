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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.IConfigurablePropertyObject;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import org.eclipse.swt.custom.CCombo;

import java.util.List;
import java.util.Map;

/**
 * The {@link PropertyEditor} for selecting single expression from given set.
 *
 * @author sablin_aa
 * @coverage core.model.property.editor
 */
public abstract class AbstractListPropertyEditor extends AbstractComboPropertyEditor
implements
IConfigurablePropertyObject,
IValueSourcePropertyEditor,
IClipboardSourceProvider {
	////////////////////////////////////////////////////////////////////////////
	//
	// TextDisplayPropertyEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getText(Property property) throws Exception {
		// return title for value
		Object value = property.getValue();
		if (value != Property.UNKNOWN_VALUE) {
			int index = getValueIndex(value);
			if (index >= 0) {
				return getTitle(index);
			} else {
				if (value instanceof String) {
					return (String) value;
				}
			}
		}
		// unknown value
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IValueSourcePropertyEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getValueSource(Object value) throws Exception {
		// return expression for value
		if (value != Property.UNKNOWN_VALUE) {
			int index = getValueIndex(value);
			if (index >= 0) {
				return getExpression(index);
			}
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

	////////////////////////////////////////////////////////////////////////////
	//
	// Combo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addItems(Property property, CCombo combo) throws Exception {
		for (int i = 0; i < getCount(); i++) {
			combo.add(getTitle(i));
		}
	}

	@Override
	protected void selectItem(Property property, CCombo combo) throws Exception {
		combo.setText(getText(property));
	}

	@Override
	protected void toPropertyEx(Property property, CCombo combo, int index) throws Exception {
		if (property instanceof GenericProperty genericProperty) {
			String expression = getExpression(index);
			Object evaluatedExpression = evaluateExpression(genericProperty, expression);
			// apply expression
			genericProperty.setExpression(expression, evaluatedExpression);
		} else {
			toPropertyEx_simpleProperty(property, combo, index);
		}
	}

	private static Object evaluateExpression(final GenericProperty genericProperty,
			final String expression) {
		return ExecutionUtils.runObjectIgnore(() -> {
			JavaInfo javaInfo = genericProperty.getJavaInfo();
			ClassLoader classLoader = JavaInfoUtils.getClassLoader(javaInfo);
			return ScriptUtils.evaluate(classLoader, expression);
		}, Property.UNKNOWN_VALUE);
	}

	/**
	 * Sets value of simple {@link Property}, not {@link GenericProperty}.
	 */
	protected void toPropertyEx_simpleProperty(Property property, CCombo combo, int index)
			throws Exception {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access to list items
	//
	////////////////////////////////////////////////////////////////////////////
	abstract protected int getCount();

	abstract protected int getValueIndex(Object value);

	abstract protected String getTitle(int index);

	abstract protected String getExpression(int index) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Extract string array from parameters.
	 */
	protected static String[] getParameterAsArray(Map<String, Object> parameters, String name) {
		return getParameterAsArray(parameters, name, false);
	}

	@SuppressWarnings("unchecked")
	protected static String[] getParameterAsArray(Map<String, Object> parameters,
			String name,
			boolean noAssert) {
		String[] values = null;
		if (parameters.containsKey(name)) {
			List<String> list = (List<String>) parameters.get(name);
			values = list.toArray(new String[list.size()]);
		} else {
			if (noAssert) {
				values = null;
			} else {
				Assert.fail(String.format("No parameter %s in %s.", name, parameters));
			}
		}
		return values;
	}
}