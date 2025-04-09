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
package org.eclipse.wb.internal.rcp.model.jface.action;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.VoidInvocationCreationSupport;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;

/**
 * Implementation of {@link CreationSupport} for {@link IContributionManager#add(IAction)}.<br>
 * This method internally creates {@link ActionContributionItem}, but does not return it.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class ContributionManagerActionCreationSupport extends VoidInvocationCreationSupport {
	private final ActionInfo m_action;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ContributionManagerActionCreationSupport(JavaInfo hostJavaInfo,
			MethodDescription description,
			MethodInvocation invocation,
			JavaInfo[] argumentInfos) {
		super(hostJavaInfo, description, invocation);
		m_action = (ActionInfo) argumentInfos[0];
	}

	public ContributionManagerActionCreationSupport(JavaInfo hostJavaInfo, ActionInfo action) {
		super(hostJavaInfo, getMethodDescription(hostJavaInfo));
		m_action = action;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static MethodDescription getMethodDescription(JavaInfo hostJavaInfo) {
		return hostJavaInfo.getDescription().getMethod("add(org.eclipse.jface.action.IAction)");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Object getObject(Object manager) throws Exception {
		Object[] items = (Object[]) ReflectionUtils.invokeMethod2(manager, "getItems");
		return items[items.length - 1];
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Special access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link ActionInfo} that is added to create this {@link ActionContributionItemInfo}.
	 */
	public ActionInfo getAction() {
		return m_action;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final boolean canReorder() {
		return true;
	}

	@Override
	public final boolean canReparent() {
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Adding
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String add_getMethodSource() throws Exception {
		return TemplateUtils.format("add({0})", m_action);
	}
}
