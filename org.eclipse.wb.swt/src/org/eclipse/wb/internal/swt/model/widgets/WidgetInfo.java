/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.widgets;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.variable.AbstractNamedVariableSupport;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swt.model.widgets.live.SwtLiveManager;
import org.eclipse.wb.internal.swt.support.ControlSupport;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.widgets.Widget;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract model representing (e)SWT {@link org.eclipse.swt.widgets.Widget}.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage swt.model.widgets
 */
public abstract class WidgetInfo extends AbstractComponentInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public WidgetInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		rememberVariableNameAsNameData();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Accessors
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * May be overridden by subclasses to cast the widget to its explicit type.
	 *
	 * @return the {@link Widget} created for this {@link WidgetInfo}.
	 */
	protected Widget getWidget() {
		return (Widget) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Initializing
	//
	////////////////////////////////////////////////////////////////////////////
	private void rememberVariableNameAsNameData() {
		addBroadcastListener(new JavaEventListener() {
			@Override
			public void variable_setName(AbstractNamedVariableSupport variableSupport,
					String oldName,
					String newName) throws Exception {
				if (variableSupport.getJavaInfo() == WidgetInfo.this) {
					setVariableNameAsNameData(newName);
				}
			}
		});
	}

	@Override
	public void createExposedChildren() throws Exception {
		super.createExposedChildren();
		createExposedChildren(this);
	}

	/**
	 * Creates exposed children for given SWT {@link JavaInfo}.
	 */
	public static void createExposedChildren(JavaInfo host) throws Exception {
		final List<Class<?>> classList = new ArrayList<>();
		final ClassLoader classLoader = JavaInfoUtils.getClassLoader(host);
		ExecutionUtils.runIgnore(new RunnableEx() {
			@Override
			public void run() throws Exception {
				Class<?> clazz = classLoader.loadClass("org.eclipse.swt.widgets.Widget");
				classList.add(clazz);
			}
		});
		ExecutionUtils.runIgnore(new RunnableEx() {
			@Override
			public void run() throws Exception {
				Class<?> clazz = classLoader.loadClass("org.eclipse.jface.viewers.Viewer");
				classList.add(clazz);
			}
		});
		Class<?>[] classes = classList.toArray(new Class[classList.size()]);
		JavaInfoUtils.addExposedChildren(host, classes);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Rename
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds/modifies {@link Widget#setData(String, Object)} with key "name" and value - new variable
	 * name.
	 *
	 * @param newName
	 *          the new variable name
	 */
	private void setVariableNameAsNameData(String newName) throws Exception {
		if (getDescription().getToolkit().getPreferences().getBoolean(
				IPreferenceConstants.P_VARIABLE_IN_COMPONENT)) {
			String valueSource = StringConverter.INSTANCE.toJavaSource(this, newName);
			// try to find existing setData("name", value);
			{
				List<MethodInvocation> invocations =
						getMethodInvocations("setData(java.lang.String,java.lang.Object)");
				for (MethodInvocation invocation : invocations) {
					Expression keyExpression = (Expression) invocation.arguments().get(0);
					Expression valueExpression = (Expression) invocation.arguments().get(1);
					//
					String key = (String) JavaInfoEvaluationHelper.getValue(keyExpression);
					if ("name".equals(key)) {
						getEditor().replaceExpression(valueExpression, valueSource);
						return;
					}
				}
			}
			// add new setData("name", value);
			{
				String nameSource = StringConverter.INSTANCE.toJavaSource(this, "name");
				String arguments = nameSource + ", " + valueSource;
				addMethodInvocation("setData(java.lang.String,java.lang.Object)", arguments);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// "Live" support
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the instance of {@link SwtLiveManager} to fetch "live" data.
	 */
	protected SwtLiveManager getLiveComponentsManager() {
		return new SwtLiveManager(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Style
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the style of this {@link WidgetInfo}, for existing {@link Object} or "live".
	 */
	public final int getStyle() {
		if (getObject() != null) {
			return ControlSupport.getStyle(getObject());
		} else {
			return getLiveComponentsManager().getStyle();
		}
	}
}
