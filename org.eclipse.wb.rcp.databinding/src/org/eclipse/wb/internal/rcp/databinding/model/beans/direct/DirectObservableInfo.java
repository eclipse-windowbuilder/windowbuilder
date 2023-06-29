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
package org.eclipse.wb.internal.rcp.databinding.model.beans.direct;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.IMasterDetailProvider;

/**
 * {@link ObservableInfo} model for objects with observable types.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public final class DirectObservableInfo extends DirectPropertyObservableInfo
implements
IMasterDetailProvider {
	public static final String DETAIL_PROPERTY_NAME = "Detail for IObservableValue";

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public DirectObservableInfo(BindableInfo bindableObject, BindableInfo property) {
		super(bindableObject, property);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Variable
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getVariableIdentifier() throws Exception {
		return m_bindableObject.getReference();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IMasterDetailProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ObservableInfo getMasterObservable() throws Exception {
		for (IObserveInfo property : m_bindableObject.getChildren(ChildrenContext.ChildrenForPropertiesTable)) {
			if (DETAIL_PROPERTY_NAME.equals(property.getPresentation().getText())) {
				return new DirectObservableInfo(m_bindableObject, (BindableInfo) property);
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getPresentationText() throws Exception {
		return getBindableObject().getPresentation().getTextForBinding();
	}
}