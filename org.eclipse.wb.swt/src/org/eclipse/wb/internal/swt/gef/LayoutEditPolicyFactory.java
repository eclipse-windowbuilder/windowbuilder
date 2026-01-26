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
package org.eclipse.wb.internal.swt.gef;

import org.eclipse.wb.core.gef.policy.layout.ILayoutEditPolicyFactory;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swt.gef.policy.layout.AbsoluteLayoutEditPolicy;
import org.eclipse.wb.internal.swt.gef.policy.layout.form.FormLayoutEditPolicy;
import org.eclipse.wb.internal.swt.gef.policy.layout.form.FormLayoutEditPolicyClassic;
import org.eclipse.wb.internal.swt.gef.policy.layout.grid.GridLayoutEditPolicy;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplAutomatic;
import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo;

import org.eclipse.gef.EditPart;

/**
 * Implementation of {@link ILayoutEditPolicyFactory} for SWT.
 *
 * @author lobas_av
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage swt.gef
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
			return new AbsoluteLayoutEditPolicy<>((AbsoluteLayoutInfo) model);
		}
		if (model instanceof GridLayoutInfo) {
			return new GridLayoutEditPolicy<>((GridLayoutInfo) model);
		}
		if (model instanceof FormLayoutInfo formLayoutInfo) {
			if (formLayoutInfo.getImpl() instanceof FormLayoutInfoImplAutomatic) {
				return new FormLayoutEditPolicy<>(formLayoutInfo);
			} else {
				return new FormLayoutEditPolicyClassic<>(formLayoutInfo);
			}
		}
		return null;
	}
}