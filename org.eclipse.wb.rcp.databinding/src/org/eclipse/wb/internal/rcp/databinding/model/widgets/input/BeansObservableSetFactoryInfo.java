/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input;

/**
 * Model for observable object <code>BeanProperties.set(...).setFactory(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class BeansObservableSetFactoryInfo extends AbstractFactoryInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BeansObservableSetFactoryInfo() {
		super("set");
	}
}