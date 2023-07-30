/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.forms;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IThisMethodParameterEvaluator;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.rcp.EditorPartInfo;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesNoArguments;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.InvocationHandlerAdapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Model for {@link FormPage}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class FormPageInfo extends EditorPartInfo implements IThisMethodParameterEvaluator {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormPageInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IThisMethodParameterEvaluator
	//
	////////////////////////////////////////////////////////////////////////////
	private Object m_FormToolkit;
	private Object m_FormEditor;

	@Override
	public Object evaluateParameter(EvaluationContext context,
			MethodDeclaration methodDeclaration,
			String methodSignature,
			SingleVariableDeclaration parameter,
			int index) throws Exception {
		ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(parameter);
		if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.ui.forms.editor.FormEditor")) {
			if (m_FormEditor == null) {
				prepare_FormEditor(typeBinding);
			}
			return m_FormEditor;
		}
		return AstEvaluationEngine.UNKNOWN;
	}

	/**
	 * Prepares implementation of {@link FormEditor} in {@link #m_FormEditor}.
	 */
	private void prepare_FormEditor(ITypeBinding editorBinding) throws Exception {
		ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
		String editorClassName = AstNodeUtils.getFullyQualifiedName(editorBinding, true);
		Class<?> editorClass = classLoader.loadClass(editorClassName);
		// prepare FormEditor instance
		{
			m_FormEditor = new ByteBuddy() //
					.subclass(editorClass) //
					.method(named("getActivePageInstance").and(takesNoArguments())) //
					.intercept(FixedValue.nullValue()) //
					.method(named("getToolkit").and(takesNoArguments())) //
					.intercept(InvocationHandlerAdapter.of((Object proxy, Method method, Object[] args) -> {
						prepare_FormToolkit();
						return m_FormToolkit;
					})) //
					.make() //
					.load(classLoader) //
					.getLoaded() //
					.getConstructor() //
					.newInstance();
		}
	}

	/**
	 * Prepares implementation of {@link FormToolkit} in {@link #m_FormToolkit}.
	 */
	private void prepare_FormToolkit() throws Exception {
		if (m_FormToolkit == null) {
			ClassLoader editorLoader = JavaInfoUtils.getClassLoader(this);
			Class<?> class_FormToolkit =
					editorLoader.loadClass("org.eclipse.ui.forms.widgets.FormToolkit");
			//
			Constructor<?> constructor = ReflectionUtils.getConstructor(class_FormToolkit, Display.class);
			m_FormToolkit = constructor.newInstance(Display.getCurrent());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Rendering
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configureTabItem(CTabItem tabItem) throws Exception {
		Optional.ofNullable(getDescription().getIcon()).map(ImageDescriptor::createImage).ifPresent(image -> {
			tabItem.setImage(image);
			tabItem.addDisposeListener(event -> image.dispose());
		});
		tabItem.setText("FormPage");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void refresh_dispose() throws Exception {
		super.refresh_dispose();
		if (m_FormToolkit != null) {
			ReflectionUtils.invokeMethod2(m_FormToolkit, "dispose");
			m_FormToolkit = null;
		}
	}
}
