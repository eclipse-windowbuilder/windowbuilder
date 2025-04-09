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
package org.eclipse.wb.internal.rcp.databinding.emf.model.observables;

import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;

/**
 * Model for observable object <code>EMFObservables.observeDetailValue(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
public final class DetailValueEmfObservableInfo extends DetailEmfObservableInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DetailValueEmfObservableInfo(ObservableInfo masterObservable,
			PropertiesSupport propertiesSupport) {
		super(masterObservable, propertiesSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getPresentationPrefix() {
		return "Value";
	}
}