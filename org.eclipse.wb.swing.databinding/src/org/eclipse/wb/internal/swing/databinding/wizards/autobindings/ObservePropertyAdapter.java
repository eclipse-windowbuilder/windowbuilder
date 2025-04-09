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
package org.eclipse.wb.internal.swing.databinding.wizards.autobindings;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;

/**
 * {@link PropertyAdapter} for {@link ObserveInfo}.
 *
 * @author lobas_av
 * @coverage bindings.swing.wizard.auto
 */
public final class ObservePropertyAdapter extends PropertyAdapter {
	private final ObserveInfo m_observe;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ObservePropertyAdapter(ObserveInfo observe) throws Exception {
		super(observe.getPresentation().getText(), observe.getObjectType().getRawType());
		m_observe = observe;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public ObserveInfo getObserve() {
		return m_observe;
	}
}