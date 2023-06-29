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