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
package org.eclipse.wb.internal.core.model.property.editor;

import org.eclipse.wb.internal.core.model.property.ITypedProperty;
import org.eclipse.wb.internal.core.model.property.Property;

/**
 * The {@link PropertyEditor} for selecting single value of type {@link Enum<?>}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage core.model.property.editor
 */
public final class EnumPropertyEditor extends AbstractEnumPropertyEditor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final EnumPropertyEditor INSTANCE = new EnumPropertyEditor();

	private EnumPropertyEditor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Combo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Enum<?>[] getElements(Property property) throws Exception {
		Enum<?>[] elements = null;
		if (property instanceof ITypedProperty) {
			Class<?> typeClass = ((ITypedProperty) property).getType();
			if (typeClass.isEnum()) {
				elements = (Enum<?>[]) typeClass.getEnumConstants();
			}
		}
		return elements == null ? new Enum<?>[0] : elements;
	}
}