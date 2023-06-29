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
package org.eclipse.wb.internal.xwt.gef;

import org.eclipse.wb.core.gef.policy.layout.ILayoutEditPolicyFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid.TableWrapLayoutEditPolicy;
import org.eclipse.wb.internal.swt.gef.policy.layout.AbsoluteLayoutEditPolicy;
import org.eclipse.wb.internal.swt.gef.policy.layout.form.FormLayoutEditPolicy;
import org.eclipse.wb.internal.swt.gef.policy.layout.form.FormLayoutEditPolicyClassic;
import org.eclipse.wb.internal.swt.gef.policy.layout.grid.GridLayoutEditPolicy;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplAutomatic;
import org.eclipse.wb.internal.xwt.model.forms.layout.table.TableWrapLayoutInfo;
import org.eclipse.wb.internal.xwt.model.layout.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.xwt.model.layout.form.FormLayoutInfo;
import org.eclipse.wb.internal.xwt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

/**
 * {@link ILayoutEditPolicyFactory} for XWT.
 *
 * @author scheglov_ke
 * @coverage XWT.gef
 */
public final class LayoutEditPolicyFactory implements ILayoutEditPolicyFactory {
	////////////////////////////////////////////////////////////////////////////
	//
	// ILayoutEditPolicyFactory
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutEditPolicy createLayoutEditPolicy(EditPart context, Object model) {
		if (model instanceof GridLayoutInfo) {
			return new GridLayoutEditPolicy<ControlInfo>((GridLayoutInfo) model);
		}
		if (model instanceof TableWrapLayoutInfo) {
			return new TableWrapLayoutEditPolicy<ControlInfo>((TableWrapLayoutInfo) model);
		}
		if (model instanceof AbsoluteLayoutInfo) {
			return new AbsoluteLayoutEditPolicy<ControlInfo>((AbsoluteLayoutInfo) model);
		}
		if (model instanceof FormLayoutInfo) {
			FormLayoutInfo formLayoutInfo = (FormLayoutInfo) model;
			if (formLayoutInfo.getImpl() instanceof FormLayoutInfoImplAutomatic) {
				return new FormLayoutEditPolicy<ControlInfo>(formLayoutInfo);
			} else {
				return new FormLayoutEditPolicyClassic<ControlInfo>(formLayoutInfo);
			}
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
    if (model instanceof AbsoluteLayout_Info) {
    	return new Absolute_LayoutEditPolicy(((AbsoluteLayout_Info) model));
    }
    if (model instanceof TableLayout_Info) {
    	return new Table_LayoutEditPolicy(((TableLayout_Info) model));
    }*/
		// not found
		return null;
	}
}
