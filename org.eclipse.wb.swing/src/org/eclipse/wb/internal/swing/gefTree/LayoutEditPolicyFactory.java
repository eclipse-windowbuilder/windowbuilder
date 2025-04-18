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
package org.eclipse.wb.internal.swing.gefTree;

import org.eclipse.wb.core.gefTree.policy.layout.ILayoutEditPolicyFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.gefTree.policy.AbsoluteLayoutEditPolicy;
import org.eclipse.wb.internal.swing.gefTree.policy.BorderLayoutEditPolicy;
import org.eclipse.wb.internal.swing.gefTree.policy.GenericFlowLayoutEditPolicy;
import org.eclipse.wb.internal.swing.gefTree.policy.GridBagLayoutEditPolicy;
import org.eclipse.wb.internal.swing.gefTree.policy.SpringLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.layout.BorderLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.GenericFlowLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.absolute.AbstractAbsoluteLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.spring.SpringLayoutInfo;

/**
 * Implementation of {@link ILayoutEditPolicyFactory} for Swing.
 *
 * @author mitin_aa
 * @coverage swing.gefTree
 */
public class LayoutEditPolicyFactory implements ILayoutEditPolicyFactory {
	////////////////////////////////////////////////////////////////////////////
	//
	// ILayoutEditPolicyFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public LayoutEditPolicy createLayoutEditPolicy(EditPart context, Object model) {
		if (model instanceof GenericFlowLayoutInfo) {
			return new GenericFlowLayoutEditPolicy((GenericFlowLayoutInfo) model);
		}
		if (model instanceof BorderLayoutInfo) {
			return new BorderLayoutEditPolicy((BorderLayoutInfo) model);
		}
		if (model instanceof GridBagLayoutInfo) {
			return new GridBagLayoutEditPolicy((GridBagLayoutInfo) model);
		}
		if (model instanceof AbstractAbsoluteLayoutInfo) {
			return new AbsoluteLayoutEditPolicy((AbstractAbsoluteLayoutInfo) model);
		}
		if (model instanceof SpringLayoutInfo) {
			return new SpringLayoutEditPolicy((SpringLayoutInfo) model);
		}
		return null;
	}
}
