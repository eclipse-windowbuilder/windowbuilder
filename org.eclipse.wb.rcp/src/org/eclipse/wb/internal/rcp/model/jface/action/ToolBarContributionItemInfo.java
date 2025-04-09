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
package org.eclipse.wb.internal.rcp.model.jface.action;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.swt.widgets.ToolBar;

/**
 * Model for {@link ToolBarContributionItem}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class ToolBarContributionItemInfo extends ContributionItemInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ToolBarContributionItemInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getComponentObject() {
		return ExecutionUtils.runObject(() -> {
			ToolBarManagerInfo managerInfo = getChildren(ToolBarManagerInfo.class).get(0);
			Object manager = managerInfo.getObject();
			return ((ToolBar) ReflectionUtils.invokeMethod(manager, "getControl()")).getParent();
		});
	}
}
