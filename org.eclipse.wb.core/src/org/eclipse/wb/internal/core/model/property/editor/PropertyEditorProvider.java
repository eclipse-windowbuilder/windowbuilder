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

import java.beans.PropertyDescriptor;

/**
 * Provider for creating {@link PropertyEditor}'s.
 *
 * @author lobas_av
 * @coverage core.model.property.editor
 */
public class PropertyEditorProvider {
	/**
	 * @return the {@link PropertyEditor} for given property type or <code>null</code>.
	 */
	public PropertyEditor getEditorForType(Class<?> propertyType) throws Exception {
		return null;
	}

	/**
	 * @return the {@link PropertyEditor} for given {@link java.beans.PropertyEditor} editor type or
	 *         <code>null</code>.
	 */
	public PropertyEditor getEditorForEditorType(Class<?> editorType) throws Exception {
		return null;
	}

	/**
	 * @return the {@link PropertyEditor} for given {@link PropertyDescriptor} or <code>null</code>.
	 */
	public PropertyEditor getEditorForPropertyDescriptor(PropertyDescriptor descriptor)
			throws Exception {
		return null;
	}
}