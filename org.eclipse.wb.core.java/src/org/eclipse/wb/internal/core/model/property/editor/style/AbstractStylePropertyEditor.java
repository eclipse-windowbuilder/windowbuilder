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
package org.eclipse.wb.internal.core.model.property.editor.style;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.EmptyProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.style.impl.BooleanStylePropertyImpl;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract {@link PropertyEditor} for editing "SWT Style"-like properties.
 *
 * These properties are complex properties with sub-properties of special
 * {@link SubStylePropertyImpl} types.
 *
 * @author lobas_av
 * @author mitin_aa
 * @coverage core.model.property.editor
 */
public abstract class AbstractStylePropertyEditor extends TextDisplayPropertyEditor
implements
IComplexPropertyEditor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	protected final List<SubStylePropertyImpl> m_macroProperties = new ArrayList<>();
	protected final List<SubStylePropertyImpl> m_otherProperties = new ArrayList<>();
	protected final List<SubStylePropertyImpl> m_properties = new ArrayList<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the current style value.
	 */
	protected long getStyleValue(Property property) throws Exception {
		Object propertyValue = property.getValue();
		if (Property.UNKNOWN_VALUE == propertyValue) {
			return 0;
		}
		Number value = (Number) propertyValue;
		return value != null ? value.longValue() : 0;
	}

	/**
	 * Sets the new value of given {@link SubStyleProperty}.
	 */
	protected abstract void setStyleValue(Property property, long newValue) throws Exception;

	/**
	 * @return the {@link Property} that has given value.
	 */
	protected static Property getPropertyForValue(final Object value) {
		return new EmptyProperty() {
			@Override
			public Object getValue() throws Exception {
				return value;
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// As string
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link String} presentation of this {@link StylePropertyEditor}, for tests.
	 */
	public String getAsString() {
		StringBuilder builder = new StringBuilder();
		for (SubStylePropertyImpl property : m_properties) {
			builder.append("\t");
			property.getAsString(builder);
			builder.append("\n");
		}
		return builder.toString();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Property[] getProperties(Property mainProperty) throws Exception {
		ObjectInfo objectInfo = mainProperty.getAdapter(ObjectInfo.class);
		Property[] properties = (Property[]) objectInfo.getArbitraryValue(this);
		if (properties == null) {
			int length = m_properties.size();
			properties = new Property[length];
			for (int i = 0; i < length; i++) {
				properties[i] = new SubStyleProperty(mainProperty, m_properties.get(i));
			}
			objectInfo.putArbitraryValue(this, properties);
		}
		return properties;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Popup menu
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Contributes actions into {@link Property} context menu.
	 */
	public void contributeActions(Property mainProperty,
			IMenuManager manager,
			String implementTitle,
			boolean isCascade) throws Exception {
		// prepare "implement" menu
		IMenuManager implementMenuManager = new MenuManager(implementTitle);
		if (isCascade) {
			// add all "boolean" properties
			for (SubStylePropertyImpl property : m_properties) {
				if (property instanceof BooleanStylePropertyImpl) {
					property.contributeActions(mainProperty, implementMenuManager);
				}
			}
			//
			implementMenuManager.add(new Separator());
			// add other properties
			for (SubStylePropertyImpl property : m_properties) {
				if (!(property instanceof BooleanStylePropertyImpl)) {
					IMenuManager subMenu = new MenuManager(property.getTitle());
					property.contributeActions(mainProperty, subMenu);
					implementMenuManager.add(subMenu);
				}
			}
		} else {
			for (SubStylePropertyImpl property : m_properties) {
				property.contributeActions(mainProperty, implementMenuManager);
			}
		}
		// add "implement" menu
		manager.appendToGroup(IContextMenuConstants.GROUP_LAYOUT, implementMenuManager);
	}
}