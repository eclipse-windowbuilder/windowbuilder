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
 * Support for managing name of <code>LayoutData</code>, so that it corresponds to the name of its
 * parent <code>Control</code>.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage core.model.layout
 */
public abstract class LayoutDataNameSupport<T extends JavaInfo>
extends
SyncParentChildVariableNameSupport<T> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutDataNameSupport(T layoutData) {
		super(layoutData);
	}
}