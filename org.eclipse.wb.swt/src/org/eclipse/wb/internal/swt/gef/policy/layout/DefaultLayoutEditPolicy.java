/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.gef.policy.layout;

import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * {@link LayoutEditPolicy} {@link CompositeInfo} without known {@link LayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.gef.policy
 */
public final class DefaultLayoutEditPolicy extends LayoutEditPolicy {
	////////////////////////////////////////////////////////////////////////////
	//
	// Decoration
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void decorateChild(EditPart child) {
		if (child.getModel() instanceof ControlInfo) {
			child.installEditPolicy(EditPolicy.SELECTION_ROLE, new NonResizableSelectionEditPolicy());
		}
	}
}