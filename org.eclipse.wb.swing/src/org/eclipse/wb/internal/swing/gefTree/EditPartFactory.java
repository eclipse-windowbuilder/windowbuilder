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
package org.eclipse.wb.internal.swing.gefTree;

import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.internal.swing.gefTree.part.ComponentEditPart;
import org.eclipse.wb.internal.swing.gefTree.part.ContainerEditPart;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.gef.EditPart;

/**
 * Implementation of {@link IEditPartFactory} for Swing.
 *
 * @author mitin_aa
 * @coverage swing.gefTree
 */
public final class EditPartFactory implements IEditPartFactory {
	////////////////////////////////////////////////////////////////////////////
	//
	// IEditPartFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public org.eclipse.wb.gef.core.EditPart createEditPart(EditPart context, Object model) {
		// components
		if (model instanceof ContainerInfo) {
			return new ContainerEditPart((ContainerInfo) model);
		} else if (model instanceof ComponentInfo) {
			return new ComponentEditPart((ComponentInfo) model);
		}
		// unknown
		return null;
	}
}
