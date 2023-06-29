/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.IMasterDetailProvider;

/**
 * Accessor model for describe method invocation <code>getKnownElements()</code> on observable
 * <code>JFace</code> content provider for viewer. For example:
 * {@link org.eclipse.jface.databinding.viewers.ObservableListContentProvider#getKnownElements()}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class KnownElementsObservableInfo extends ObservableInfo implements IMasterDetailProvider {
	private final AstObjectInfo m_parent;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public KnownElementsObservableInfo(AstObjectInfo parent) {
		m_parent = parent;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link AstObjectInfo} for content provider.
	 */
	public AstObjectInfo getParent() {
		return m_parent;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ObservableInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public BindableInfo getBindableObject() {
		return null;
	}

	@Override
	public BindableInfo getBindableProperty() {
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return access source code.
	 */
	public String getSourceCode() throws Exception {
		return m_parent.getVariableIdentifier() + ".getKnownElements()";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IMasterDetailProvider
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public ObservableInfo getMasterObservable() throws Exception {
		return this;
	}
}