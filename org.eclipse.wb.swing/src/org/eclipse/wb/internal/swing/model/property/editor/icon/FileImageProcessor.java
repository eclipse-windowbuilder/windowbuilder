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
package org.eclipse.wb.internal.swing.model.property.editor.icon;

import org.eclipse.wb.core.editor.icon.AbstractFileImageProcessor;
import org.eclipse.wb.core.model.IGenericProperty;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Default implementation for handling AWT {@link Image}s via the file system.
 */
public final class FileImageProcessor extends AbstractFileImageProcessor {
	@Override
	public boolean process(IGenericProperty property, String[] value) {
		JavaInfo javaInfo = property.getJavaInfo();
		Expression expression = getFinalExpression(javaInfo, property.getExpression());
		if (expression instanceof MethodInvocation invocation) {
			if (AstNodeUtils.isMethodInvocation(invocation, "java.awt.Toolkit", "getImage(java.lang.String)")) {
				Expression pathExpression = DomGenerics.arguments(invocation).get(0);
				String path = (String) JavaInfoEvaluationHelper.getValue(pathExpression);
				value[0] = prefix + path;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean postOpen(IGenericProperty property, String path, String[] value) {
		String pathSource = StringConverter.INSTANCE.toJavaSource(property.getJavaInfo(), path);
		value[0] = "java.awt.Toolkit.getDefaultToolkit().getImage(" + pathSource + ")";
		return true;
	}
}
