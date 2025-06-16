/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.core.gef.policy.layout;

import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.gef.EditPart;

import java.util.List;

/**
 * Contains utilities for {@link LayoutEditPolicy}'s.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public class LayoutPolicyUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// LayoutEditPolicy creation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link LayoutEditPolicy} for given model.
	 */
	public static LayoutEditPolicy createLayoutEditPolicy(org.eclipse.wb.gef.core.EditPart context, Object model) {
		// try to create policy
		List<ILayoutEditPolicyFactory> factories =
				ExternalFactoriesHelper.getElementsInstances(
						ILayoutEditPolicyFactory.class,
						"org.eclipse.wb.core.editPolicyFactories",
						"factory");
		for (ILayoutEditPolicyFactory factory : factories) {
			LayoutEditPolicy layoutEditPolicy = factory.createLayoutEditPolicy(context, model);
			if (layoutEditPolicy != null) {
				return layoutEditPolicy;
			}
		}
		// not found
		return null;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Side figures utilities
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Checks that selection edit policy should display side figures for given edit part.
	 */
	public static boolean shouldShowSideFigures(String figuresMode, EditPart part) {
		return part.getSelected() == EditPart.SELECTED_PRIMARY;
		// TODO implement full check
		/*boolean showFiguresAll = IDesignerPrefConstants.V_ALIGNMENT_FIGURES_ALL.equals(figuresMode);
    if (showFiguresAll) {
    	return true;
    }
    //
    boolean isSelected = part.getSelected() != EditPart.SELECTED_NONE;
    boolean isPrimarySelected = part.getSelected() == EditPart.SELECTED_PRIMARY;
    boolean showFiguresSelected = IDesignerPrefConstants.V_ALIGNMENT_FIGURES_SELECTED.equals(figuresMode);
    boolean showFiguresPrimarySelected = IDesignerPrefConstants.V_ALIGNMENT_FIGURES_PRIMARY_SELECTED.equals(figuresMode);
    return (showFiguresSelected && isSelected) || (showFiguresPrimarySelected && isPrimarySelected);*/
	}
}
