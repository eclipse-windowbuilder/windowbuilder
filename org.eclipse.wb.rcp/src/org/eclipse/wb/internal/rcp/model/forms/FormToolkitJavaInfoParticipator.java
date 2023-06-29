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
package org.eclipse.wb.internal.rcp.model.forms;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.IJavaInfoInitializationParticipator;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.IActionSingleton;
import org.eclipse.wb.internal.rcp.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import java.util.List;

/**
 * {@link IJavaInfoInitializationParticipator} that contributes {@link FormToolkit} related actions
 * into context menu.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class FormToolkitJavaInfoParticipator implements IJavaInfoInitializationParticipator {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final Object INSTANCE = new FormToolkitJavaInfoParticipator();

	private FormToolkitJavaInfoParticipator() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IJavaInfoInitializationParticipator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void process(JavaInfo javaInfo) throws Exception {
		if (javaInfo instanceof InstanceFactoryInfo
				&& ReflectionUtils.isSuccessorOf(
						javaInfo.getDescription().getComponentClass(),
						"org.eclipse.ui.forms.widgets.FormToolkit")) {
			final InstanceFactoryInfo toolkit = (InstanceFactoryInfo) javaInfo;
			toolkit.addBroadcastListener(new JavaEventListener() {
				@Override
				public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
					IPreferenceStore preferences = child.getDescription().getToolkit().getPreferences();
					support_paintBordersFor(toolkit, preferences, child);
					support_adaptControl(toolkit, preferences, child);
				}
			});
			toolkit.addBroadcastListener(new ObjectEventListener() {
				@Override
				public void addContextMenu(List<? extends ObjectInfo> objects,
						ObjectInfo object,
						IMenuManager manager) throws Exception {
					contribute_FormToolkit_decorateFormHeading(manager, toolkit, object);
				}
			});
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Adapt new Control/Composite
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds {@link FormToolkit#paintBordersFor(org.eclipse.swt.widgets.Composite)}.
	 */
	private void support_paintBordersFor(InstanceFactoryInfo toolkit,
			IPreferenceStore preferences,
			JavaInfo child) throws Exception {
		if (preferences.getBoolean(IPreferenceConstants.FORMS_PAINT_BORDERS)
				&& JavaInfoUtils.hasTrueParameter(child, "FormsAPI.paintBordersFor")) {
			addToolkitInvocation("{0}.paintBordersFor({1})", toolkit, child);
		}
	}

	/**
	 * Adds {@link FormToolkit#adapt(org.eclipse.swt.widgets.Composite)} or
	 * {@link FormToolkit#adapt(org.eclipse.swt.widgets.Control, boolean, boolean)} depending on type
	 * of "child".
	 */
	private void support_adaptControl(InstanceFactoryInfo toolkit,
			IPreferenceStore preferences,
			JavaInfo child) throws Exception {
		if (preferences.getBoolean(IPreferenceConstants.FORMS_ADAPT_CONTROL)
				&& child instanceof ControlInfo
				&& child.getCreationSupport() instanceof ConstructorCreationSupport
				&& isVisibleControl(child)) {
			if (JavaInfoUtils.hasTrueParameter(child, "FormsAPI.adapt(Composite)")) {
				addToolkitInvocation("{0}.adapt({1})", toolkit, child);
			} else {
				addToolkitInvocation("{0}.adapt({1}, true, true)", toolkit, child);
			}
		}
	}

	/**
	 * @return <code>true</code> if given {@link JavaInfo} is visible, i.e. not filler.
	 */
	private static boolean isVisibleControl(JavaInfo child) throws Exception {
		return child.getParentJava().getPresentation().getChildrenGraphical().contains(child);
	}

	/**
	 * Adds invocation of "toolkit" with "child" as one of arguments. Adds related nodes.
	 */
	private static void addToolkitInvocation(String format,
			InstanceFactoryInfo toolkit,
			JavaInfo child) throws Exception {
		String source = TemplateUtils.format(format, toolkit, child);
		Expression expression = child.addExpressionStatement(source);
		toolkit.addRelatedNodes(expression);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Contributes {@link IAction} to add/remove {@link FormToolkit#decorateFormHeading(Form)}.
	 */
	private void contribute_FormToolkit_decorateFormHeading(IMenuManager manager,
			InstanceFactoryInfo toolkit,
			ObjectInfo object) {
		if (object instanceof FormInfo) {
			FormInfo form = (FormInfo) object;
			manager.appendToGroup(
					IContextMenuConstants.GROUP_ADDITIONAL,
					new DecorateFormHeading_Action(toolkit, form));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DecorateFormHeading_Action
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link IAction} to add/remove {@link FormToolkit#decorateFormHeading(Form)}.
	 */
	private static class DecorateFormHeading_Action extends ObjectInfoAction
	implements
	IActionSingleton {
		private static final String SIGNATURE =
				"decorateFormHeading(org.eclipse.ui.forms.widgets.Form)";
		private final InstanceFactoryInfo m_toolkit;
		private final FormInfo m_form;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public DecorateFormHeading_Action(InstanceFactoryInfo toolkit, FormInfo form) {
			super(form, "Decorate heading", AS_CHECK_BOX);
			m_toolkit = toolkit;
			m_form = form;
			// update check state
			for (MethodInvocation invocation : m_toolkit.getMethodInvocations(SIGNATURE)) {
				Expression formExpression = DomGenerics.arguments(invocation).get(0);
				if (m_form.isRepresentedBy(formExpression)) {
					setChecked(true);
				}
			}
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Run
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void runEx() throws Exception {
			if (isChecked()) {
				String source = TemplateUtils.format("{0}.decorateFormHeading({1})", m_toolkit, m_form);
				Expression expression = m_form.addExpressionStatement(source);
				m_toolkit.addRelatedNodes(expression);
			} else {
				for (MethodInvocation invocation : m_toolkit.getMethodInvocations(SIGNATURE)) {
					Expression formExpression = DomGenerics.arguments(invocation).get(0);
					if (m_form.isRepresentedBy(formExpression)) {
						m_form.getEditor().removeEnclosingStatement(invocation);
					}
				}
			}
		}
	}
}
