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
package org.eclipse.wb.internal.core.databinding.ui.editor;

/**
 * This adapter class provides default implementations for the non GUI methods described by the
 * {@link IUiContentProvider} interface.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public abstract class UiContentProviderAdapter implements IUiContentProvider {
	////////////////////////////////////////////////////////////////////////////
	//
	// Complete
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setCompleteListener(ICompleteListener listener) {
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() throws Exception {
	}

	@Override
	public void saveToObject() throws Exception {
	}
}