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
package org.eclipse.wb.gef.core.tools;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utilities for {@link Tool}'s.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class ToolUtilities {
	/**
	 * Returns a list containing the top level selected {@link EditPart}'s based on the viewer's
	 * selection. If selection parents of edit parts is differed returns empty list.
	 */
	public static List<EditPart> getSelectionWithoutDependants(EditPartViewer viewer) {
		List<EditPart> operationSet = new ArrayList<>();
		// add selected EditPart's only if their parent is not added yet
		{
			List<? extends EditPart> selectedParts = viewer.getSelectedEditParts();
			for (EditPart part : selectedParts) {
				if (!isAncestorContainedIn(selectedParts, part)) {
					operationSet.add(part);
				}
			}
		}
		// check that all EditPart's have same parent
		{
			EditPart commonParent = null;
			for (EditPart editPart : operationSet) {
				if (commonParent == null) {
					commonParent = editPart.getParent();
				} else if (editPart.getParent() != commonParent) {
					return Collections.emptyList();
				}
			}
		}
		// OK, we have valid set
		return operationSet;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if <code>containers</code> contains parent of given {@link EditPart}.
	 */
	private static boolean isAncestorContainedIn(List<? extends EditPart> container, EditPart part) {
		EditPart parent = part.getParent();
		while (parent != null) {
			if (container.contains(parent)) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;
	}
}