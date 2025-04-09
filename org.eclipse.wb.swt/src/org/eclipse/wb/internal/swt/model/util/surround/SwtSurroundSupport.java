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
package org.eclipse.wb.internal.swt.model.util.surround;

import org.eclipse.wb.internal.core.model.util.surround.SurroundSupport;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * Helper for surrounding {@link ControlInfo}'s with some {@link CompositeInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.model.util
 */
public abstract class SwtSurroundSupport extends SurroundSupport<CompositeInfo, ControlInfo> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SwtSurroundSupport(CompositeInfo sourceContainer) {
		super(sourceContainer, ControlInfo.class);
	}
}
