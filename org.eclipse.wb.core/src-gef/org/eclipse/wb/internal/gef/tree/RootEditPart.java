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
package org.eclipse.wb.internal.gef.tree;

import org.eclipse.gef.TreeEditPart;
import org.eclipse.gef.editparts.RootTreeEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 *
 * @author lobas_av
 * @coverage gef.tree
 */
public class RootEditPart extends RootTreeEditPart {
	@Override
	protected void addChildVisual(org.eclipse.gef.EditPart childPart, int index) {
		getContents().setWidget(new TreeItem(getWidget(), SWT.NONE));
	}
	@Override
	public Tree getWidget() {
		return (Tree) getViewer().getControl();
	}

	@Override
	public TreeEditPart getContents() {
		return (TreeEditPart) super.getContents();
	}
}