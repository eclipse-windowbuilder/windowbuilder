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
package org.eclipse.wb.internal.xwt.gefTree;

import org.eclipse.wb.core.gefTree.policy.layout.ILayoutEditPolicyFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swt.gefTree.policy.AbsoluteLayoutEditPolicy;
import org.eclipse.wb.internal.swt.gefTree.policy.FormLayoutEditPolicy;
import org.eclipse.wb.internal.xwt.model.layout.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.xwt.model.layout.form.FormLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

/**
 * {@link ILayoutEditPolicyFactory} for XWT.
 *
 * @author scheglov_ke
 * @coverage XWT.gefTree
 */
public final class LayoutEditPolicyFactory implements ILayoutEditPolicyFactory {
	////////////////////////////////////////////////////////////////////////////
	//
	// ILayoutEditPolicyFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public LayoutEditPolicy createLayoutEditPolicy(EditPart context, Object model) {
		if (model instanceof AbsoluteLayoutInfo) {
			return new AbsoluteLayoutEditPolicy<ControlInfo>((AbsoluteLayoutInfo) model);
		}
		if (model instanceof FormLayoutInfo) {
			return new FormLayoutEditPolicy<ControlInfo>((FormLayoutInfo) model);
		}
		/*if (model instanceof GenericFlowLayout_Info) {
    	return new GenericFlowLayout_LayoutEditPolicy((GenericFlowLayout_Info) model);
    }*/
		/*if (model instanceof DefaultLayout_Info) {
    	return new Default_LayoutEditPolicy(((DefaultLayout_Info) model));
    }
    if (model instanceof BorderLayout_Info) {
    	return new Border_LayoutEditPolicy(((BorderLayout_Info) model));
    }
    if (model instanceof TableLayout_Info) {
    	return new Table_LayoutEditPolicy(((TableLayout_Info) model));
    }*/
		// not found
		return null;
	}
}
