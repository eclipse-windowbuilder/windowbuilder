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
package org.eclipse.wb.internal.core.model.layout;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.variable.SyncParentChildVariableNameSupport;

/**
 * Support for managing name of <code>Layout</code>, so that it corresponds to the name of its
 * <code>Container</code>.
 *
 * @author sablin_aa
 * @coverage core.model.layout
 */
public abstract class LayoutNameSupport<T extends JavaInfo>
extends
SyncParentChildVariableNameSupport<T> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutNameSupport(T layout) {
		super(layout);
	}
}