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
package org.eclipse.wb.internal.swing.databinding.model;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.presentation.SimpleObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;

import java.util.Collections;
import java.util.List;

/**
 * Fake {@link ObserveInfo} for undefined properties.
 *
 * @author lobas_av
 * @coverage bindings.swing.model
 */
public final class UndefineObserveInfo extends ObserveInfo {
	private final IObservePresentation m_presentation;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public UndefineObserveInfo(String text, IReferenceProvider referenceProvider) {
		super(null, referenceProvider);
		m_presentation = new SimpleObservePresentation(text, text, null);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Type
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ObserveType getType() {
		return null;
	}

	@Override
	public ObserveCreationType getCreationType() {
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Hierarchy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IObserveInfo getParent() {
		return null;
	}

	@Override
	public List<IObserveInfo> getChildren(ChildrenContext context) {
		return Collections.emptyList();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IObservePresentation getPresentation() {
		return m_presentation;
	}
}