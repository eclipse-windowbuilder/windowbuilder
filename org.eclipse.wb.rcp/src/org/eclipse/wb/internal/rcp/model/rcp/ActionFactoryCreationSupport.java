/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.rcp;

import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.ExecutionFlowFrameVisitor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.IActionIconProvider;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import net.bytebuddy.ByteBuddy;

/**
 * {@link CreationSupport} for {@link Action} created from {@link ActionFactory}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class ActionFactoryCreationSupport extends CreationSupport
implements
IActionIconProvider {
	private MethodInvocation m_invocation;
	private final String m_name;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ActionFactoryCreationSupport(MethodInvocation node, String name) {
		m_invocation = node;
		m_name = name;
	}

	private ActionFactoryCreationSupport(String name) {
		m_name = name;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Action_Info creation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @param name
	 *          then name of field from {@link ActionFactory}.
	 *
	 * @return the {@link ActionInfo} for {@link IWorkbenchAction} from {@link ActionFactory}.
	 */
	public static ActionInfo createNew(JavaInfo root, String name) throws Exception {
		// prepare new Action_Info
		ActionInfo action;
		{
			AstEditor editor = root.getEditor();
			action =
					(ActionInfo) JavaInfoUtils.createJavaInfo(
							editor,
							"org.eclipse.jface.action.IAction",
							new ActionFactoryCreationSupport(name));
		}
		// set default variable name
		{
			StringBuffer newName = new StringBuffer(name.length() + 16);
			boolean hasUnderscore = false;
			for (int i = 0; i < name.length(); i++) {
				char c = name.charAt(i);
				if (c == '_') {
					hasUnderscore = true;
					continue;
				} else {
					if (hasUnderscore) {
						c = Character.toUpperCase(c);
						hasUnderscore = false;
					} else {
						c = Character.toLowerCase(c);
					}
					newName.append(c);
				}
			}
			newName.append("Action");
			// do set default variable name
			JavaInfoUtils.setParameter(action, NamesManager.NAME_PARAMETER, newName.toString());
		}
		// OK, we have Action_Info
		return action;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return m_invocation != null ? "ActionFactory." + m_name : "ActionFactory";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ASTNode getNode() {
		return m_invocation;
	}

	@Override
	public boolean isJavaInfo(ASTNode node) {
		return node == m_invocation;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IActionIconProvider
	//
	////////////////////////////////////////////////////////////////////////////
	public static final ImageDescriptor DEFAULT_ICON = Activator.getImageDescriptor("info/Action/workbench_action.gif");

	@Override
	public ImageDescriptor getActionIcon() {
		return DEFAULT_ICON;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Evaluation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canBeEvaluated() {
		return false;
	}

	@Override
	public Object create(EvaluationContext context, ExecutionFlowFrameVisitor visitor)
			throws Exception {
		// create Action object
		IAction action;
		{
			ClassLoader editorLoader = context.getClassLoader();
			action = new ByteBuddy() //
					.subclass(Action.class) //
					.make() //
					.load(editorLoader) //
					.getLoaded() //
					.getConstructor() //
					.newInstance();
		}
		// create IWorkbenchAction from "this" Eclipse
		IWorkbenchAction thisAction;
		{
			ActionFactory actionFactory =
					(ActionFactory) ReflectionUtils.getFieldObject(ActionFactory.class, m_name);
			thisAction = actionFactory.create(DesignerPlugin.getActiveWorkbenchWindow());
		}
		// configure Action object
		try {
			action.setText(thisAction.getText());
			action.setDescription(thisAction.getDescription());
			action.setToolTipText(thisAction.getToolTipText());
			action.setImageDescriptor(thisAction.getImageDescriptor());
		} finally {
			thisAction.dispose();
		}
		// OK, we have Action object
		return action;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canReorder() {
		return true;
	}

	@Override
	public boolean canReparent() {
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canDelete() {
		return true;
	}

	@Override
	public void delete() throws Exception {
		JavaInfoUtils.deleteJavaInfo(m_javaInfo, true);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Create
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String add_getSource(NodeTarget target) throws Exception {
		return "org.eclipse.ui.actions.ActionFactory." + m_name + ".create(window)";
	}

	@Override
	public void add_setSourceExpression(Expression expression) throws Exception {
		m_invocation = (MethodInvocation) expression;
		m_javaInfo.bindToExpression(m_invocation);
	}
}
