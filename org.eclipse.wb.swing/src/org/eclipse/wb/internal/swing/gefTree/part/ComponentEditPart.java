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
package org.eclipse.wb.internal.swing.gefTree.part;

import org.eclipse.wb.core.gefTree.part.JavaEditPart;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

/**
 * {@link EditPart} for {@link ComponentInfo} for GEF Tree.
 *
 * @author mitin_aa
 * @coverage swing.gefTree.part
 */
public class ComponentEditPart extends JavaEditPart {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ComponentEditPart(ComponentInfo model) {
		super(model);
	}
}
