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

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.InvocationEvaluatorInterceptor;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.util.PlaceholderUtils;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.IExceptionConstants;
import org.eclipse.wb.internal.swt.model.ModelMessages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;

import java.awt.Component;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * For custom SWT {@link Component} try to find and use default constructor.
 *
 * @author scheglov_ke
 * @coverage swt.model
 */
public final class SwtInvocationEvaluatorInterceptor extends InvocationEvaluatorInterceptor {
	////////////////////////////////////////////////////////////////////////////
	//
	// InvocationEvaluatorInterceptor
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public Object evaluate(EvaluationContext context,
			MethodInvocation invocation,
			IMethodBinding methodBinding,
			Class<?> clazz,
			Method method,
			Object[] argumentValues) {
		if ("org.eclipse.ui.plugin.AbstractUIPlugin".equals(clazz.getName())
				&& "imageDescriptorFromPlugin".equals(method.getName())) {
			try {
				URL entry = getEntry((String) argumentValues[0], (String) argumentValues[1]);
				if (entry != null) {
					return ImageDescriptor.createFromURL(entry);
				}
			} catch (Exception e) {
				DesignerPlugin.log(e);
			}
		}
		return AstEvaluationEngine.UNKNOWN;
	}

	/**
	 * @return the {@link URL} for resource in plugin.
	 */
	public static URL getEntry(String symbolicName, String fullPath) throws Exception {
		// try target platform
		{
			IPluginModelBase modelBase = PluginRegistry.findModel(symbolicName);
			String installLocation = modelBase.getInstallLocation();
			if (!StringUtils.isEmpty(installLocation) && installLocation.toLowerCase().endsWith(".jar")) {
				String urlPath = "jar:file:/" + installLocation + "!/" + fullPath;
				urlPath = FilenameUtils.normalize(urlPath, true);
				return new URL(urlPath);
			}
		}
		// try workspace plugin
		{
			IPluginModelBase pluginModel = PluginRegistry.findModel(symbolicName);
			if (pluginModel != null) {
				IResource underlyingResource = pluginModel.getUnderlyingResource();
				if (underlyingResource != null) {
					IProject project = underlyingResource.getProject();
					return project.getFile(new Path(fullPath)).getLocationURI().toURL();
				}
			}
		}
		// try runtime plugin
		{
			Bundle bundle = Platform.getBundle(symbolicName);
			if (bundle != null) {
				return bundle.getEntry(fullPath);
			}
		}
		// not found
		return null;
	}

	@Override
	public Object evaluate(EvaluationContext context,
			ClassInstanceCreation expression,
			ITypeBinding typeBinding,
			Class<?> clazz,
			Constructor<?> actualConstructor,
			Object[] arguments) throws Exception {
		// standard SWT control
		if (isControl(actualConstructor)) {
			return evaluateSWT(context, expression, clazz, actualConstructor, arguments);
		}
		// ComboBoxCellEditor with setItems() argument
		if (ReflectionUtils.isSuccessorOf(clazz, "org.eclipse.jface.viewers.ComboBoxCellEditor")
				&& actualConstructor.getParameterTypes().length >= 2) {
			if (arguments[1] == null) {
				// replace null items array with empty array
				arguments[1] = new String[0];
			}
		}
		return AstEvaluationEngine.UNKNOWN;
	}

	private Object evaluateSWT(EvaluationContext context,
			ClassInstanceCreation expression,
			Class<?> clazz,
			Constructor<?> actualConstructor,
			Object[] arguments) throws Exception {
		PlaceholderUtils.clear(expression);
		Object parent = arguments[0];
		int style = (Integer) arguments[1];
		// try actual constructor
		try {
			return tryToCreate(actualConstructor, arguments);
		} catch (Throwable e) {
			context.addException(expression, e);
			PlaceholderUtils.addException(expression, e);
		}
		// may be it failed because of "null" parent
		if (parent == null) {
			throw new DesignerException(IExceptionConstants.NULL_PARENT);
		}
		// some exception happened, try default constructor (if actual was not default)
		{
			Class<?> parentType = actualConstructor.getParameterTypes()[0];
			Constructor<?> defaultConstructor =
					ReflectionUtils.getConstructor(clazz, parentType, int.class);
			if (defaultConstructor != null
					&& !ReflectionUtils.equals(actualConstructor, defaultConstructor)) {
				try {
					return tryToCreate(defaultConstructor, parent, style);
				} catch (Throwable e) {
					context.addException(expression, e);
					PlaceholderUtils.addException(expression, e);
				}
			}
		}
		// still no success, use placeholder
		PlaceholderUtils.markPlaceholder(expression);
		return createPlaceholder(clazz, parent, style);
	}

	/**
	 * Tries to create {@link Control} using given constructor and arguments. If fails, disposes
	 * partially created {@link Control}-s.
	 */
	private static Object tryToCreate(Constructor<?> actualConstructor, Object... arguments)
			throws Exception {
		Composite parent = (Composite) arguments[0];
		// special case: no parent (probably only for Shell)
		if (parent == null) {
			return actualConstructor.newInstance(arguments);
		}
		// when has parent
		int oldChildrenCount = parent.getChildren().length;
		try {
			return actualConstructor.newInstance(arguments);
		} catch (Throwable e) {
			// dispose new Control(s)
			Control[] newChildren = parent.getChildren();
			for (int i = oldChildrenCount; i < newChildren.length; i++) {
				Control newChild = newChildren[i];
				newChild.dispose();
			}
			// re-throw
			throw ReflectionUtils.getExceptionToThrow(e);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	private static boolean isControl(Constructor<?> constructor) {
		Class<?>[] parameters = constructor.getParameterTypes();
		Class<?> clazz = constructor.getDeclaringClass();
		return ReflectionUtils.isSuccessorOf(clazz, "org.eclipse.swt.widgets.Control")
				&& parameters.length >= 2
				&& ReflectionUtils.isSuccessorOf(parameters[0], "org.eclipse.swt.widgets.Composite")
				&& parameters[1] == int.class;
	}

	/**
	 * @return the {@link Control} to use as placeholder instead of real component that can not be
	 *         created because of some exception.
	 */
	private static Object createPlaceholder(Class<?> clazz, Object parent, int style)
			throws Exception {
		String message =
				MessageFormat.format(
						ModelMessages.SwtInvocationEvaluatorInterceptor_placeholderText,
						CodeUtils.getShortClass(clazz.getName()));
		ClassLoader classLoader = parent.getClass().getClassLoader();
		String script =
				CodeUtils.getSource(
						"import org.eclipse.swt.SWT;",
						"import org.eclipse.swt.graphics.Color;",
						"import org.eclipse.swt.widgets.*;",
						"import org.eclipse.swt.layout.FillLayout;",
						"",
						"composite = new Composite(parent, SWT.NONE);",
						"composite.setLayout(new FillLayout());",
						"",
						"label = new Label(composite, SWT.WRAP | SWT.CENTER);",
						"label.setText(message);",
						"label.setBackground(new Color(null, 0xFF, 0xCC, 0xCC));",
						"",
						"return composite;");
		Map<String, Object> variables = new TreeMap<>();
		variables.put("parent", parent);
		variables.put("message", message);
		return ScriptUtils.evaluate(classLoader, script, variables);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Anonymous
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object evaluateAnonymous(EvaluationContext context,
			ClassInstanceCreation expression,
			ITypeBinding typeBinding,
			ITypeBinding typeBindingConcrete,
			IMethodBinding methodBinding,
			Object[] arguments) throws Exception {
		if (isViewerCreation_withControl(typeBindingConcrete, arguments)
				|| isControlCreation_withParentStyle(typeBindingConcrete, arguments)) {
			String stubClassName = AstNodeUtils.getFullyQualifiedName(typeBindingConcrete, true);
			Class<?> stubClass = context.getClassLoader().loadClass(stubClassName);
			Constructor<?> constructor = ReflectionUtils.getConstructorForArguments(stubClass, arguments);
			if (constructor != null) {
				return constructor.newInstance(arguments);
			}
		}
		return AstEvaluationEngine.UNKNOWN;
	}

	private static boolean isViewerCreation_withControl(ITypeBinding typeBinding, Object[] arguments) {
		return AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.jface.viewers.Viewer")
				&& arguments.length == 1
				&& ReflectionUtils.isSuccessorOf(arguments[0], "org.eclipse.swt.widgets.Control");
	}

	private static boolean isControlCreation_withParentStyle(ITypeBinding typeBinding,
			Object[] arguments) {
		boolean isControl = AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.widgets.Control");
		boolean isViewer = AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.jface.viewers.Viewer");
		return (isControl || isViewer)
				&& arguments.length >= 2
				&& ReflectionUtils.isSuccessorOf(arguments[0], "org.eclipse.swt.widgets.Composite")
				&& ReflectionUtils.isSuccessorOf(arguments[1], "int");
	}
}
