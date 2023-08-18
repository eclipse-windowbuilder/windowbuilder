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
package org.eclipse.wb.internal.swt.model.property.editor.image;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.IExpressionEvaluator;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.property.editor.image.plugin.WorkspacePluginInfo;
import org.eclipse.wb.internal.swt.support.ImageSupport;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.io.InputStream;
import java.util.List;

/**
 * Implementation of {@link IExpressionEvaluator} for evaluating plugin image for workspace
 * plugin's.
 *
 * @author lobas_av
 * @coverage swt.property.editor
 */
public class ImageEvaluator implements IExpressionEvaluator {
	private static final String[] IMAGE_SIGNATURES_OLD = {
			"getPluginImage(java.lang.Object,java.lang.String)",
	"getPluginImageDescriptor(java.lang.Object,java.lang.String)"};
	private static final String[] IMAGE_SIGNATURES_NEW = {
			"getPluginImage(java.lang.String,java.lang.String)",
	"getPluginImageDescriptor(java.lang.String,java.lang.String)"};

	////////////////////////////////////////////////////////////////////////////
	//
	// IExpressionEvaluator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object evaluate(EvaluationContext context,
			Expression expression,
			ITypeBinding typeBinding,
			String typeQualifiedName) throws Exception {
		// check for old version ResourceManager
		if (AstNodeUtils.isMethodInvocation(
				expression,
				"org.eclipse.wb.swt.ResourceManager",
				IMAGE_SIGNATURES_OLD)) {
			MethodInvocation invocation = (MethodInvocation) expression;
			List<Expression> arguments = DomGenerics.arguments(invocation);
			IProject project = getProjectOverActivator(arguments.get(0));
			return getPluginImage(invocation, arguments, context, project);
		}
		// we don't understand given expression
		return AstEvaluationEngine.UNKNOWN;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Image utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static Object getPluginImage(MethodInvocation invocation,
			List<Expression> arguments,
			EvaluationContext context,
			final IProject project) throws Exception {
		if (project != null) {
			return getPluginImage(invocation, arguments, context, new InputStreamProvider() {
				@Override
				public InputStream getInputStream(String imagePath) throws Exception {
					IFile file = project.getFile(imagePath);
					return file.exists() ? file.getContents(true) : null;
				}
			});
		}
		return null;
	}

	private static Object getPluginImage(MethodInvocation invocation,
			List<Expression> arguments,
			EvaluationContext context,
			InputStreamProvider provider) throws Exception {
		// prepare image path
		String imagePath = (String) AstEvaluationEngine.evaluate(context, arguments.get(1));
		// load image
		Object image;
		try {
			InputStream stream = provider.getInputStream(imagePath);
			if (stream == null) {
				return null;
			}
			try {
				image = ImageSupport.createImage(stream);
			} finally {
				stream.close();
			}
		} catch (Throwable e) {
			return null;
		}
		// handle for ResourceManager.getPluginImage()
		if ("getPluginImage".equals(invocation.getName().getIdentifier())) {
			return image;
		}
		// handle for ResourceManager.getPluginImageDescriptor()
		Class<?> imageDescriptorClass =
				context.getClassLoader().loadClass("org.eclipse.jface.resource.ImageDescriptor");
		return ReflectionUtils.invokeMethod(
				imageDescriptorClass,
				"createFromImage(org.eclipse.swt.graphics.Image)",
				image);
	}

	private static interface InputStreamProvider {
		InputStream getInputStream(String imagePath) throws Exception;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>[symbolic name, image path]</code> for image property if is't set as
	 *         <code>ResourceManager.getPluginImageXXX()</code> or <code>null</code> otherwise.
	 */
	public static String[] getPluginImageValue(Property property) throws Exception {
		Expression expression = ((GenericProperty) property).getExpression();
		// check for new version ResourceManager
		if (AstNodeUtils.isMethodInvocation(
				expression,
				"org.eclipse.wb.swt.ResourceManager",
				IMAGE_SIGNATURES_NEW)) {
			MethodInvocation invocation = (MethodInvocation) expression;
			List<Expression> arguments = DomGenerics.arguments(invocation);
			String symbolicName = (String) JavaInfoEvaluationHelper.getValue(arguments.get(0));
			String imagePath = (String) JavaInfoEvaluationHelper.getValue(arguments.get(1));
			return new String[]{symbolicName, imagePath};
		}
		// check for old version ResourceManager
		if (AstNodeUtils.isMethodInvocation(
				expression,
				"org.eclipse.wb.swt.ResourceManager",
				IMAGE_SIGNATURES_OLD)) {
			MethodInvocation invocation = (MethodInvocation) expression;
			List<Expression> arguments = DomGenerics.arguments(invocation);
			IProject project = getProjectOverActivator(arguments.get(0));
			// handle only workspace resources
			if (project != null) {
				String symbolicName = WorkspacePluginInfo.getBundleSymbolicName(project);
				if (symbolicName != null) {
					String imagePath = (String) JavaInfoEvaluationHelper.getValue(arguments.get(1));
					return new String[]{symbolicName, imagePath};
				}
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Workspace utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static IProject getProjectOverActivator(Expression activatorAccessNode) throws Exception {
		if (activatorAccessNode instanceof MethodInvocation pluginAccessInvocation) {
			String activatorClass =
					AstNodeUtils.getFullyQualifiedName(pluginAccessInvocation.getExpression(), false);
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (IProject project : projects) {
				if (activatorClass.equals(WorkspacePluginInfo.getBundleActivator(project))) {
					return project;
				}
			}
		}
		return null;
	}
}