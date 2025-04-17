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
package org.eclipse.wb.internal.core.databinding.ui.property;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @author lobas_av
 * @coverage bindings.ui.properties
 */
public final class Context {
	public final AbstractUIPlugin plugin;
	public final IDatabindingsProvider provider;
	public final ObjectInfo objectInfo;
	public IObserveInfo observeObject;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public Context(AbstractUIPlugin plugin, IDatabindingsProvider provider, ObjectInfo objectInfo) {
		this.plugin = plugin;
		this.provider = provider;
		this.objectInfo = objectInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public JavaInfo javaInfo() {
		return (JavaInfo) objectInfo;
	}
}