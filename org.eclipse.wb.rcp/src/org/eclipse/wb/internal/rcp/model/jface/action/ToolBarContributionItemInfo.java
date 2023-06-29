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
package org.eclipse.wb.internal.rcp.model.jface.action;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
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
		return ExecutionUtils.runObject(new RunnableObjectEx<Object>() {
			@Override
			public Object runObject() throws Exception {
				ToolBarManagerInfo managerInfo = getChildren(ToolBarManagerInfo.class).get(0);
				Object manager = managerInfo.getObject();
				return ((ToolBar) ReflectionUtils.invokeMethod(manager, "getControl()")).getParent();
			}
		});
	}
}
