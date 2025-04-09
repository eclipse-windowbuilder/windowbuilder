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
package org.eclipse.wb.internal.core.model.generation;

import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.ui.AbstractBindingComposite;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;

/**
 * Abstract {@link Composite} for editing properties of {@link GenerationDescription}.
 *
 * @author scheglov_ke
 * @coverage core.preferences.ui
 */
public abstract class GenerationPropertiesComposite extends AbstractBindingComposite {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GenerationPropertiesComposite(Composite parent,
			DataBindManager bindManager,
			IPreferenceStore preferences) {
		super(parent, bindManager, preferences);
	}
}
