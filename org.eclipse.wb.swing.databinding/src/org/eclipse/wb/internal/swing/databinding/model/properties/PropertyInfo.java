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
package org.eclipse.wb.internal.swing.databinding.model.properties;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.UiUtils;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;

/**
 * Model for {@link org.jdesktop.beansbinding.Property}.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.properties
 */
public abstract class PropertyInfo extends AstObjectInfo {
	protected final IGenericType m_sourceObjectType;
	protected IGenericType m_valueType;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PropertyInfo(IGenericType sourceObjectType, IGenericType valueType) {
		m_sourceObjectType = sourceObjectType;
		m_valueType = valueType;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public boolean canShared(PropertyInfo property) {
		return false;
	}

	public final IGenericType getSourceObjectType() {
		return m_sourceObjectType;
	}

	public final IGenericType getValueType() {
		return m_valueType;
	}

	public final void setValueType(IGenericType valueType) {
		m_valueType = valueType;
	}

	/**
	 * XXX
	 */
	public abstract ObserveInfo getObserveProperty(ObserveInfo observeObject) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the text for visual presentation of this object.
	 */
	public String getPresentationText(IObserveInfo observeObject,
			IObserveInfo observeProperty,
			boolean full) throws Exception {
		return UiUtils.getPresentationText(observeObject, observeProperty);
	}
}