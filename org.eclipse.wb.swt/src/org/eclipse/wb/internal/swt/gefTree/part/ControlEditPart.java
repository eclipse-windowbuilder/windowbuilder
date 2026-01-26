/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.gefTree.part;

import org.eclipse.wb.core.gefTree.part.JavaEditPart;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for {@link ControlInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.gefTree.part
 */
public class ControlEditPart extends JavaEditPart {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ControlEditPart(ControlInfo control) {
		super(control);
	}
}
