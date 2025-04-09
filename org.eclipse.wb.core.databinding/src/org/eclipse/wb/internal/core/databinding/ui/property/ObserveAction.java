/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.core.databinding.ui.property;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;

/**
 * Action for create new binding.
 *
 * @author lobas_av
 * @coverage bindings.ui.properties
 */
public class ObserveAction extends ObjectInfoAction {
	private final AbstractObserveProperty m_property;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ObserveAction(ObjectInfo object, AbstractObserveProperty property) throws Exception {
		super(object);
		m_property = property;
		//
		IObservePresentation presentation = m_property.getObserveProperty().getPresentation();
		setText(presentation.getText());
		setImageDescriptor(presentation.getImageDescriptor());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ObjectInfoAction
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void runEx() throws Exception {
		m_property.createBinding();
	}
}