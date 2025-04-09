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
package org.eclipse.wb.internal.swing.model.util.surround;

import org.eclipse.wb.internal.core.model.util.surround.SurroundSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

/**
 * Helper for surrounding {@link ComponentInfo}'s with some {@link ContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.model.util
 */
public abstract class SwingSurroundSupport extends SurroundSupport<ContainerInfo, ComponentInfo> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SwingSurroundSupport(ContainerInfo sourceContainer) {
		super(sourceContainer, ComponentInfo.class);
	}
}
